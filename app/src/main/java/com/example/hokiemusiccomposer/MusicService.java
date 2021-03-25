package com.example.hokiemusiccomposer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class MusicService extends Service {

    MusicPlayer musicPlayers[];
    private final IBinder iBinder= new MyBinder();

    public static final String COMPLETE_INTENT = "complete intent";
    public static final String MUSICNAME = "music name";

    @Override
    public void onCreate() {
        super.onCreate();
        // Create 4 music players, one for each sound.
        musicPlayers = new MusicPlayer[4];
        musicPlayers[0] = new MusicPlayer(this);
        musicPlayers[1] = new MusicPlayer(this);
        musicPlayers[2] = new MusicPlayer(this);
        musicPlayers[3] = new MusicPlayer(this);
    }

    public void startMusic(String songName, int num){

        musicPlayers[num].playMusic(songName);
    }

    public void pauseMusic(int num){

        musicPlayers[num].pauseMusic();
    }

    public void resumeMusic(int num){

        musicPlayers[num].resumeMusic();
    }

    public void restartMusic(String songName) {
        if(musicPlayers[0].getIsPlaying()) musicPlayers[0].pauseMusic();
        if(musicPlayers[1].getIsPlaying()) musicPlayers[1].pauseMusic();
        if(musicPlayers[2].getIsPlaying()) musicPlayers[2].pauseMusic();
        if(musicPlayers[3].getIsPlaying()) musicPlayers[3].pauseMusic();
        //musicPlayers.pauseMusic();
        musicPlayers[0].onCompletion(null);
        musicPlayers[1].onCompletion(null);
        musicPlayers[2].onCompletion(null);
        musicPlayers[3].onCompletion(null);
        musicPlayers[0].playMusic(songName);
    }

    public int getPlayingStatus(int num){

        return musicPlayers[num].getMusicStatus();
    }


    public void onUpdateMusicName(String musicname) {
        Intent intent = new Intent(COMPLETE_INTENT);
        intent.putExtra(MUSICNAME, musicname);
        sendBroadcast(intent);
    }

    public int getDuration(int num){
        return musicPlayers[num].getDuration();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }


    public class MyBinder extends Binder {

        MusicService getService(){
            return MusicService.this;
        }
    }

    public boolean getIsPlaying(int num){
        return musicPlayers[num].getIsPlaying();
    }
}