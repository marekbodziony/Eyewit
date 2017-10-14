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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.Date;

import marekbodziony.eyewit.api.Api;
import marekbodziony.eyewit.model.Incident;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity{

    private static final int RECORD_REQUEST_CODE = 1;
    private static final int PERMISSION_FINE_LOCATION_REQUEST = 1;

    private ImageButton recordBtn;
    private Button policeBtn;
    private TextView clickTimesTextView;
    private TextView lat;
    private TextView lon;
    private TextView time;

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
        lat = (TextView) findViewById(R.id.lat_val);
        lon = (TextView) findViewById(R.id.lon_val);
        time = (TextView) findViewById(R.id.time_val);
        policeBtn = (Button) findViewById(R.id.policja_btn);

        clickTimesTextView.setText(String.valueOf(clickTimes));

        api = new Api();

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        askForLocationAndIfNeedForPermission();

        time.setText(new Date().toString());

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickTimes > 1){
                    clickTimesTextView.setText(String.valueOf(--clickTimes));
                }else {
                    Intent recordIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (recordIntent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(recordIntent,RECORD_REQUEST_CODE);
                    }
                }
            }
        });

        incident = new Incident();

        policeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String response ="";
                 {
                     new Api.AsyncTask(api) {
                         @Override
                         protected void onPostExecute(String response) {
                             super.onPostExecute(response);
//                             Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                             Log.i("Marek",response);
                         }
                     }.execute("json");
                 }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        clickTimes = 3;
        clickTimesTextView.setText(String.valueOf(clickTimes));
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK){
            //
        }
    }

    // ask for location and permission if needed
    private void askForLocationAndIfNeedForPermission(){
        // if permission isn't granted show dialog prompt with question to give permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST);
        }
        // if permission is granted ask for location update
        else {
            locationProviderClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    lat.setText(String.valueOf(location.getLatitude()));
                    lon.setText(String.valueOf(location.getLongitude()));
                }
            });
        }
    }

}
