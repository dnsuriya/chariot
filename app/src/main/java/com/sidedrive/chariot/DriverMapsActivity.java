package com.sidedrive.chariot;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap m_map;
    private GoogleApiClient m_googleApiClient;
    private Location m_lastLocation;
    private LocationRequest m_locationRequest;
    private static final int REQUEST_LOCATION = 2;

    private String m_riderID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getAssignedRider();
    }

    private void initializeGoogleApiClient() {
        m_googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        m_googleApiClient.connect();
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if((grantResults.length == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) || (grantResults.length == 1
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) ) {
                // We can now safely use the API we requested access to
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                m_map.setMyLocationEnabled(true);
                Toast.makeText(DriverMapsActivity.this, "Location On", Toast.LENGTH_SHORT).show();

                if(m_googleApiClient != null && m_locationRequest != null)
                    LocationServices.FusedLocationApi.requestLocationUpdates(m_googleApiClient, m_locationRequest, this);

            } else {
                // Permission was denied or request was cancelled
            }
        }
    }

    private void getAssignedRider()
    {
        String driverID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference availableDriverLocationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverID).child("RiderID");

        availableDriverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    m_riderID = dataSnapshot.getValue().toString();
                    getAssignedRiderPickupLocation();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedRiderPickupLocation()
    {
        DatabaseReference assignedRiderPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("RiderRequest").child(m_riderID).child("l");

        assignedRiderPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists())
               {
                   List<Object> locationMap = (List<Object>) dataSnapshot.getValue();

                   double locationLat = 0,  locationLng= 0;

                   if(locationMap.get(0) != null && locationMap.get(1) != null)
                   {
                       locationLat = Double.parseDouble(locationMap.get(0).toString());
                       locationLng = Double.parseDouble(locationMap.get(1).toString());
                   }

                   LatLng driverLatLngLocation = new LatLng(locationLat, locationLng);

                   m_map.addMarker(new MarkerOptions().position(driverLatLngLocation).title("Pickup Location"));
               }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;

        initializeGoogleApiClient();



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
            return;
        }
        else {
            m_map.setMyLocationEnabled(true);
        }

        m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //m_map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // m_map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        m_locationRequest = new LocationRequest()
                .setInterval(1000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
            return;
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(m_googleApiClient, m_locationRequest, this);
            Toast.makeText(DriverMapsActivity.this, "Location Requested", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if(getApplicationContext() != null)
        {
            m_lastLocation = location;
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            m_map.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            m_map.animateCamera(CameraUpdateFactory.zoomTo(11));

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference availableDrversRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers");
            DatabaseReference occupiedDriversRef = FirebaseDatabase.getInstance().getReference("OccupiedDrivers");

            GeoFire geoFireAvailableDrversClient = new GeoFire(availableDrversRef);
            GeoFire geoFireOccupiedDriversClient = new GeoFire(occupiedDriversRef);

            switch (m_riderID)
            {
                case "":
                    geoFireOccupiedDriversClient.removeLocation(userID);
                    geoFireAvailableDrversClient.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailableDrversClient.removeLocation(userID);
                    geoFireOccupiedDriversClient.setLocation(userID, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
            }
        }
        else
            Toast.makeText(DriverMapsActivity.this, "Context error", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        super.onStop();

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference availableDrversRef = FirebaseDatabase.getInstance().getReference("AvailableDrivers");

        GeoFire geoFireClient = new GeoFire(availableDrversRef);
        geoFireClient.removeLocation(userID);
    }
}
