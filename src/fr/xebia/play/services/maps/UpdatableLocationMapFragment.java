package fr.xebia.play.services.maps;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class UpdatableLocationMapFragment extends SupportMapFragment {

    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMap();
    }


    private void setUpMap() {
        mMap = getMap();
        if (mMap == null) {
            Toast.makeText(getActivity(), "Maps initialization failed", Toast.LENGTH_LONG).show();
        }

        mMap.setMyLocationEnabled(true);
    }

    private Marker mLastMarker;

    public void updateLocation(Location location) {
        if (mMap == null) return;

        Toast.makeText(getActivity(), "Accuracy : " + location.getAccuracy() + " provider : " + location.getProvider(),
                Toast.LENGTH_SHORT).show();
        LatLng locationPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if (mLastMarker != null) {
            mLastMarker.remove();
        }
        mLastMarker = mMap.addMarker(new MarkerOptions().position(locationPosition));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPosition, 15));
    }

    public void drawGeofence(Location lastLocation, int geofenceRadius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                .radius(geofenceRadius);

        mMap.addCircle(circleOptions);
    }
}
