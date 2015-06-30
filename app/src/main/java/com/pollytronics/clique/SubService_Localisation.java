package com.pollytronics.clique;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pollytronics.clique.lib.base.Blip;
import com.pollytronics.clique.lib.service.CliqueService;
import com.pollytronics.clique.lib.service.SubService;

import java.util.Random;

/**
 * Localisation subservice
 * requests location updates from the google play services on creation
 * and pushes them to the database
 * TODO: figure out wether mGoogleApiClient can be null, ive seen a RunTimeExceptions seemingly caused by such a thing using an emulator AND my old tablet without google services
 * TODO: it says smth like unable to stop CliqueService (IllegalStateException) GoogleApiClient is not connected yet
 * TODO: might want to notify the user to enable its own location. (later with maps, you could run the app without your own location in principle)
 */
public class SubService_Localisation extends SubService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private final String TAG = "SubService_Localisation";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    public SubService_Localisation(CliqueService rs) {
        super(rs);
    }

    private synchronized void buildGoogleApiClient() {
        Context theContext = getCliqueService().getContext();
        mGoogleApiClient = new GoogleApiClient.Builder(theContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void createLocationRequest() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLocationRequest = new LocationRequest();
        int updateTime_ms = (int) getCliquePreferences().getLocalisationUpdateTime_ms();
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
        Log.i(TAG, "onDestroy");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRegister() {
        Log.i(TAG, "onRegister");
    }

    @Override
    public void onUnregister() {
        Log.i(TAG, "onUnregister");
    }

    @Override
    public void onNewSettings() {
        int updateTime_ms = (int) getCliquePreferences().getLocalisationUpdateTime_ms();
        mLocationRequest.setInterval(updateTime_ms);
        Log.i(TAG, "set updateTime to (ms) " + Integer.toString(updateTime_ms));
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, "and re-requested Location Updates with new update rate settings.");
        }
    }

    //-------------------------------------------------

    private void addBlipFromLocation(Location location) {
        //Contact selfContact = getCliqueDb().getSelfContact();
        //selfContact.addBlip(new Blip(location));
        //getCliqueDb().updateSelfContact(selfContact);
        getCliqueDb().addSelfBlip(new Blip(location));
        getCliqueService().notifyNewData();
    }

    /**
     * TODO: remove ths testing location faking
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "woohoo Connected to google location services");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i(TAG, "obtained last location from google location services :" + mLastLocation.toString());
            addBlipFromLocation(mLastLocation);
        } else {
            Log.i(TAG, "could not obtain last location from google location service");
            Log.i(TAG, "will use fake location for testing on emulator");
            mLastLocation = new Blip(51.072478 + (new Random().nextDouble()-.5)*.003, 3.709913 + (new Random().nextDouble()-.5)*.003, System.currentTimeMillis()/1000);
            addBlipFromLocation(mLastLocation);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
   }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "shit, connection to google location services suspended!");
    }

    /**
     * TODO: remove this testing location faking
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection to google location services failed!: "+connectionResult.toString() + " will fake location for testing");
        mLastLocation = new Blip(51.072478 + (new Random().nextDouble()-.5)*.003, 3.709913 + (new Random().nextDouble()-.5)*.003, System.currentTimeMillis()/1000);
        addBlipFromLocation(mLastLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        addBlipFromLocation(location);
        Log.i(TAG, "location changed! to: " + location.toString());
    }
}
