package com.namminamm.spinit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final String GOOGLE_BROWSER_API_KEY = "AIzaSyDlvNouFUKkAnYCl_lRhECTjoRtJDVss48";
    private FusedLocationProviderClient mFusedLocationClient;
    private Random mRandom;
    private TextView txt;
    private TextView txtRadius;
    private SeekBar seekBar;
    private int locationRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt = (TextView) findViewById(R.id.textView);
        txtRadius = (TextView) findViewById(R.id.txtRadius);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        mRandom = new Random();
        mFusedLocationClient = new FusedLocationProviderClient(this);
        locationRadius = 500;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                locationRadius = progresValue;
                txtRadius.setText(locationRadius+"m");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
        .addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    getRestaurant(task.getResult());
                } else {
                    txt.setText("Location not found!");
                }
            }
        });
    }

    public void onClick(View v) {
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            startLocationPermissionRequest();;
        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void getRestaurant(Location location){
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(location.getLatitude()).append(",").append(location.getLongitude());
        googlePlacesUrl.append("&radius=").append(locationRadius);
        googlePlacesUrl.append("&types=").append("food");
        googlePlacesUrl.append("&sensor=true"); // What does this attribute do?
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, googlePlacesUrl.toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseLocationResult(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        txt.setText("ErrorListener " + e.getMessage());
                    }
                });

        queue.add(jsonRequest);
    }

    private void parseLocationResult(JSONObject result){
        try {
            JSONArray array = result.getJSONArray("results");
            JSONObject obj = array.getJSONObject(mRandom.nextInt(array.length()));
            txt.setText(obj.getString("name"));
        }
        catch (JSONException e){
            txt.setText("parseLocationError " + e.getMessage());
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Do something
    }
}
