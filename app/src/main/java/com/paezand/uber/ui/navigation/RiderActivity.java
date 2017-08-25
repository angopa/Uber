package com.paezand.uber.ui.navigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paezand.uber.R;
import com.paezand.uber.data.UserLocation;
import com.paezand.uber.ui.main.MainActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private final static Integer REQUEST_ACCESS_FINE_LOCATION_CODE = 123;

    @BindView(R.id.manage_button)
    protected Button manageButton;

    @BindView(R.id.logout_button)
    protected Button logout;

    @BindView(R.id.info_text)
    protected TextView infoTextView;

    LocationManager locationManager;

    LocationListener locationListener;

    GoogleMap gMap;

    private Location lastKnowLocation;

    private boolean isActiveRequest = false;

    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        ButterKnife.bind(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        isActiveRequest = true;
                        manageButton.setText("Cancel Uber");
                        checkForUpdates();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                lastKnowLocation = location;
                updateLocation();
            }

            @Override
            public void onStatusChanged(final String s, final int i, final Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(final String s) {

            }

            @Override
            public void onProviderDisabled(final String s) {

            }
        };

        updateLastKnownLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnowLocation != null) {
                        updateLocation();
                    }
                }
            }
        }
    }

    @OnClick(R.id.manage_button)
    protected void onManageButtonTapped() {
        if (isActiveRequest) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.deleteInBackground();
                            }
                            isActiveRequest = false;
                            manageButton.setText("Call an Uber");
                            checkForUpdates();
                        }
                    }
                }
            });
        } else {
            if (lastKnowLocation != null) {
                ParseObject request = new ParseObject("Request");
                request.put("username", ParseUser.getCurrentUser().getUsername());
                ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
                request.put("location", parseGeoPoint);

                request.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            manageButton.setText("Cancel Uber");
                            isActiveRequest = true;
                            checkForUpdates();
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Could not find location. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkForUpdates() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");

        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverUserName");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("username", objects.get(0).getString("driverUserName"));
                        query.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> objects, ParseException e) {
                                if (e == null && objects.size() > 0){
                                    ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");

                                    if (ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                        if (location != null) {
                                            ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

                                            Double distanceInMiles = driverLocation.distanceInMilesTo(userLocation);

                                            Double distanceOnDP = (double) Math.round(distanceInMiles * 10) / 10;

                                            infoTextView.setText("Your driver is "+distanceOnDP.toString() +" miles away!");

                                        }

                                    }
                                }
                            }
                        });


                        manageButton.setVisibility(View.INVISIBLE);

                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkForUpdates();
                        }
                    }, 2000);
                }
            }
        });
    }

    @OnClick(R.id.logout_button)
    protected void onLogoutTapped() {
        ParseUser.logOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void updateLocation() {
        LatLng userLocation = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        gMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
    }

    public void updateLastKnownLocation() {
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_FINE_LOCATION_CODE);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnowLocation != null) {
                    updateLocation();
                }
            }
        }
    }
}
