package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageTrash;
    private MediaPlayer mMediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageTrash = findViewById(R.id.image_trash);

        content();
    }

    private class GetData extends AsyncTask<Integer, Void, Integer>{
        @Override
        protected Integer doInBackground(Integer... strings) {
            final int[] volume = new int[1];
            String url = "https://api.thingspeak.com/channels/929191/feeds.json?api_key=YO9ELQ27P06ZGQGX";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray feed = response.getJSONArray("feeds");
                        for (int i = 0; i < feed.length(); i++) {
                            JSONObject feedObject = feed.getJSONObject(i);
                            volume[0] = feedObject.getInt("field1");
                            Log.e("VOLUME",volume[0]+"");
                        }
                        if (volume[0] >= 35){
                            mImageTrash.setImageResource(R.drawable.trash_zero);
                        }else if (volume[0] >= 25){
                            mImageTrash.setImageResource(R.drawable.trash_twenty_five);
                        }else if (volume[0] >= 20){
                            mImageTrash.setImageResource(R.drawable.trash_fifty);
                        }else if (volume[0] >= 10){
                            mImageTrash.setImageResource(R.drawable.trash_seventy_five);
                        }else {
                            mImageTrash.setImageResource(R.drawable.trash_full);
                            proximity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR GET DATA", error.getMessage());
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(jsonObjectRequest);

            return volume[0];
        }
    }

    private void content(){
        new GetData().execute();

        refresh();
    }

    private void refresh(){

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                content();
            }
        };
        handler.postDelayed(runnable,5000);
    }

    private void proximity(){
        //Proximity Sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //Vibrator Sensor
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (event.values[0] < sensorProximity.getMaximumRange()) {
                    if (Build.VERSION.SDK_INT >= 26){
                        vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE));
                    }else {
                        vibrator.vibrate(500);
                    }
                }else {
                    mMediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.tone);
                    mMediaPlayer.start();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager.registerListener(listener, sensorProximity, 2 * 1000 * 1000);
    }
}
