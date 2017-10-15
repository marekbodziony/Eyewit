package marekbodziony.eyewit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import marekbodziony.eyewit.api.Api;
import marekbodziony.eyewit.model.Incident;

public class MainActivity extends AppCompatActivity{

    private static final int RECORD_VIDEO_REQUEST_CODE = 1;
    private static final int PERMISSION_FINE_LOCATION_REQUEST_CODE = 2;
    private static final int TURN_ON_GPS_REQUEST_CODE = 3;

    private ImageButton recordBtn;
    private Button helpBtn;
    private TextView gpsWarningTextView;
    private TextView clickTimesTextView;

    private int clickTimes = 3;

    private Incident incident;

    private Api api;

    private FusedLocationProviderClient locationProviderClient;
    private String lat;
    private String lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();   // hide actionbar

        recordBtn = (ImageButton) findViewById(R.id.record_btn);
        clickTimesTextView = (TextView) findViewById(R.id.click_times_val);
        gpsWarningTextView = (TextView) findViewById(R.id.gps_warning_txt);
        helpBtn = (Button) findViewById(R.id.help_btn);

        clickTimesTextView.setText(String.valueOf(clickTimes));

        // show GPS warning
        gpsWarningTextView.setVisibility(View.INVISIBLE);


        incident = new Incident();
        api = new Api();

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        askForLocationAndIfNeedForPermission();

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTimes > 1){
                    clickTimesTextView.setText(String.valueOf(--clickTimes));
                }else {
                    Intent recordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (recordIntent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(recordIntent, RECORD_VIDEO_REQUEST_CODE);
                    }
                }
            }
        });

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // display help_me information
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clickTimes = 3;
        clickTimesTextView.setText(String.valueOf(clickTimes));
        if (requestCode == RECORD_VIDEO_REQUEST_CODE && resultCode == RESULT_OK){
            JSONObject json = new JSONObject();

            // send video and get video_url from Firebase
            //
            //

            try {
                json.put("lat","13,0000000");
                json.put("lon","52,0000000");
                json.put("date","1234505");
                json.put("video_url","http://www.google.pl");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // send json
            new Api.AsyncTask(api) {
                @Override
                protected void onPostExecute(String response) {
                    super.onPostExecute(response);
                    Log.i("Marek",response);
                }
            }.execute(json.toString());
        }
    }

    // ask for location and permission if needed
    private void askForLocationAndIfNeedForPermission(){
        // if permission isn't granted show dialog prompt with question to give permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST_CODE);
        }
        // if permission is granted ask for location update
        else {
            locationProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Log.i("Marek","lat="+location.getLatitude() + ", Lon="+location.getLongitude());
                        hideGpsWarning();
                    }
                    else {
                        Log.i("Marek","Error! Włacz GPS!!!");
                        showGpsWarning();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if permission wasn't granted ask again
        if (requestCode == PERMISSION_FINE_LOCATION_REQUEST_CODE && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST_CODE);
        }
    }

    private void showGpsWarning(){
        Animation tapAnim = new AlphaAnimation(0.1f,1.0f);
        tapAnim.setDuration(250);
        tapAnim.setStartOffset(20);
        tapAnim.setRepeatMode(Animation.REVERSE);
        tapAnim.setRepeatCount(Animation.INFINITE);
        gpsWarningTextView.setAnimation(tapAnim);
        gpsWarningTextView.setVisibility(View.VISIBLE);
    }
    private void hideGpsWarning(){
        gpsWarningTextView.setVisibility(View.INVISIBLE);
        gpsWarningTextView.clearAnimation();
    }
}
