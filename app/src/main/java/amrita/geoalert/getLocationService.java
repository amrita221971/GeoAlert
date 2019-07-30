package amrita.geoalert;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class getLocationService extends IntentService implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Intent intent;
    //locationRequest = LocationRequest
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation,cl;
   // private String lat, lon;
    public static boolean locationTracking=false;
    public static String str_receiver = "servicetutorial.service.receiver";
    public static final String ACTION_LOCATION_BROADCAST = getLocationService.class.getName() + "LocationBroadcast";


    public getLocationService() {
        super("getLocationService");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null  ) {
            final String action = intent.getAction();
                LocationResult locationResult = LocationResult.extractResult(intent);
                if (locationResult != null) {
                    currentLocation = locationResult.getLastLocation();
                    Log.d("IntentService", "onLocationResult: " + currentLocation.getLatitude() + "  " + currentLocation.getLongitude());
                }
            } else if(intent==null)
            {Log.d("IntentService", "no location"); }

    }


    @Override
    public void onCreate() {

        //Log.d("Service started Getting", "started!!!!!!!!!!");
        //if(locationTracking==true)
        //{Log.d("Service started Getting", "ccccccccccccccccccccccccccccccccc");}

        super.onCreate();
        buildGoogleApiClient();
//        intent = new Intent(str_receiver);

    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
       // Log.d("Connected:Getting ", "LOcation!!!!!!!!!!");

       //startLocationMonitor();

    }


    @Override
    public void onConnectionSuspended(int i) {
       // startLocationMonitor();

    }

    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }


}