package com.paezand.uber.ui.navigation;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paezand.uber.R;
import com.paezand.uber.data.UserLocation;
import com.paezand.uber.ui.main.MainActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng userLatLng;
    private LatLng driverLatLng;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ConstraintLayout mapConstraintLayout = findViewById(R.id.map_constraint_layout);
        mapConstraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(userLatLng)
                        .include(driverLatLng)
                        .build();

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);

                mMap.animateCamera(cameraUpdate);
            }
        });

        UserLocation userLocation = getIntent().getParcelableExtra("userLocation");
        userLatLng = new LatLng(userLocation.getLocation().getLatitude(), userLocation.getLocation().getLongitude());

        Location driverLocation = getIntent().getParcelableExtra("driverLocation");
        driverLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());

        userName = userLocation.getUserName();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(userLatLng).title(userName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @OnClick(R.id.logout_button)
    protected void onLogoutTapped() {
        ParseUser.logOut();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.accept_request)
    protected void onAcceptRequestTapped() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", userName);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        for (ParseObject object : objects) {
                            object.put("driverUserName", ParseUser.getCurrentUser().getUsername());

                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("http://maps.google.com/maps?saddr="
                                                        +driverLatLng.latitude+","
                                                        +driverLatLng.longitude+"&daddr="
                                                        +userLatLng.latitude+","
                                                        +userLatLng.longitude));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }


}
