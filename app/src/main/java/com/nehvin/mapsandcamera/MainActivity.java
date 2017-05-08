package com.nehvin.mapsandcamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    boolean mapReady=false;
    private LocationManager locMgr;
    private LocationListener locListner;
    private Button btnMumbai;
    private Button btnSFO;
    private Button btnSydney;

    private static final CameraPosition Mumbai = CameraPosition.builder()
            .target(new LatLng(18.9388656,72.8349698))
            .bearing(0)
            .zoom(15)
            .tilt(0)
            .build();

    private static final CameraPosition SFO = CameraPosition.builder()
            .target(new LatLng(37.7578,-122.5076))
            .bearing(0)
            .zoom(15)
            .tilt(0)
            .build();

    private static final CameraPosition Sydney = CameraPosition.builder()
            .target(new LatLng(-33.8678,151.1924))
            .bearing(0)
            .zoom(15)
            .tilt(0)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnMumbai = (Button) findViewById(R.id.btnMumbai);
        btnSydney = (Button) findViewById(R.id.btnSydney);
        btnSFO = (Button) findViewById(R.id.btnSFO);



        btnMumbai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    flyTo(Mumbai);
                }
            }
        });

        btnSydney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    flyTo(Sydney);
                }
            }
        });

        btnSFO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady) {
                    flyTo(SFO);
                }
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void flyTo(CameraPosition cp)
    {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp),5000,null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location localLocation = fetchBestLocation();
        locListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location){

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
        if (Build.VERSION.SDK_INT < 23)
        {
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
        }
        else
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            else
            {
                locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
                Location location = fetchBestLocation();
                updateCurrentLoc(location);
            }
        }
    }
    private void updateCurrentLoc(Location lastUnkownLocation) {
        LatLng currentLocation = new LatLng(lastUnkownLocation.getLatitude(), lastUnkownLocation.getLongitude());

//        String nameOfPlace = getAddressOnMarker(lastUnkownLocation);

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(currentLocation)); //.title("Your Current Location")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
    }

    private Location fetchBestLocation() {
        Location locationGPS = null;
        Location locationNetwork = null;

        // get both but return more accurate of GPS & network location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationGPS = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationNetwork = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (locationGPS == null && locationNetwork == null)
            return null;
        else
        if (locationGPS == null)
            return locationNetwork;
        else
        if (locationNetwork == null)
            return locationGPS;
        else
            return (locationGPS.getAccuracy() < locationNetwork.getAccuracy() ? locationGPS : locationNetwork);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListner);
        }
        Location lastUnkownLocation = fetchBestLocation();
        if (lastUnkownLocation != null) {
            updateCurrentLoc(lastUnkownLocation);
        }
    }
}