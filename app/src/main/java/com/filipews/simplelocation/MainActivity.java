package com.filipews.simplelocation;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.filipews.simplelocation.helpers.LocationHelper;
import com.filipews.simplelocation.helpers.PermissionRequestHelper;

public class MainActivity extends AppCompatActivity implements LocationHelper.OnLocationListener, View.OnClickListener {
    private PermissionRequestHelper permissionRequestHelper;
    private LocationHelper locationHelper;
    private TextView text_location;
    private Button btn_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_location = (TextView)findViewById(R.id.text_location);
        btn_location = (Button)findViewById(R.id.btn_location);

        btn_location.setOnClickListener(this);

        permissionRequestHelper = new PermissionRequestHelper(this);
        locationHelper = new LocationHelper(this, permissionRequestHelper, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationHelper.onResume();
    }

    @Override
    protected void onPause() {
        locationHelper.onPause();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        setLocation(locationHelper.getLastKnownLocation());
    }

    private void setLocation(Location location)
    {
        if (location != null) text_location.setText(String.format("Lat: %s | Lng: %s", location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onLocationFound(Location location) {
        setLocation(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionRequestHelper != null) permissionRequestHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}