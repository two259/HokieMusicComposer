package com.example.hokiemusiccomposer;

import android.media.MediaPlayer;

public class MusicPlayer implements MediaPlayer.OnCompletionListener {

    MediaPlayer player;
    int currentPosition = 0;
    int musicIndex = 0;
    private int musicStatus = 0;//0: before playing, 1 playing, 2 paused
    private MusicService musicService;

    static final int[] MUSICPATH = new int[]{
            R.raw.gotechgo,
            R.raw.lestgohokies,
            R.raw.clapping,
            R.raw.cheering,
            R.raw.mario,
            R.raw.tetris
    };

    static final String[] MUSICNAME = new String[]{
            "Go Tech Go",
            "Lets Go Hokies",
            "Clapping",
            "Cheering",
            "Mario",
            "Tetris"
    };

    public MusicPlayer(MusicService service) {

        this.musicService = service;
    }


    public int getMusicStatus() {

        return musicStatus;
    }

    public String getMusicName() {

        return MUSICNAME[musicIndex];
    }

    public void playMusic(String songName) {
        int foundIndex = 0;
        for(int i = 0; i < MUSICNAME.length; i++){
            if(MUSICNAME[i].equals(songName)) {
                foundIndex = i;
                break;
            }
        }
        player= MediaPlayer.create(this.musicService, MUSICPATH[foundIndex]);
        player.start();
        player.setOnCompletionListener(this);
        musicService.onUpdateMusicName(getMusicName());
        musicStatus = 1;
    }

    public void pauseMusic() {
        if(player!= null && player.isPlaying()){
            player.pause();
            currentPosition= player.getCurrentPosition();
            musicStatus= 2;
        }
    }

    public void resumeMusic() {
        if(player!= null){
            player.seekTo(currentPosition);
            player.start();
            musicStatus=1;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //musicIndex = (musicIndex +1) % MUSICNAME.length;
        //player= MediaPlayer.create(this.musicService, MUSICPATH[0]);
        //player.pause();
        //player.release();
        //player= null;
        //playMusic();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean getIsPlaying(){
        return player.isPlaying();
    }
}
