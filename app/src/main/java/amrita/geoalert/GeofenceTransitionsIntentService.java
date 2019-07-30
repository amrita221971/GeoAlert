package amrita.geoalert;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String CHANNEL_ID = "100";
    public GeofenceTransitionsIntentService() {
       // super(name);
        super("GeofenceTransitionsIntentService");
    }


    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {

            Log.e("tttaaaggg", "geofencingEvent not working properly");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        Log.d("integer", "onHandleIntent:  "+geofenceTransition);


        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Geofence geofence = triggeringGeofences.get(0);
            NotificationCompat.Builder nbuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("GeoAlert !")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Log.d("blah", "onHandleIntent: " + geofence.getRequestId().toString());
            if ((geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER | geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)) {
                    Log.d("ttaagg", "You are inside geofence");
                    nbuilder.setContentText("You have reached your destination");
                    mNotificationManager.notify(10, nbuilder.build());

                }
                else {
                    Log.d("ttaagg", "You are outside geofence");
                    nbuilder.setContentText("You have exit the geofence");
                    mNotificationManager.notify(10, nbuilder.build());
                }

            }

    }
}
