package fr.xebia.play.services;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import fr.xebia.play.services.location.LocationActivity;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startActivity(new Intent(this, LocationActivity.class));
    }
}
