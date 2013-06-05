package fr.xebia.play.services;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class BaseFragmentActivity extends FragmentActivity {

    public static final int CONNECTION_FAILURE_REQUEST = 10000;
    public static final int LOCATION_CONNECTION_FAILURE_REQUEST = 10001;

    protected boolean playServicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        boolean isConnected = resultCode == ConnectionResult.SUCCESS;
        if (!isConnected) {
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, CONNECTION_FAILURE_REQUEST);
            if (errorDialog != null) {
                new ErrorDialogFragment(errorDialog).show(getSupportFragmentManager(), "Connection failure");
            }
        }
        return isConnected;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
