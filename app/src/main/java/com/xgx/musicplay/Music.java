package com.xgx.musicplay;

import android.os.Parcel;
import android.os.Parcelable;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by xgx on 2018/12/8 for MusicPlayDemo
 */

@Entity(indexes = {
        @Index(value = "scantime DESC", unique = true)
})
public class Music implements Parcelable, Serializable {
    private static final long serialVersionUID = -6894540417204020204L;
    @Id
    private long id;
    @Property
    private String name;
    @Property
    private String singer;
    @Property
    private String album;
    @Property
    private String musicDir;
    @Property
    private String path;
    @Property
    private int sound;
    @Property
    private String playtime;
    @Property
    private String scantime;
    @Property
    private boolean isPrize;
    @Property
    private String allTimeStr;

    public Music() {
    }


    protected Music(Parcel in) {
        id = in.readLong();
        name = in.readString();
        singer = in.readString();
        album = in.readString();
        musicDir = in.readString();
        path = in.readString();
        sound = in.readInt();
        playtime = in.readString();
        scantime = in.readString();
        isPrize = in.readByte() != 0;
        allTimeStr = in.readString();
    }


    @Generated(hash = 886408151)
    public Music(long id, String name, String singer, String album, String musicDir,
            String path, int sound, String playtime, String scantime,
            boolean isPrize, String allTimeStr) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.album = album;
        this.musicDir = musicDir;
        this.path = path;
        this.sound = sound;
        this.playtime = playtime;
        this.scantime = scantime;
        this.isPrize = isPrize;
        this.allTimeStr = allTimeStr;
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getMusicDir() {
        return musicDir;
    }

    public void setMusicDir(String musicDir) {
        this.musicDir = musicDir;
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

    public String getPlaytime() {
        return playtime;
    }

    public void setPlaytime(String playtime) {
        this.playtime = playtime;
    }

    public String getScantime() {
        return scantime;
    }

    public void setScantime(String scantime) {
        this.scantime = scantime;
    }

    public boolean isPrize() {
        return isPrize;
    }

    public void setPrize(boolean prize) {
        isPrize = prize;
    }

    public String getAllTimeStr() {
        return allTimeStr;
    }

    public void setAllTimeStr(String allTimeStr) {
        this.allTimeStr = allTimeStr;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(singer);
        dest.writeString(album);
        dest.writeString(musicDir);
        dest.writeString(path);
        dest.writeInt(sound);
        dest.writeString(playtime);
        dest.writeString(scantime);
        dest.writeByte((byte) (isPrize ? 1 : 0));
        dest.writeString(allTimeStr);
    }


    public boolean getIsPrize() {
        return this.isPrize;
    }


    public void setIsPrize(boolean isPrize) {
        this.isPrize = isPrize;
    }
}