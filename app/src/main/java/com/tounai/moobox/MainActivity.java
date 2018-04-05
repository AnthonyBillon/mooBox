package com.tounai.moobox;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final int NBSOUNDS = 3;
    final String K_SCORE = "score";
    final String PREFS_NAME = "mooBox";

    private SensorManager mSensorManager;
    private int status;
    private int[] sounds;
    private SoundPool soundPool;
    private boolean loaded;
    private ImageView vBox;


    private int prec;
    private int pprec;

    private int score;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        pprec = -1;
        prec = -1;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        status = 0;
        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder = new SoundPool.Builder().setAudioAttributes(audioAttrib);
        builder.setAudioAttributes(audioAttrib);
        this.soundPool = builder.build();
        addSounds();
        loaded = false;
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        score = prefs.getInt(K_SCORE, 0); //0 is the default value

        vBox = findViewById(R.id.cboite);
        //TODO : modify the token here too !
        MobileAds.initialize(this, "your-token");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }

    private void addSounds() {
        sounds = new int[NBSOUNDS];
        sounds[0] = soundPool.load(this, R.raw.moo1, 1);
        sounds[1] = soundPool.load(this, R.raw.moo2, 1);
        sounds[2] = soundPool.load(this, R.raw.moo3, 1);

    }


    @Override
    protected void onResume() {
        super.onResume();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);


    }


    @Override
    protected void onPause() {
        super.onPause();


        status = 0;
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(K_SCORE, score);
        // Commit the edits!
        editor.apply();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!loaded) return;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] gravity;
            gravity = sensorEvent.values;
            if (status == 0 && gravity[1] < -1 /* flip ! -1 can be changed by another value to be more or less sensitive */) {
                status = 1;
            }
            // and unflip
            if (status == 1 && gravity[1] > 7 /* same comment : 7 is arbitrary ^_^*/) {
                status = 0;

                score++;
                Random r = new Random();
                int rsound;
                do {
                    rsound = r.nextInt(sounds.length);
                } while (rsound == prec || rsound == pprec);
                soundPool.play(this.sounds[rsound], 1, 1, 1, 0, 1f);
                animatePicture();
                pprec = prec;
                prec = rsound;

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void animatePicture() {
        ValueAnimator rotation = ObjectAnimator.ofPropertyValuesHolder(
                vBox, PropertyValuesHolder.ofFloat("rotation", 10f, -10f, 10f, -10f, 10f, -10f, 10f, -10f, 10f, -10f, 0f));
        rotation.setDuration(500);
        rotation.start();
    }

}
