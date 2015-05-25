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
 * Localisation subservice
 * requests location updates from the google play services on creation
 * and pushes them to the database
 */
public class SubService_Localisation extends SubService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private final String TAG = "SubService_Localisation";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    public SubService_Localisation(RadarService rs) {
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
        int updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        mLocationRequest.setInterval(updateTime_ms);
        Log.i(TAG, "location request created with update time = " + Integer.toString(updateTime_ms));
        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        buildGoogleApiClient();
        createLocationRequest();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
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
        int updateTime_ms = (int) getRadarPreferences().getLocalisationUpdateTime_ms();
        mLocationRequest.setInterval(updateTime_ms);
        Log.i(TAG, "set updateTime to (ms) "+Integer.toString(updateTime_ms));
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, "and re-requested Location Updates with new update rate settings.");
        }
    }

    //-------------------------------------------------

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
        Log.i(TAG, "Connection to google location services failed!: "+connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        addBlipFromLocation(location);
        Log.i(TAG, "location changed! to: " + location.toString());
    }
}