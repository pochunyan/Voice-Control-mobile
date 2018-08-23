package com.example.tony.smarthelper.Geocoding;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class Geocode {
    private static final String TAG = "Geocode";

    Geocoder geocoder;
    Context context;
    List addr = new ArrayList<>();

    LocationManager mLocationManager;

    public Geocode(Context context){
        this.context = context;
        geocoder = new Geocoder(context, Locale.TAIWAN);
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }


    public List findLocation(String address){
        List addressList = null;
        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && addressList.size() > 0) {
                Address addrAns = (Address) addressList.get(0);
                Log.d(TAG, "ADDRESS:" + addrAns);
                addr.add(String.valueOf(addrAns.getLatitude()));
                addr.add(String.valueOf(addrAns.getLongitude()));
                Log.d(TAG, "lat:" + addr.get(0) + " / long:" + addr.get(1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addr;
    }

    public Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
}