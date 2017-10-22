package eeelectrify.myapplication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener {

    private static WifiConfiguration wifiConfig = null;
    private GoogleMap mMap;
    private Button mBtSetLocation;
    private String myLocation = "";
    private Marker myMarker;
    private Marker beaconMarker;
    private String beacon = "";
    private String rawLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mBtSetLocation = findViewById(R.id.bt_set_position);
        setOnClick(mBtSetLocation, this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
            if(!Settings.System.canWrite(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                this.startActivityForResult(intent, 111);
            }
        }else{
            updateLocation(this);
        }

    }

    public String getDecimal(double x){
        int mag = 100000;
        int decimal = (int)(x*mag);
        int integer = ((int)x)*mag;

        decimal = decimal - integer;

        String decString = String.format("%05d", decimal);

        return decString;
    }

    private void setOnClick(final Button btn, final Context context){
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setLocation(context);
            }
        });
    }

    public void updateMarker(LatLng point){
        if(myMarker != null){myMarker.remove();}
        myMarker = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    public void beaconMarker(LatLng point){
        if(beaconMarker != null){beaconMarker.remove();}
        beaconMarker = mMap.addMarker(new MarkerOptions().position(point));
        Log.d("beaconLocation", point.toString());
    }

    @Override
    public void onMapClick(LatLng point) {
        updateMarker(point);
        double x = point.latitude;
        double y = point.longitude;

        Log.d("rawLocation", point.toString());

        rawLocation = getDecimal(x) + getDecimal(y);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        updateMarker(point);
        mMap.addMarker(new MarkerOptions().position(point).title("MDM"));
        double x = point.latitude;
        double y = point.longitude;

        Log.d("rawLocation", point.toString());
        rawLocation = getDecimal(x) + getDecimal(y);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                myLocation = data.getStringExtra("newLocation");
                updateLocation(this);
            }
        }
        if (requestCode == 111 && Settings.System.canWrite(this)){
            Log.d("TAG", "CODE_WRITE_SETTINGS_PERMISSION success");
            finish();
        }
    }

    public void updateLocation(Context context){
        getScanResults(context);

        setOnClick(mBtSetLocation, this);
    }

    public void setLocation(Context context){
        updateLocation(context);
        startBeacon(context);
        Log.d("setLocation", myLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1001 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocation(this);
        }
    }

    public ArrayList<String> getScanResults(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> list = null;
        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, wifiConfig, false);
            Log.d("scan", String.valueOf(apstatus));
        } catch (Exception e) {
            Log.d("scan", e.toString());
        }

        while(!wifiManager.setWifiEnabled(true)){;}

        SystemClock.sleep(5000);

        boolean f = wifiManager.startScan();

        if(!f){
            return getScanResults(context);
        }else{
            list = wifiManager.getScanResults();
        }

        Log.d("testResults", list.toString());

        ArrayList<String> res = new ArrayList<>();

        for (ScanResult result : list){
            Log.d("SSIDs", result.SSID);
            if (result.SSID.length() == 10 && result.SSID.matches("^[0-9]*$")) {
                beacon = result.SSID;
                if(myMarker != null){
                    LatLng base = myMarker.getPosition();
                    double x = Integer.parseInt(beacon.substring(0, 4));
                    double y = Integer.parseInt(beacon.substring(5, 9));
                    Log.d("x", String.valueOf(x));
                    Log.d("y", String.valueOf(y));
                    beaconMarker(new LatLng((int)base.latitude + x/10000.00f, (int)base.longitude + y/10000.00f));
                }
            }
            if (result.SSID.length() == 20 && result.SSID.matches("^[0-9]*$")) {
                if(result.SSID.substring(0,9) != myLocation){
                    beacon = result.SSID.substring(0,9);
                } else {
                    beacon = result.SSID.substring(10,19);
                }
                if(myMarker != null){
                    LatLng base = myMarker.getPosition();
                    double x = Integer.parseInt(beacon.substring(0, 4));
                    double y = Integer.parseInt(beacon.substring(5, 9));
                    Log.d("x", String.valueOf(x));
                    Log.d("y", String.valueOf(y));
                    beaconMarker(new LatLng(((int)base.latitude + (x/10000.00f)), ((int)base.longitude + (y/10000.00f))));
                }
            }
        }
        myLocation = rawLocation + beacon;
        Log.d("getResults", beacon);

        return res;
    }

    public void startBeacon(Context context) {
        Log.d("Beacon", "Starting Beacon");
        WifiManager wifimanager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        wifimanager.setWifiEnabled(false);

        try {
            Method getConfigMethod = wifimanager.getClass().getMethod("getWifiApConfiguration");
            wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifimanager);
            wifiConfig.SSID = myLocation;

            Method setWifiApMethod = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifimanager, wifiConfig, true);
            Log.d("Beacon", String.valueOf(apstatus));
        } catch (Exception e) {
            Log.d("Beacon", e.toString());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng MDM = new LatLng(14.5519277, 121.0207722);
        myMarker = mMap.addMarker(new MarkerOptions().position(MDM).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MDM, 17.5f));

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }
}
