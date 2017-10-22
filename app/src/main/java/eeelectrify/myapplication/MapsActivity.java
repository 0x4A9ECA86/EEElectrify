package eeelectrify.myapplication;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener {

    private GoogleMap mMap;
    private Button mBtGoBack;
    private String location = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Log.d("Test", this.toString());

        mBtGoBack = (Button) findViewById(R.id.bt_go_back);
        setOnClick(mBtGoBack, location);
    }

    private void setOnClick(final Button btn, final String location){
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent();
                Log.d("newLocation", location);

                if (location != ""){
                    intent.putExtra("newLocation", location);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }

                finish();
            }
        });
    }

    public String getDecimal(double x){
        int mag = 100000;
        int decimal = (int)(x*mag);
        int integer = ((int)x)*mag;

        decimal = decimal - integer;

        String decString = String.format("%05d", decimal);

        return decString;
    }


    @Override
    public void onMapClick(LatLng point) {
        double x = point.latitude;
        double y = point.longitude;

        Log.d("rawLocation", point.toString());

        location = getDecimal(x) + getDecimal(y);

        setOnClick(mBtGoBack, location);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        double x = point.latitude;
        double y = point.longitude;

        Log.d("rawLocation", point.toString());
        location = getDecimal(x) + getDecimal(y);

        setOnClick(mBtGoBack, location);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Pl ay services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng MDM = new LatLng(14.5519277, 121.0197722);
        mMap.addMarker(new MarkerOptions().position(MDM).title("MDM"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MDM, 18));

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }
}
