package fr.xebia.play.services.location;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.*;
import fr.xebia.play.services.BaseFragmentActivity;
import fr.xebia.play.services.R;
import fr.xebia.play.services.maps.UpdatableLocationMapFragment;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends BaseFragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener , LocationClient.OnRemoveGeofencesResultListener,
        LocationClient.OnAddGeofencesResultListener{

    public static final int GEOFENCE_RADIUS = 100;
    private LocationClient mLocationClient;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private LocationRequest mlocationRequest;
    private UpdatableLocationMapFragment mUpdatableLocationMapFragment;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 20;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    public static final int DETECTION_INTERVAL_SECONDS = 30;
    public static final int DETECTION_INTERVAL_MILLISECONDS = MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
    private PendingIntent mActivityPendingIntent;
    private PendingIntent mGeofencePendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        mUpdatableLocationMapFragment = (UpdatableLocationMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        mLocationClient = new LocationClient(this, this, this);
        mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);

        // Update request
        mlocationRequest = LocationRequest.create();
        // Use high accuracy
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
        mActivityRecognitionClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!playServicesConnected()) {
            Toast.makeText(this, "Fatal error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
            mLocationClient.removeGeofences(mActivityPendingIntent, this);
            mLocationClient.disconnect();
        }

        if(mActivityRecognitionClient.isConnected()) {
            mActivityRecognitionClient.removeActivityUpdates(mActivityPendingIntent);
            mActivityRecognitionClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mLocationClient.isConnected()) {
            Location lastLocation = mLocationClient.getLastLocation();
            mUpdatableLocationMapFragment.updateLocation(lastLocation);

            mLocationClient.requestLocationUpdates(mlocationRequest, this);

            Intent intent = new Intent("fr.xebia.play.services.GeofenceTrigger");
            mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mLocationClient.addGeofences(buildGeofences(), mGeofencePendingIntent, this);
        }

        if (mActivityRecognitionClient.isConnected()) {
            Intent intent = new Intent("fr.xebia.play.services.ActivityUpdate");
            mActivityPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mActivityRecognitionClient.requestActivityUpdates(DETECTION_INTERVAL_MILLISECONDS, mActivityPendingIntent);
        }
    }

    private List<Geofence> buildGeofences() {
        List<Geofence> geofences = new ArrayList<Geofence>();
        Location location = mLocationClient.getLastLocation();
        geofences.add(new Geofence.Builder().setRequestId("ArequestId")
                .setCircularRegion(location.getLatitude(), location.getLongitude(), GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT) //
                .build()); //
        return geofences;
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Location client services", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LOCATION_CONNECTION_FAILURE_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                Log.e("Error", e.getMessage());
            }
        } else {
            Toast.makeText(this, "Error code received : " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!mLocationClient.isConnected()) return;

        mUpdatableLocationMapFragment.updateLocation(mLocationClient.getLastLocation());
    }

    @Override
    public void onRemoveGeofencesByRequestIdsResult(int i, String[] strings) {
        // TODO
    }

    @Override
    public void onRemoveGeofencesByPendingIntentResult(int i, PendingIntent pendingIntent) {
       // TODO
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {
        if(LocationStatusCodes.SUCCESS == i){
            mUpdatableLocationMapFragment.drawGeofence(mLocationClient.getLastLocation(), GEOFENCE_RADIUS);
        }
    }

    public static class ActivityRecognitionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                // Get the update
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                // Get the most probable activity
                DetectedActivity mostProbableActivity = result.getMostProbableActivity();
                /*
                 * Get the probability that this activity is the
                 * the user's actual activity
                 */
                int confidence = mostProbableActivity.getConfidence();

                /*
                 * Get an integer describing the type of activity
                 */
                int activityType = mostProbableActivity.getType();
                String activityName = getNameFromType(activityType);

                Toast.makeText(context, "Activity : " + activityName + " confidence : " + confidence, Toast.LENGTH_LONG).show();
            } else if (LocationClient.hasError(intent)){
                Log.e("Location service error ", Integer.toString(LocationClient.getErrorCode(intent)));
            }

        }

        private String getNameFromType(int activityType) {
            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    return "in_vehicle";
                case DetectedActivity.ON_BICYCLE:
                    return "on_bicycle";
                case DetectedActivity.ON_FOOT:
                    return "on_foot";
                case DetectedActivity.STILL:
                    return "still";
                case DetectedActivity.UNKNOWN:
                    return "unknown";
                case DetectedActivity.TILTING:
                    return "tilting";
            }
            return "unknown";
        }
    }

    public static class GeofenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationClient.hasError(intent)){
                Log.e("Location service error ", Integer.toString(LocationClient.getErrorCode(intent)));
            } else {
                int geofenceTransition = LocationClient.getGeofenceTransition(intent);
                Log.i("Geofence trigger", getGeofenceTransitionFromCode(geofenceTransition));
            }
        }

        private String getGeofenceTransitionFromCode(int transitionCode) {
            switch (transitionCode) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    return "enter geofence";
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    return "exit geofence";
            }
            return "unknown";
        }
    }
}
