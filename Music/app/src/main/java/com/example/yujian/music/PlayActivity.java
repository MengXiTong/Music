package com.example.yujian.music;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yujian.library.RoundImageView;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

    private RoundImageView imgCover;
    private ImageView imgStart;
    private ImageView imgPrev;
    private ImageView imgNext;
    private ImageView imgPlayType;
    private TextView tvName;
    private TextView tvSinger;
    private TextView tvSound;
    private TextView tvNowTime;
    private TextView tvAllTime;
    private SeekBar seekBar;
    private boolean isStop=true;    //判断是否音乐播放状态
    private MusicService musicService;
    private boolean mBound=false;
    private String path="";
    private int index=-1;     //当前所播放的音乐索引
    private MyDatabaseHelper myDatabaseHelper;
    private List<Music> musicList;
    private int musicNowTime;
    private Timer mTimer;
    private int type = 0;  //播放类型，循环播放0，单曲播放1，随机播放2；
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initView();
        Intent intent=new Intent(this,MusicService.class);
        bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
        Toast.makeText(PlayActivity.this,"服务开启",Toast.LENGTH_SHORT).show();
    }

    //初始化控件属性
    private void initView(){
        Intent intent = getIntent();
        index = intent.getIntExtra("index",-1);
        imgCover = (RoundImageView)findViewById(R.id.img_cover);
        myDatabaseHelper = new MyDatabaseHelper(this);
        musicList = myDatabaseHelper.query();
        tvName = (TextView) findViewById(R.id.tv_name);
        tvSinger = (TextView) findViewById(R.id.tv_singer);
        tvSound = (TextView) findViewById(R.id.tv_sound);
        tvNowTime = (TextView) findViewById(R.id.tv_now_time);
        tvAllTime = (TextView) findViewById(R.id.tv_all_time);
        imgStart = (ImageView) findViewById(R.id.img_start);
        imgPrev = (ImageView) findViewById(R.id.img_prev);
        imgNext = (ImageView) findViewById(R.id.img_next);
        imgPlayType = (ImageView) findViewById(R.id.img_play_type);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        imgStart.setOnClickListener(new MyOnClickListener());
        imgPrev.setOnClickListener(new MyOnClickListener());
        imgNext.setOnClickListener(new MyOnClickListener());
        imgPlayType.setOnClickListener(new MyOnClickListener());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicService.seekTo(seekBar.getProgress());
            }
        });
    }

    //初始化服务内的媒体类以及有关媒体类get到的信息设置
    private void playPerpare(){
        Music music = musicList.get(index);
        path = music.getPath();
        tvName.setText(music.getName());
        tvSinger.setText(music.getSinger());
        int sound = music.getSound();
        if(sound==0){
            tvSound.setText("  标准  ");
        }
        else if(sound==1){
            tvSound.setText("  HQ  ");
        }
        else {
            tvSound.setText("  SQ  ");
        }
        long albumID = -1;
        long songID = -1;
        Cursor cursor = getArtwork();
        if(cursor != null){
            songID = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
            albumID = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
        imgCover.setImageBitmap(getArtworkFromFile(songID,albumID));
        musicService.prepare(path);
        final int musicAllTime = musicService.getDuration();
        seekBar.setMax(musicAllTime);
        tvAllTime.setText(correctTime(musicAllTime/60000)+":"+correctTime(musicAllTime/1000%60));
        mTimer = new Timer();
        TimerTask mTimerTask = new TimerTask(){

            @Override
            public void run() {
                musicNowTime = musicService.getCurrentPosition();
                seekBar.setProgress(musicNowTime);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvNowTime.setText(correctTime(musicNowTime/60000)+":"+correctTime(musicNowTime/1000%60));
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask,0,10);
        musicStart();
    }

    //获取当前mp3文件的信息
    private Cursor getArtwork(){
        try {
            String nowpath = null;
            int i = 0;
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if(cursor.moveToFirst()) {
                do {
                    nowpath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    if(nowpath.equals(path)){
                        break;
                    }
                    i++;
                    if(i==cursor.getCount()){
                        return null;
                    }
                }while (cursor.moveToNext());
            }
            return cursor;
        }catch (Exception e){
            return null;
        }
    }

    //获取当前mp3的封面
    private Bitmap getArtworkFromFile(long songID,long albumID){
        Bitmap bm=null;
        try {
            if(songID<0&&albumID<0){
                bm = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.bg));
            }
            else {
                if (albumID < 0) {
                    Uri uri = Uri.parse("content://media/external/audio/media/" + songID + "/albumart");
                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    }
                } else {
                    Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumID);
                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfd != null) {
                        bm = BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(bm==null){
            bm = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.bg));
        }
        return bm;
    }

    //纠正显示的时间格式
    private String correctTime(int time){
        if(time/10==0){
            return "0"+time;
        }else {
            return time+"";
        }
    }

    //控件的单击事件
    class MyOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.img_start:
                    if(isStop){
                        musicStart();
                    }
                    else{
                        musicStop();
                    }
                    break;
                case R.id.img_prev:
                    prevMusic();
                    break;
                case R.id.img_next:
                    nextMusic();
                    break;
                case R.id.img_play_type:
                    if(type==2){
                        type = 0;
                    }
                    else {
                        type++;
                    }
                    switch (type){
                        case 0:
                            imgPlayType.setImageDrawable(getResources().getDrawable(R.drawable.xunhuan));
                            break;
                        case 1:
                            imgPlayType.setImageDrawable(getResources().getDrawable(R.drawable.danqu));
                            break;
                        case 2:
                            imgPlayType.setImageDrawable(getResources().getDrawable(R.drawable.suiji));
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
        }
    }

    //播放音乐
    private void musicStart(){
        imgStart.setImageDrawable(getResources().getDrawable(R.drawable.player_stop));
        musicService.start();
        imgCover.startRun();
        isStop=false;
    }

    //暂停音乐
    private void musicStop(){
        imgStart.setImageDrawable(getResources().getDrawable(R.drawable.player_start));
        musicService.pause();
        imgCover.stopRun();
        isStop=true;
    }

    //切换成上一首歌
    private void prevMusic(){
        mTimer.cancel();
        if(index == 0){
            index = musicList.size()-1;
        }
        else {
            index--;
        }
        playPerpare();
    }

    //切换成下一首歌
    private void nextMusic(){
        mTimer.cancel();
        if(index == musicList.size()-1){
            index = 0;
        }
        else {
            index++;
        }
        playPerpare();
    }

    //当前活动结束后的响应
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBound){
            musicService.stop();
            unbindService(mServiceConnection);
            mBound=false;
            isStop=true;
        }
        Toast.makeText(PlayActivity.this,"服务关闭",Toast.LENGTH_SHORT).show();
    }


    //问服务连接是否开了线程   与服务建立连接
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder musicBinder= (MusicService.MusicBinder) service;
            musicService = musicBinder.getService();
            mBound=true;
            playPerpare();
            //这个响应事件不能放initView()中的原因是musicService要在服务开启后才能调用
            //不放playPerpare()中是不用重复执行，所以才放这边的。
            musicService.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switch (type){
                        case 0:
                            nextMusic();
                            break;
                        case 1:
                            mTimer.cancel();
                            playPerpare();
                            break;
                        case 2:
                            mTimer.cancel();
                            index = random.nextInt(musicList.size());
                            playPerpare();
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
