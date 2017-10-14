package marekbodziony.eyewit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import marekbodziony.eyewit.api.Api;
import marekbodziony.eyewit.model.Incident;

public class MainActivity extends AppCompatActivity{

    private static final int RECORD_VIDEO_REQUEST_CODE = 1;
    private static final int PERMISSION_FINE_LOCATION_REQUEST_CODE = 1;

    private ImageButton recordBtn;
    private Button helpBtn;
    private TextView clickTimesTextView;

    private int clickTimes = 3;
    private Incident incident;

    private Api api;

    private FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();   // hide actionbar

        recordBtn = (ImageButton) findViewById(R.id.record_btn);
        clickTimesTextView = (TextView) findViewById(R.id.click_times_val);
        helpBtn = (Button) findViewById(R.id.help_btn);

        clickTimesTextView.setText(String.valueOf(clickTimes));

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

        incident = new Incident();

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response ="";
                JSONObject json = new JSONObject();
                try {
                    json.put("lat","13,0000000");
                    json.put("lon","52,0000000");
                    json.put("date","1234505");
                    json.put("video_url","http://www.google.pl");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                {
                     new Api.AsyncTask(api) {
                         @Override
                         protected void onPostExecute(String response) {
                             super.onPostExecute(response);
//                             Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                             Log.i("Marek",response);
                         }
                     }.execute(json.toString());
                 }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clickTimes = 3;
        clickTimesTextView.setText(String.valueOf(clickTimes));
        if (requestCode == RECORD_VIDEO_REQUEST_CODE && resultCode == RESULT_OK){
            //
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
                    // set lon and lat
                }
            });
        }
    }

}
