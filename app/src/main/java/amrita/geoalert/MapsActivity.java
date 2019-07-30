package amrita.geoalert;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleMap mMap;
    private EditText searchtext;
    private Button searchbutton;
    private boolean searching = false;
    private boolean createGeofencebool = false;



    public GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent,locationPendingIntent;
    private Marker geoFenceMarker;
    private Location currentLocation ;
    private Circle geoFenceLimits;


    private ArrayList<Geofence> geofenceList = new ArrayList<Geofence>();

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maplayout);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        searchtext = findViewById(R.id.searchEditText);
        searchbutton = findViewById(R.id.searchButton);

        setSupportActionBar(myToolbar);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googlemap);

        mapFragment.getMapAsync(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        geofencingClient = LocationServices.getGeofencingClient(this);
        currentLocation = new Location("");//provider name is unnecessary
        currentLocation.setLatitude(22);
        currentLocation.setLongitude(22);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);
        //googleApiClient = new GoogleApiClient.Builder(this)

        mMap.setOnMyLocationButtonClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_FINE_LOCATION);
            return;
        }else
            {  mMap.setMyLocationEnabled(true);
                if (googleApiClient == null) {
                    googleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .build();

                }
                if (googleApiClient != null) {
                    googleApiClient.connect();
                }
            }




    }

    ///////////////////////////////////////////my methods////////////////////////

    ////go to a location with given lat long
    public void goToLoc(double lat, double lng) {
        LatLng loc = new LatLng(lat, lng);
        //mMap.addMarker(new MarkerOptions().position(loc).title("Marker set"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));
    }

    ////////functionality for search button
    public void findOnMap(View v) {
        searching = true;
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> list = geocoder.getFromLocationName(searchtext.getText().toString(), 1);
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(getApplicationContext(), locality, Toast.LENGTH_SHORT).show();
            double lat = add.getLatitude();
            double lng = add.getLongitude();
            goToLoc(lat, lng);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private PendingIntent getLocationPendingIntent() {
        Log.d("method", "getLocationPendingIntent ");
        // Reuse the PendingIntent if we already have it.
        if (locationPendingIntent != null) {
            return locationPendingIntent;
        }
        Intent intent = new Intent(this, getLocationService.class);

        locationPendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return locationPendingIntent;
    }

    public void startLocationMonitor() {
        getLocationService.locationTracking=true;

        locationRequest = LocationRequest.create()
                .setInterval(8000)
                .setFastestInterval(5000)
                .setMaxWaitTime(10000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                super.onLocationResult(locationResult); // why? this. is. retarded. Android.
//                currentLocation = locationResult.getLastLocation();
//                Log.d("tahhhh", "onLocationResult: " + currentLocation.getLatitude() + "  " + currentLocation.getLongitude());
//
//            }
//        };


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        try {
            //fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);//Looper.myLooper());
            fusedLocationClient.requestLocationUpdates(locationRequest, getLocationPendingIntent());
            } catch (SecurityException e) {
                Log.d("taaaaggg", e.getMessage());
                }


    }




    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences( geofenceList);
        return builder.build();
    }


    private PendingIntent getGeofencePendingIntent() {
        Log.d("method", "getGeofencePendingIntent ");
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private void startGeofence() {
        Log.i("method", "startGeofence()");
        if (geoFenceMarker != null) {
            LatLng latlng = geoFenceMarker.getPosition();
            Geofence g = new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    .setRequestId("My Geofence")
                    .setCircularRegion(latlng.latitude, latlng.longitude, 500)
                    .setExpirationDuration(10 * 24 * 60 * 60 * 60)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .setLoiteringDelay(200)
                    .build();
            geofenceList.add(g);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
                return;
            }
            else
                {
                    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("kkkkk", "onSuccess: added");
                                }
                            })
                            .addOnFailureListener(this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("kkkkk", "onFailure: not added");                            }
                            });
                }
            if(geoFenceLimits!=null)
            {geoFenceLimits.remove();}
            CircleOptions circleOptions = new CircleOptions()
                    .center(latlng)
                    .strokeWidth(2f)
                    .strokeColor(Color.blue(100))
                    .fillColor(Color.argb(100, 0, 0, 20))
                    .radius(1000);
            geoFenceLimits = mMap.addCircle( circleOptions );
            createGeofencebool = false;

        } else {
            Log.e("error : ", "set Geofence marker ");
        }

    }

    private void destroyGeofence()
    {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("tag", "onSuccess: geofences removed");                                // ...
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("tag", "onfailure: geofences were not removed");
                    // ...
                }
            });
        geoFenceLimits.remove();
        geoFenceMarker.remove();
    }

    //////////////////////////////////////////////////////////////////////////////

    @Override
    public void onConnected(@Nullable Bundle bundle) {/////when the map gets connected
     startLocationMonitor();
        googleApiClient.connect();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onMapClick(LatLng latLng) {
        if (createGeofencebool == true)
        {
            MarkerOptions markeroptions = new MarkerOptions()
                    .position(latLng)
                    .title(latLng.toString());
            if (mMap != null)
            {
                // Remove the anterior marker
                if (geoFenceMarker != null)
                    geoFenceMarker.remove();
                geoFenceMarker = mMap.addMarker(markeroptions);
                Log.d("ttaagg", "onMapClick:  "+latLng.latitude+" "+latLng.longitude);

                startGeofence();
            }

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

            menu.findItem(R.id.menuCreateGeofence).setVisible(true);
            menu.findItem(R.id.menuDestroyGeofence).setVisible(true);
            //menu.findItem(R.id.menuStart).setVisible(true);
            //menu.findItem(R.id.menuStop).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuCreateGeofence:
                createGeofencebool=true;
                break;
            case R.id.menuDestroyGeofence:
                createGeofencebool=false;
                destroyGeofence();
                break;
//            case R.id.menuStart:
//                startLocationMonitor();
//                break;
//            case R.id.menuStop:
//                getLocationService.locationTracking=false;
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if(mMap!=null)
        {goToLoc(currentLocation.getLatitude(),currentLocation.getLongitude());}
        return false;
    }

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {

            case MY_PERMISSION_REQUEST_FINE_LOCATION:


                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission was granted do nothing and carry on

                } else {

                    Toast.makeText(getApplicationContext(), "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();

                    finish();

                }

                break;

        }

    }

    @Override
    protected void onStart()
    {super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}




