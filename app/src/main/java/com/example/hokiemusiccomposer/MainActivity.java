package com.example.hokiemusiccomposer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    Spinner backSpinner;
    Spinner overlapOne;
    Spinner overlapTwo;
    Spinner overlapThree;

    Button play;

    SeekBar overlapSeekOne;
    SeekBar overlapSeekTwo;
    SeekBar overlapSeekThree;

    MusicService musicService;
    MusicCompletionReceiver musicCompletionReceiver;
    Intent loadPlayScreenIntent;

    boolean isInitialized = false;
    boolean isBound = false;

    public static final String INITIALIZE_STATUS = "initialization status";
    public static final String MUSIC_PLAYING = "music playing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the needed fields and initialize the variables needed.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backSpinner = (Spinner) findViewById(R.id.backgroundSpinner);
        overlapOne = (Spinner) findViewById(R.id.overlapSpinnerOne);
        overlapTwo = (Spinner) findViewById(R.id.overlapSpinnerTwo);
        overlapThree = (Spinner) findViewById(R.id.overlapSpinnerThree);

        String[] backgroundChoices = {"Go Tech Go!", "Mario", "Tetris"};
        ArrayAdapter backgroundAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, backgroundChoices);
        backgroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backSpinner.setAdapter(backgroundAdapter);
        backSpinner.setOnItemSelectedListener(this);

        String[] overlapChoices = {"No Overlap", "Clapping", "Cheering", "Lets Go Hokies"};
        ArrayAdapter overlapAdapterOne = new ArrayAdapter(this, android.R.layout.simple_spinner_item, overlapChoices);
        ArrayAdapter overlapAdapterTwo = new ArrayAdapter(this, android.R.layout.simple_spinner_item, overlapChoices);
        ArrayAdapter overlapAdapterThree = new ArrayAdapter(this, android.R.layout.simple_spinner_item, overlapChoices);

        overlapAdapterOne.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overlapAdapterTwo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overlapAdapterThree.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        overlapOne.setAdapter(overlapAdapterOne);
        overlapTwo.setAdapter(overlapAdapterTwo);
        overlapThree.setAdapter(overlapAdapterThree);

        overlapOne.setOnItemSelectedListener(this);
        overlapTwo.setOnItemSelectedListener(this);
        overlapThree.setOnItemSelectedListener(this);

        play = (Button) findViewById(R.id.playButton);
        play.setOnClickListener(this);

        overlapSeekOne = (SeekBar) findViewById(R.id.overlapSeekBarOne);
        overlapSeekTwo = (SeekBar) findViewById(R.id.overlapSeekBarTwo);
        overlapSeekThree = (SeekBar) findViewById(R.id.overlapSeekBarThree);
        overlapSeekOne.setMax(100);
        overlapSeekTwo.setMax(100);
        overlapSeekThree.setMax(100);
        loadPlayScreenIntent = new Intent(this, PlayActivity.class);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onClick(View view){
        // If the play button is clicked, load the second activity, and send it the user's choices.
        if(view.getId() == play.getId()){
            String backgroundSong = (String) backSpinner.getSelectedItem();
            String overlapSoundOne = (String) overlapOne.getSelectedItem();
            String overlapSoundTwo = (String) overlapTwo.getSelectedItem();
            String overlapSoundThree = (String) overlapThree.getSelectedItem();
            int overlapOnePos = overlapSeekOne.getProgress();
            int overlapTwoPos = overlapSeekTwo.getProgress();
            int overlapThreePos = overlapSeekThree.getProgress();

            loadPlayScreenIntent.putExtra("back song", backgroundSong);
            loadPlayScreenIntent.putExtra("overlap one", overlapSoundOne);
            loadPlayScreenIntent.putExtra("overlap two", overlapSoundTwo);
            loadPlayScreenIntent.putExtra("overlap three", overlapSoundThree);
            loadPlayScreenIntent.putExtra("overlap pos one", String.valueOf(overlapOnePos));
            loadPlayScreenIntent.putExtra("overlap pos two", String.valueOf(overlapTwoPos));
            loadPlayScreenIntent.putExtra("overlap pos three", String.valueOf(overlapThreePos));
            this.startActivity(loadPlayScreenIntent);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isInitialized && !isBound){
            //bindService(startMusicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        }

        registerReceiver(musicCompletionReceiver, new IntentFilter(MusicService.COMPLETE_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isBound){
            //unbindService(musicServiceConnection);
            isBound= false;
        }

        //unregisterReceiver(musicCompletionReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(INITIALIZE_STATUS, isInitialized);
        //outState.putString(MUSIC_PLAYING, music.getText().toString());
        super.onSaveInstanceState(outState);
    }
}