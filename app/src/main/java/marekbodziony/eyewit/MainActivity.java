package marekbodziony.eyewit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import marekbodziony.eyewit.api.Api;
import marekbodziony.eyewit.model.Incident;

public class MainActivity extends AppCompatActivity{

    private static final int RECORD_VIDEO_REQUEST_CODE = 1;
    private static final int PERMISSION_FINE_LOCATION_REQUEST_CODE = 2;
    private static final int TURN_ON_GPS_REQUEST_CODE = 3;
    private static final String SENDING_DATA_IN_PROGRESS = "sending in progress";

    private ImageButton recordBtn;
    private Button helpBtn;
    private TextView gpsWarningTextView;
    private TextView clickTimesTextView;
    private ProgressBar sendingProgress;

    private int clickTimes = 3;

    private Incident incident;

    private Api api;

    // GoogleMAps Location
    private FusedLocationProviderClient locationProviderClient;
    private String lat;
    private String lon;

    // Firebase storage
    private FirebaseStorage firebaseStorage;
    private StorageReference videosStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();   // hide actionbar

        // Firebase
        firebaseStorage = FirebaseStorage.getInstance();
        videosStorage = firebaseStorage.getReference().child("videos");

        recordBtn = (ImageButton) findViewById(R.id.record_btn);
        clickTimesTextView = (TextView) findViewById(R.id.click_times_val);
        gpsWarningTextView = (TextView) findViewById(R.id.gps_warning_txt);
        helpBtn = (Button) findViewById(R.id.help_btn);
        sendingProgress = (ProgressBar) findViewById(R.id.sending_progress);

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
                    //recordIntent.putExtra(android.provider.MediaStore.EXTRA_VIDEO_QUALITY, 0);         <--  to lower video quality
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
                Intent helpIntent = new Intent(getApplicationContext(),HelpMeActivity.class);
                startActivity(helpIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clickTimes = 3;
        clickTimesTextView.setText(String.valueOf(clickTimes));
        if (requestCode == RECORD_VIDEO_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            Uri videoUri = data.getData();
            // send video, get video_url and send in JSON
            StorageReference videoRef = videosStorage.child(videoUri.getLastPathSegment());
            videoRef.putFile(videoUri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            sendingProgress.setVisibility(View.VISIBLE);
                            Log.i("Marek","Sending video to Firebase...");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            JSONObject json = new JSONObject();
                            String vidUrl = taskSnapshot.getDownloadUrl().toString();
                            incident.setVideoURL(vidUrl);
                            Log.i("Marek","OK! Video was send to Firebase!");
                            Log.i("Marek","OK! Video URL = " + vidUrl);
                            try {
                                json = putIncidentToJson(incident);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // send json
                            new Api.AsyncTask(api) {
                                @Override
                                protected void onPostExecute(String response) {
                                    super.onPostExecute(response);
                                    sendingProgress.setVisibility(View.INVISIBLE);
                                    Log.i("Marek","Sending JSON to server = " + response);
                                    Toast.makeText(MainActivity.this, "Wysłano", Toast.LENGTH_LONG).show();
                                }
                            }.execute(json.toString());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Marek", "Error! Video wasn't send to Firebase! Check connection with Firebase!");
                            Log.i("Marek", "Error = " + e.getMessage());
                        }
                    });
        }
    }

    // parse Incident to JSON
    private JSONObject putIncidentToJson(Incident inc) throws JSONException{
        JSONObject json = new JSONObject();
        json.put("lat",inc.getLat());
        json.put("lon",inc.getLon());
        json.put("date",String.valueOf(new Date().getTime()));
        json.put("video_url",inc.getVideoURL());
        json.put("important",0);
        Log.i("Marek","JSON = " + json.toString());
        return json;
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
                        lat = String.valueOf(location.getLatitude());
                        lon = String.valueOf(location.getLongitude());
                        incident.setLat(lat);
                        incident.setLon(lon);
                        Log.i("Marek","lat="+lat + ", Lon="+lon);
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
