package com.example.fakedatingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.imperiumlabs.geofirestore.GeoFirestore;
import org.imperiumlabs.geofirestore.GeoQuery;
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener;

import java.util.ArrayList;

public class SetupActivity extends AppCompatActivity {

    EditText nameField;
    EditText ageField;
    Button registerbtn;
    Spinner sex;

    private static final String TAG = "SetupActivity";

    private FirebaseFirestore mDb;
    private FusedLocationProviderClient mFusedLocationClient;
    private User mUser;
    private CollectionReference mCr;
    private GeoFirestore geoFirestore;
    LocationRequest mLocationRequest;
    private ArrayList<User> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        nameField = findViewById(R.id.fullName);
        ageField = findViewById(R.id.age);
        registerbtn = findViewById(R.id.register);
        sex = findViewById(R.id.gender);
        String[] items = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_dropdown_item, items);
        sex.setAdapter(adapter);

        mDb = FirebaseFirestore.getInstance();
        mUser = new User();
        mUserList = new ArrayList<>();

        mCr = mDb.collection(getString(R.string.collection_users));
        geoFirestore = new GeoFirestore(mCr);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetupActivity.this.register();
            }
        });
    }

    boolean getMale() {
        if (sex.getSelectedItem().toString().equals("Male"))
            return true;
        else
            return false;
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    mUser.setTimestamp(null);
                    mUser.setL(geoPoint);
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Give Permission")
                        .setMessage("Give Permission Message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void register() {
        String temp = ageField.getText().toString();
        int age = 0;
        if (! "".equals(temp))
            age = Integer.parseInt(temp);

        User user = new User(
                FirebaseAuth.getInstance().getUid(),
                nameField.getText().toString(),
                age,
                getMale(),
                mUser.getTimestamp());

        mDb.collection(getString(R.string.collection_users))
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        System.out.println("added*@#*)#");
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        geoFirestore.setLocation(documentReference.getId(),
                                new GeoPoint(mUser.getL().getLatitude(), mUser.getL().getLongitude()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error adding document", e);
                    }
                });

        startGeoQuery();
    }

    private void startGeoQuery() {
        GeoQuery geoQuery = geoFirestore.queryAtLocation(new GeoPoint(mUser.getL().getLatitude(), mUser.getL().getLongitude()), 1);

        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDocumentEntered(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {
                User user = documentSnapshot.toObject(User.class);
                mUserList.add(user);
            }

            @Override
            public void onDocumentMoved(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {

            }

            @Override
            public void onDocumentExited(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                mUserList.remove(user);
            }

            @Override
            public void onDocumentChanged(DocumentSnapshot documentSnapshot, GeoPoint geoPoint) {

            }

            @Override
            public void onGeoQueryReady() {
                Intent intent = new Intent(SetupActivity.this, DiscoverActivity.class);
                intent.putParcelableArrayListExtra("User List", mUserList);
                startActivity(intent);
            }

            @Override
            public void onGeoQueryError(Exception e) {

            }
        });

    }

}