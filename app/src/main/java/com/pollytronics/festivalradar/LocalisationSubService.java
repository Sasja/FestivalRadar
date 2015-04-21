package com.pollytronics.festivalradar;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pollytronics.festivalradar.lib.RadarBlip;
import com.pollytronics.festivalradar.lib.RadarContact;

/**
 * Mock SubService to spoof own location lookup
 * it periodically updates the self-contact in the database
 * Created by pollywog on 9/23/14.
 */
public class LocalisationSubService extends AbstractSubService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private final String TAG = "LocalisationSubService";
    private int updateTime_ms;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    public LocalisationSubService(RadarService rs) {
        super(rs);
    }

    protected synchronized void buildGoogleApiClient() {
        Context theContext = getRadarService().getContext();
        mGoogleApiClient = new GoogleApiClient.Builder(theContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(updateTime_ms);
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        buildGoogleApiClient();
        createLocationRequest();
        mGoogleApiClient.connect();
        updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        getMainHandler().post(localiseLoop);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        getMainHandler().removeCallbacks(localiseLoop);
    }

    @Override
    protected void onRegister() {
        Log.i(TAG,"onRegister");
    }

    @Override
    protected void onUnregister() {
        Log.i(TAG,"onUnregister");
    }

    @Override
    protected void onNewSettings() {
        mLocationRequest.setInterval(updateTime_ms);            // TODO this wont work yet, updating this variable does not tell android anything
        getMainHandler().removeCallbacks(localiseLoop);         // TODO remove this looping thing, im using google service events instead.
        updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        getMainHandler().post(localiseLoop);
    }

    //-------------------------------------------------

    private final Runnable localiseLoop = new Runnable() {
        @Override
        public void run() {
            try{
//                RadarContact selfContact = getRadarDatabase().getSelfContact();
//                if (mGoogleApiClient.isConnected()) {
//                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                    Log.i(TAG, "wow, got an actual location from google!! :" + mLastLocation.toString());
//                    selfContact.addBlip(new RadarBlip(mLastLocation));
//                } else {
//                    Log.i(TAG, "Not connected to google location services so no location availabe!");
//                }
////                selfContact.addBlip(selfContact.getLastBlip().brownian(0.00001).reClock());
////                Log.i(TAG ,"spoofed new position in LocalisationSubService "+selfContact.getLastBlip().toString());
//                getRadarDatabase().updateSelfContact(selfContact);
//                getRadarService().notifyNewData();
                getMainHandler().postDelayed(localiseLoop,updateTime_ms);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void addBlipFromLocation(Location location) {
        RadarContact selfContact = getRadarDatabase().getSelfContact();
        selfContact.addBlip(new RadarBlip(location));
        getRadarDatabase().updateSelfContact(selfContact);
        getRadarService().notifyNewData();

    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "woohoo Connected to google location services");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.i(TAG, "wow, got an actual location from google!! :" + mLastLocation.toString());
        addBlipFromLocation(mLastLocation);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
   }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "shit, connection to google location services suspended!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "even worse! Connection to google location services failed!: "+connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        addBlipFromLocation(location);
        Log.i(TAG, "location changed! to: " + location.toString());
    }
}
