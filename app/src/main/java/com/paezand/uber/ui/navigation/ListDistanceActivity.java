package com.paezand.uber.ui.navigation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.paezand.uber.R;
import com.paezand.uber.data.UserLocation;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListDistanceActivity extends AppCompatActivity {

    private final static Integer REQUEST_ACCESS_FINE_LOCATION_CODE = 123;

    @BindView(R.id.distances_container)
    protected ListView distanceListView;

    ArrayList<String> distances = new ArrayList<>();
    ArrayAdapter adapter;

    private Set<UserLocation> userLocationList = new LinkedHashSet<>();

    LocationManager locationManager;

    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_distance);
        ButterKnife.bind(this);

        distances.clear();

        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, distances);

        distanceListView.setAdapter(adapter);
        distanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (Build.VERSION.SDK_INT < 23
                        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (distances.size() > 0 && location != null) {
                        Intent intent = new Intent(getApplicationContext(), DriverActivity.class);
                        List<UserLocation> userList = new ArrayList<>(userLocationList);
                        intent.putExtra("userLocation", userList.get(i));
                        intent.putExtra("driverLocation", location);
                        startActivity(intent);
                    }
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                updateDistanceList(location);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
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

                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location != null) {
                        updateDistanceList(location);
                    }
                }
            }
        }
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

                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    updateDistanceList(location);
                }
            }
        }
    }

    private void updateDistanceList(Location location) {
        distances.clear();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        final ParseGeoPoint geoPointLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        query.whereNear("location", geoPointLocation);
        query.whereDoesNotExist("driverUserName");
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects) {
                            String userName = (String) object.get("username");
                            ParseGeoPoint geoPoint = (ParseGeoPoint) object.get("location");
                            Double distanceInMiles = geoPointLocation.distanceInMilesTo(geoPoint);

                            Double distanceOnDP = (double) Math.round(distanceInMiles * 10) / 10;
                            distances.add(distanceOnDP + " miles");
                            userLocationList.add(new UserLocation(userName, createLocation(geoPoint.getLatitude(), geoPoint.getLongitude())));
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ListDistanceActivity.this, "No active request nearby", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private Location createLocation(Double latitude, Double longitude) {
        Location location = new Location("userlocation");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }
}
