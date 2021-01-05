package com.pratik.hitssolution;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestList extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private ListView lvRideRequests;
    private Button btnRefreshRequests;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ArrayList<String> requestNearBy,passengerNames;
    private ArrayAdapter adapter;
    private ArrayList<Double> passengerLat,passengerLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request_list);
        lvRideRequests = findViewById(R.id.lv_request);
        btnRefreshRequests = findViewById(R.id.btn_refresh);
        btnRefreshRequests.setOnClickListener(this);
        requestNearBy = new ArrayList<>();
        passengerLat = new ArrayList<>();
        passengerLong = new ArrayList<>();
        passengerNames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,requestNearBy);
        lvRideRequests.setAdapter(adapter);
        lvRideRequests.setOnItemClickListener(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        requestNearBy.clear();
        if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_driver_logout){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        startActivity(new Intent(DriverRequestList.this,MainActivity.class));
                        finish();
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestListView(currentDriverLocation);
        }
        else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(DriverRequestList.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestList.this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1000);


            }else{
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestListView(currentDriverLocation);
            }
        }
    }

    private void updateRequestListView(Location location) {
        if (location != null) {

            ParseGeoPoint driverLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            ParseQuery<ParseObject> requestQuery = new ParseQuery<ParseObject>("RequestCab");
            requestQuery.whereNotEqualTo("confirm","Done");
            requestQuery.whereNear("passengerLocation",driverLocation);
            requestQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            if (requestNearBy.size() > 0){
                                requestNearBy.clear();
                                passengerLat.clear();
                                passengerLong.clear();
                                passengerNames.clear();
                            }
                            for (ParseObject nearRequest : objects) {
                                ParseGeoPoint pLocation = nearRequest.getParseGeoPoint("passengerLocation");
                                Double distance = driverLocation.distanceInKilometersTo(nearRequest.getParseGeoPoint("passengerLocation"));
                                float roundOffDistance = Math.round(distance * 10) / 10;
                                passengerLat.add(pLocation.getLatitude());
                                passengerLong.add(pLocation.getLongitude());
                                passengerNames.add(nearRequest.get("username")+"");

                                requestNearBy.add(nearRequest.get("username") + " : " + roundOffDistance + "Km away");


                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });

        }else{
            Toast.makeText(DriverRequestList.this,"No request pending",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(DriverRequestList.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentDriverLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                updateRequestListView(currentDriverLocation);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location cdLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (cdLocation != null) {
                Intent intent = new Intent(this,ViewRequestLocationActivity.class);
                intent.putExtra("dLat", cdLocation.getLatitude());
                intent.putExtra("dLong", cdLocation.getLongitude());
                intent.putExtra("pName", passengerNames.get(position));
                intent.putExtra("pLat", passengerLat.get(position));
                intent.putExtra("pLong", passengerLong.get(position));
                startActivity(intent);
            }
        }

    }
}