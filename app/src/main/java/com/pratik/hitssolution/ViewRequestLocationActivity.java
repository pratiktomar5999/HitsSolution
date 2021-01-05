package com.pratik.hitssolution;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestLocationActivity extends FragmentActivity implements OnMapReadyCallback , View.OnClickListener{

    private GoogleMap mMap;
    private Button btnConfirmRide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        btnConfirmRide = findViewById(R.id.btnConfirmRide);
        btnConfirmRide.setOnClickListener(this);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


//        // Add a marker in Sydney and move the camera
        LatLng pLocation = new LatLng(getIntent().getDoubleExtra("pLat",0), getIntent().getDoubleExtra("pLong",0));
        LatLng dLocation = new LatLng(getIntent().getDoubleExtra("dLat",0), getIntent().getDoubleExtra("dLong",0));
//        mMap.addMarker(new MarkerOptions().position(pLocation).title(getIntent().getStringExtra("pName")));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pLocation,15));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Marker dMarker = mMap.addMarker(new MarkerOptions().position(dLocation).title("You"));
        Marker pMarker = mMap.addMarker(new MarkerOptions().position(pLocation).title(getIntent().getStringExtra("pName")));

        ArrayList<Marker> markers = new ArrayList<>();
        markers.add(dMarker);
        markers.add(pMarker);

        for(Marker marker : markers){

            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,0);
        mMap.animateCamera(cameraUpdate);


    }

    @Override
    public void onClick(View v) {
        ParseQuery<ParseObject> pRequestQuery = ParseQuery.getQuery("RequestCab");
        pRequestQuery.whereEqualTo("username",getIntent().getStringExtra("pName"));
        pRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null){

                    for (ParseObject object : objects){
                        object.put("confirm","Done");
                        object.put("confirmedBy", ParseUser.getCurrentUser().getUsername());
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null){
                                    Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/maps?saddr="
                                                            + getIntent().getDoubleExtra("dLat",0) + ","
                                                            + getIntent().getDoubleExtra("dLong",0) + "&" + "daddr="
                                                            + getIntent().getDoubleExtra("pLat",0) + ","
                                                            + getIntent().getDoubleExtra("pLong",0)));
                                    startActivity(googleIntent);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}