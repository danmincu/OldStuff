package com.example.myapp.Location;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class Location implements ILocation{

    public Location(Context contextWrapper){

            LocationManager locationManager = (LocationManager)contextWrapper.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(android.location.Location location) {
                    Location.this.location = location;
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    float accuracy = location.getAccuracy();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationListener);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locationListener);}


    android.location.Location location = null;
    @Override
    public android.location.Location getLocation() {
        return this.location;
    }
}


