package com.filipews.simplelocation.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/*
 * This is intended to help handle permissions required by the app.
 */
public class PermissionRequestHelper
{
    private final static int PERMISSION_REQUEST_LOCATION = 101;
    private final static int PERMISSION_REQUEST_CAMERA = 102;
    private final static int PERMISSION_REQUEST_LOGIN_INFO = 103;
    private final static int PERMISSION_REQUEST_PHONE_CALL = 104;
    private PermissionCallbacks pendingCallbacks;
    private Fragment fragment;
    private Activity activity;

    public interface PermissionCallbacks
    {
        void onPermissionGranted();
        void onPermissionDenied();
        void onRationaleRequired();
    }

    /**
    * When using this class the fragment or activity MUST override the onRequestPermissionsResult method and pass it onto the helper!!
    * */
    public PermissionRequestHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public PermissionRequestHelper(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPhoneCallRationale()
    {
        return checkRationale(new String[] { Manifest.permission.CALL_PHONE });
    }

    public void requestAccountInfo(PermissionCallbacks callbacks)
    {
        request(callbacks, PERMISSION_REQUEST_LOGIN_INFO, new String[] { Manifest.permission.GET_ACCOUNTS }, false);
    }

    public void requestPhoneCall(PermissionCallbacks callbacks)
    {
        requestPhoneCall(callbacks, false);
    }

    public void requestPhoneCall(PermissionCallbacks callbacks, boolean forceRequestPermission)
    {
        request(callbacks, PERMISSION_REQUEST_PHONE_CALL, new String[] { Manifest.permission.CALL_PHONE }, forceRequestPermission);
    }

    public void requestCamera(PermissionCallbacks callbacks)
    {
        requestCamera(callbacks, false);
    }

    public void requestCamera(PermissionCallbacks callbacks, boolean forceRequestPermission)
    {
        request(callbacks, PERMISSION_REQUEST_CAMERA, new String[]{Manifest.permission.CAMERA}, forceRequestPermission);
    }

    public void requestLocation(PermissionCallbacks callbacks)
    {
        requestLocation(callbacks, false);
    }

    public void requestLocation(PermissionCallbacks callbacks, boolean forceRequestPermission)
    {
        request(callbacks, PERMISSION_REQUEST_LOCATION, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, forceRequestPermission);
    }

    private boolean checkRationale(String[] permissions)
    {
        if (permissions != null && fragment != null)
        {
            boolean granted = true;
            for (String s:permissions)
            {
                if (ContextCompat.checkSelfPermission(fragment.getContext(), s) != PackageManager.PERMISSION_GRANTED)
                {
                    granted = false;
                    break;
                }
            }
            if (!granted)
            {
                for (String s:permissions)
                {
                    if (fragment.shouldShowRequestPermissionRationale(s))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void request(PermissionCallbacks callbacks, int permissionGroup, String[] permissions, boolean forceRequestPermission)
    {
        this.pendingCallbacks = callbacks;
        if (callbacks != null)
        {
            if (fragment != null)
            {
                boolean granted = true;
                for (String s:permissions)
                {
                    if (ContextCompat.checkSelfPermission(fragment.getContext(), s) != PackageManager.PERMISSION_GRANTED)
                    {
                        granted = false;
                        break;
                    }
                }
                if (!granted)
                {
                    boolean requestPermissionRationale = false;
                    for (String s:permissions)
                    {
                        if (fragment.shouldShowRequestPermissionRationale(s))
                        {
                            requestPermissionRationale = true;
                            break;
                        }
                    }

                    // Should we show an explanation?
                    if (requestPermissionRationale && !forceRequestPermission)
                    {
                        callbacks.onRationaleRequired();
                    }
                    else
                    {
                        fragment.requestPermissions(permissions, permissionGroup);
                    }
                }
                else callbacks.onPermissionGranted();
            }
            else if (activity != null)
            {
                if (permissions != null)
                {
                    boolean granted = true;
                    for (String s:permissions)
                    {
                        if (ContextCompat.checkSelfPermission(activity, s) != PackageManager.PERMISSION_GRANTED)
                        {
                            granted = false;
                            break;
                        }
                    }
                    if (!granted)
                    {
                        boolean requestPermissionRationale = false;
                        for (String s:permissions)
                        {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, s))
                            {
                                requestPermissionRationale = true;
                                break;
                            }
                        }

                        // Should we show an explanation?
                        if (requestPermissionRationale && !forceRequestPermission)
                        {
                            callbacks.onRationaleRequired();
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(activity, permissions, permissionGroup);
                        }
                    }
                    else callbacks.onPermissionGranted();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (pendingCallbacks != null)
        {
            if (grantResults != null && grantResults.length > 0)
            {
                for (int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        //If any of the permissions is denied we take it as if all of them were
                        pendingCallbacks.onPermissionDenied();
                        return;
                    }
                }
                //If no permission is denied then we assume everything is fine
                pendingCallbacks.onPermissionGranted();
                return;
            }
            //If there is no return on the results something went wrong. Assume no permissions were granted
            pendingCallbacks.onPermissionDenied();
        }
    }
}