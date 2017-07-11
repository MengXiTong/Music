package com.example.yujian.music;

/**
 * Created by yujian on 2017/5/23.
 */

public class Music {
    private int id;
    private String name;
    private String singer;
    private String album;
    private String path;
    private int sound;

    public Music(){

    }

    public Music(int id, String name, String singer, String album, String path, int sound) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.album = album;
        this.path = path;
        this.sound = sound;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSound() {
        return sound;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }
}
