package com.filipews.simplelocation.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    public interface OnLocationListener
    {
        void onLocationFound(Location location);
        //void onConnectionFailed(ConnectionResult connectionResult);
    }

    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private PermissionRequestHelper permissionRequestHelper;
    private OnLocationListener callback;

    public LocationHelper(Context context, PermissionRequestHelper permissionRequestHelper, OnLocationListener callback)
    {
        this.context = context;
        this.permissionRequestHelper = permissionRequestHelper;
        this.callback = callback;
    }

    public void onPause()
    {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onResume()
    {
        if (mGoogleApiClient == null) buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient()
    {
        if (permissionRequestHelper != null)
        {
            permissionRequestHelper.requestLocation(new PermissionRequestHelper.PermissionCallbacks()
            {
                @Override
                public void onPermissionGranted()
                {
                    if (mGoogleApiClient == null)
                    {
                        mGoogleApiClient = new GoogleApiClient.Builder(context)
                                .addConnectionCallbacks(LocationHelper.this)
                                .addOnConnectionFailedListener(LocationHelper.this)
                                .addApi(LocationServices.API)
                                .build();
                    }
                    if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) mGoogleApiClient.connect();
                }

                @Override
                public void onPermissionDenied()
                {
                }

                @Override
                public void onRationaleRequired()
                {
                }
            });
        }
    }

    public Location getLastKnownLocation()
    {
        Location location = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (location == null)
        {
            final LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            location = getLastKnownLocation(context, locationManager, LocationManager.GPS_PROVIDER);
            if (location == null) location = getLastKnownLocation(context, locationManager, LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private Location getLastKnownLocation(Context context, LocationManager manager, String provider)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                return manager.getLastKnownLocation(provider);
        }
        else
        {
            return manager.getLastKnownLocation(provider);
        }
        return null;
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        if (callback != null) callback.onLocationFound(getLastKnownLocation());
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}