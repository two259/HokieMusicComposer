package com.example.hokiemusiccomposer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



public class PlayActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    MyAsyncTask myAsyncTask;
    int count;

    String backgroundSong;
    String overlapSoundOne;
    String overlapSoundTwo;
    String overlapSoundThree;
    String overlapPosOne;
    String overlapPosTwo;
    String overlapPosThree;
    int overlapPosOneInt;
    int overlapPosTwoInt;
    int overlapPosThreeInt;
    int adjustedPosOne;
    int adjustedPosTwo;
    int adjustedPosThree;

    int overlapOneDuration;
    int overlapTwoDuration;
    int overlapThreeDuration;


    int backgroundSongDuration;
    int backgroundSongDurationSeconds;

    TextView backgroundSongView;
    Button playButton;
    Button restartButton;
    ImageView currentImage;

    MusicService backgroundSongService;
    MusicCompletionReceiver musicCompletionReceiver;
    Intent startMusicServiceIntent;

    boolean isInitialized = false;
    boolean isBound = false;

    public static final String INITIALIZE_STATUS = "initialization status";
    public static final String MUSIC_PLAYING = "music playing";

    boolean firstOverlapStarted = false;
    boolean secondOverlapStarted = false;
    boolean thirdOverlapStarted = false;

    /**
     * Create the activity and initialize the fields needed.
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        backgroundSongView = (TextView) findViewById(R.id.backgroundSongTitle);
        playButton = (Button) findViewById(R.id.playpausebutton);
        restartButton = (Button) findViewById(R.id.restartButton);
        currentImage = (ImageView) findViewById(R.id.imageView);

        playButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);

        // This section retrieves the user's choices from the main activity.
        backgroundSong = intent.getStringExtra("back song");
        overlapSoundOne = intent.getStringExtra("overlap one");
        overlapSoundTwo = intent.getStringExtra("overlap two");
        overlapSoundThree = intent.getStringExtra("overlap three");
        overlapPosOne = intent.getStringExtra("overlap pos one");
        overlapPosTwo = intent.getStringExtra("overlap pos two");
        overlapPosThree = intent.getStringExtra("overlap pos three");
        overlapPosOneInt = Integer.valueOf(overlapPosOne);
        overlapPosTwoInt = Integer.valueOf(overlapPosTwo);
        overlapPosThreeInt = Integer.valueOf(overlapPosThree);

        backgroundSongView.setText(backgroundSong);

        if(savedInstanceState != null){
            isInitialized = savedInstanceState.getBoolean(INITIALIZE_STATUS);
        }

        startMusicServiceIntent= new Intent(this, MusicService.class);


        if(!isInitialized){
            startService(startMusicServiceIntent);
            isInitialized= true;
        }

        musicCompletionReceiver = new MusicCompletionReceiver(this);
        myAsyncTask = new MyAsyncTask();
        overlapOneDuration = 0;
        overlapTwoDuration = 0;
        overlapThreeDuration = 0;
    }

    @Override
    public void onClick(View v) {
        // If the play button was clicked. Below are different scenarios for what mode we are in.
        if(v.getId() == playButton.getId()){
            // If this is the first time playing.
            if(playButton.getText().equals("Play")){
                backgroundSongService.startMusic(backgroundSong, 0);
                backgroundSongDuration = backgroundSongService.getDuration(0);
                backgroundSongDurationSeconds = backgroundSongDuration / 1000;
                startTimerForOverlaps(backgroundSongDuration);
                adjustedPosOne = (overlapPosOneInt * backgroundSongDurationSeconds) / 100;
                adjustedPosTwo = (overlapPosTwoInt * backgroundSongDurationSeconds) / 100;
                adjustedPosThree = (overlapPosThreeInt * backgroundSongDurationSeconds) / 100;
                // Check to see if any overlaps start at zero.
                checkStartOverlaps(0);
                playButton.setText("Pause");
            }
            // If the music is playing now.
            else if(playButton.getText().equals("Pause")){
                if(backgroundSongService.getIsPlaying(0)) {
                    backgroundSongService.pauseMusic(0);
                }
                if(firstOverlapStarted && backgroundSongService.musicPlayers[1] != null  && backgroundSongService.getIsPlaying(1)) {
                    backgroundSongService.pauseMusic(1);
                }
                if(secondOverlapStarted && backgroundSongService.musicPlayers[2] != null && backgroundSongService.getIsPlaying(2)) {
                    backgroundSongService.pauseMusic(2);
                }
                if(thirdOverlapStarted && backgroundSongService.musicPlayers[3] != null && backgroundSongService.getIsPlaying(3)) {
                    backgroundSongService.pauseMusic(3);
                }
                boolean check = false;
                myAsyncTask.cancel(check);
                playButton.setText("Resume");
            }
            // If the music is paused.
            else if(playButton.getText().equals("Resume")){
                backgroundSongService.resumeMusic(0);
                if(firstOverlapStarted && !checkIsFinished(1)) backgroundSongService.resumeMusic(1);
                if(secondOverlapStarted && !checkIsFinished(2)) backgroundSongService.resumeMusic(2);
                if(thirdOverlapStarted && !checkIsFinished(3)) backgroundSongService.resumeMusic(3);
                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute(backgroundSongDurationSeconds);
                playButton.setText("Pause");
            }
        }
        // If the user pressed the restart button.
        // Resets the needed fields for a new execution, and starts the music.
        else if(v.getId() == restartButton.getId()){
            if(backgroundSongService.getIsPlaying(0)) backgroundSongService.pauseMusic(0);
            if(firstOverlapStarted && backgroundSongService.getIsPlaying(1)) backgroundSongService.pauseMusic(1);
            if(secondOverlapStarted && backgroundSongService.getIsPlaying(2)) backgroundSongService.pauseMusic(2);
            if(thirdOverlapStarted && backgroundSongService.getIsPlaying(3)) backgroundSongService.pauseMusic(3);
            firstOverlapStarted = false;
            secondOverlapStarted = false;
            thirdOverlapStarted = false;
            //backgroundSongService.pauseMusic();
            backgroundSongService.startMusic(backgroundSong, 0);
            //backgroundSongService.restartMusic(backgroundSong);
            backgroundSongDuration = backgroundSongService.getDuration(0);
            backgroundSongDurationSeconds = backgroundSongDuration / 1000;
            boolean check = false;
            myAsyncTask.cancel(check);
            myAsyncTask = new MyAsyncTask();
            count = 0;
            overlapOneDuration = 0;
            overlapTwoDuration = 0;
            overlapThreeDuration = 0;
            startTimerForOverlaps(backgroundSongDuration);
            adjustedPosOne = (overlapPosOneInt * backgroundSongDurationSeconds) / 100;
            adjustedPosTwo = (overlapPosTwoInt * backgroundSongDurationSeconds) / 100;
            adjustedPosThree = (overlapPosThreeInt * backgroundSongDurationSeconds) / 100;
            // Check to see if any overlaps start at zero.
            checkStartOverlaps(0);
            playButton.setText("Pause");
            currentImage.setImageResource(R.drawable.gotechgoimage);
        }
    }

    /**
     * Private method that checks to see if its time to start a sound.
     * @param currTime
     * @return
     */
    private int checkStartOverlaps(int currTime){
        if(currTime == adjustedPosOne){
            if(!overlapSoundOne.equals("No Overlap")){
                backgroundSongService.startMusic(overlapSoundOne, 1);
                overlapOneDuration = backgroundSongService.getDuration(1);
                firstOverlapStarted = true;
                return 1;
            }
        }
        if(currTime == adjustedPosTwo){
            if(!overlapSoundTwo.equals("No Overlap")){
                backgroundSongService.startMusic(overlapSoundTwo, 2);
                overlapTwoDuration = backgroundSongService.getDuration(2);
                secondOverlapStarted = true;
                return 2;
            }
        }
        if(currTime == adjustedPosThree){
            if(!overlapSoundThree.equals("No Overlap")){
                backgroundSongService.startMusic(overlapSoundThree, 3);
                overlapThreeDuration = backgroundSongService.getDuration(3);
                thirdOverlapStarted = true;
                return 3;
            }
        }
        return 0;
    }

    /**
     * Checks to see if the overlapped sound is finished.
     * @param num
     * @return
     */
    private boolean checkIsFinished(int num){
        boolean checker = false;
        if(num == 1 && firstOverlapStarted){
            if(count >= (overlapOneDuration / 1000) + adjustedPosOne){
                return true;
            }
        }
        else if(num == 2 && secondOverlapStarted){
            if(count >= (overlapTwoDuration / 1000) + adjustedPosTwo){
                return true;
            }
        }
        else if(num == 1 && thirdOverlapStarted){
            if(count >= (overlapThreeDuration / 1000) + adjustedPosThree){
                return true;
            }
        }
        return checker;
    }

    private void startTimerForOverlaps(int duration){
        myAsyncTask.execute(duration);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isInitialized && !isBound){
            bindService(startMusicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        }

        registerReceiver(musicCompletionReceiver, new IntentFilter(MusicService.COMPLETE_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(isBound){
            unbindService(musicServiceConnection);
            isBound= false;
        }

        unregisterReceiver(musicCompletionReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(INITIALIZE_STATUS, isInitialized);
        outState.putInt("counter", count);
        outState.putString("status", playButton.getText().toString());
        outState.putInt("background duration", backgroundSongDurationSeconds);
        outState.putInt("overlap1 duration", overlapOneDuration);
        outState.putInt("overlap2 duration", overlapTwoDuration);
        outState.putInt("overlap3 duration", overlapThreeDuration);
        outState.putInt("overlap1 pos", adjustedPosOne);
        outState.putInt("overlap2 pos", adjustedPosTwo);
        outState.putInt("overlap3 pos", adjustedPosThree);
        outState.putBoolean("overlap1 started", firstOverlapStarted);
        outState.putBoolean("overlap2 started", secondOverlapStarted);
        outState.putBoolean("overlap3 started", thirdOverlapStarted);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        this.count = savedInstanceState.getInt("counter");
        playButton.setText(savedInstanceState.getString("status"));
        this.overlapOneDuration = savedInstanceState.getInt("overlap1 duration");
        this.overlapTwoDuration = savedInstanceState.getInt("overlap2 duration");
        this.overlapThreeDuration = savedInstanceState.getInt("overlap3 duration");
        this.adjustedPosOne = savedInstanceState.getInt("overlap1 pos");
        this.adjustedPosTwo = savedInstanceState.getInt("overlap2 pos");
        this.adjustedPosThree = savedInstanceState.getInt("overlap3 pos");
        this.firstOverlapStarted = savedInstanceState.getBoolean("overlap1 started");
        this.secondOverlapStarted = savedInstanceState.getBoolean("overlap2 started");
        this.thirdOverlapStarted = savedInstanceState.getBoolean("overlap3 started");
        backgroundSongDurationSeconds = savedInstanceState.getInt("background duration");
        if(playButton.getText().equals("Pause")){
            myAsyncTask.execute(backgroundSongDurationSeconds);
        }
    }

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
            backgroundSongService = binder.getService();
            isBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            backgroundSongService = null;
            isBound = false;
        }
    };

    private class MyAsyncTask extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            while(count < params[0]){
                try{
                    //checking if the asynctask has been cancelled, end loop if so
                    if(isCancelled()) break;

                    Thread.sleep(1000);


                    int checker = checkStartOverlaps(count);
                    if(checker > 0) publishProgress(checker);
                    count++;


                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //setting count to 0 and setting textview to 0 after doInBackground finishes running
            count= 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Changes the image as needed.
            if(values[0] == 1){
                if(!overlapSoundOne.equals("No Overlap")){
                    if(overlapSoundOne.equals("Cheering")){
                        currentImage.setImageResource(R.drawable.cheeringimage);
                    }
                    else if(overlapSoundOne.equals("Clapping")){
                        currentImage.setImageResource(R.drawable.clappingimage);
                    }
                    else {
                        currentImage.setImageResource(R.drawable.letsgohokiesimage);
                    }
                }
            }
            if(values[0] == 2){
                if(!overlapSoundTwo.equals("No Overlap")){
                    if(overlapSoundTwo.equals("Cheering")){
                        currentImage.setImageResource(R.drawable.cheeringimage);
                    }
                    else if(overlapSoundTwo.equals("Clapping")){
                        currentImage.setImageResource(R.drawable.clappingimage);
                    }
                    else {
                        currentImage.setImageResource(R.drawable.letsgohokiesimage);
                    }
                }
            }
            if(values[0] == 3){
                if(!overlapSoundThree.equals("No Overlap")){
                    if(overlapSoundThree.equals("Cheering")){
                        currentImage.setImageResource(R.drawable.cheeringimage);
                    }
                    else if(overlapSoundThree.equals("Clapping")){
                        currentImage.setImageResource(R.drawable.clappingimage);
                    }
                    else {
                        currentImage.setImageResource(R.drawable.letsgohokiesimage);
                    }
                }
            }
        }
    }
}
