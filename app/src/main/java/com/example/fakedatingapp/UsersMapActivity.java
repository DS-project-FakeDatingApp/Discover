package com.example.fakedatingapp;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsersMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mLogout;

    private LatLng currentLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_map);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent (UsersMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            }
            else {
                checkLocationPermission();
            }
        }

    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {

                mLastLocation = location;

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersAvailable");

                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("You"));

                    //getNearbyUsers();

                if (!getUsersAroundStarted)
                    getUsersAround();
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission")
                        .setMessage("Please give permission")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                ActivityCompat.requestPermissions(UsersMapActivity.this, new String [] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                .create()
                .show();
            }
            else {
                ActivityCompat.requestPermissions(UsersMapActivity.this, new String [] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                       mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                       mMap.setMyLocationEnabled(true);
                   }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    boolean getUsersAroundStarted = false;
    List<Marker> markerList = new ArrayList<>();
    int radius = 1;

    private void getUsersAround() {
        getUsersAroundStarted = true;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference usersAroundLocation = FirebaseDatabase.getInstance().getReference().child("UsersAvailable");

        GeoFire geoFire = new GeoFire(usersAroundLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);

        Toast.makeText(UsersMapActivity.this, "Searching within " + radius + " km radius", Toast.LENGTH_SHORT).show();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                nearbyUsersFound = true;
                for (Marker marker : markerList) {
                    if (marker.getTag().equals(key)) {
                        return;
                    }
                }

                LatLng usersAroundLocation = new LatLng(location.latitude, location.longitude);

                Marker usersMarker = mMap.addMarker(new MarkerOptions().position(usersAroundLocation).title(key).icon(BitmapDescriptorFactory.defaultMarker(342)));
                usersMarker.setTag(key);

                markerList.add(usersMarker);

            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : markerList) {
                    if (marker.getTag().equals(key)) {
                        marker.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : markerList) {
                    if (marker.getTag().equals(key)) {
                        marker.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }

            }

            @Override
            public void onGeoQueryReady() {
                if(!nearbyUsersFound) {
                    radius++;
                    Toast.makeText(UsersMapActivity.this, "Searching within " + radius + " radius", Toast.LENGTH_SHORT).show();
                    getNearbyUsers();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    List<String> nearbyUsersList = new ArrayList<String>();
    boolean nearbyUsersFound = false;

    // get all nearby users:
    private void getNearbyUsers() {

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        DatabaseReference usersAroundLocation = FirebaseDatabase.getInstance().getReference().child("UsersAvailable");

        GeoFire geoFire = new GeoFire(usersAroundLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            // user has been found within the radius:
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                nearbyUsersFound = true;
                String nearbyUsersFoundID = key;
                if (nearbyUsersList.contains(nearbyUsersFoundID)) {
                    Log.d("MainActivity", "The nearby users list already contains this user!");
                } else {

                    if (userId!=nearbyUsersFoundID)
                        nearbyUsersList.add(nearbyUsersFoundID);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            // all users within the radius have been identified:
            @Override
            public void onGeoQueryReady() {
                // recurse if no users have been found within the radius:
                if(!nearbyUsersFound) {
                    radius++;
                    getNearbyUsers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
// end of getNearbyUsers()
}
