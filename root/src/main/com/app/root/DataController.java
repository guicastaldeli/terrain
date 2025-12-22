package main.com.app.root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class DataController implements Serializable {
    private static final long serialVersionUID = 1L;

    /* World Data */
    private long worldSeed;
    private long worldTime;

    /* Player Data */
    private Vector3f playerPos;
    private Vector3f playerRotation;

    /* State */
    private List<String> items;

    /* Settings */
    private float masterVolume;
    private float musicVolume;
    private float sfxVolume;
    
    /* Stats */
    private int playTimeSecs;
    private int itemsCollected;

    public DataController() {
        /* World */
        worldSeed = System.currentTimeMillis();
        worldTime = 0;

        /* Player */
        playerPos = new Vector3f(0, 0, 0);
        playerRotation = new Vector3f(0, 0, 0);

        /* State */
        items = new ArrayList<>();

        /* Settings */
        masterVolume = 1.0f;
        musicVolume = 0.8f;
        sfxVolume = 0.8f;

        playTimeSecs = 0;
        itemsCollected = 0;
    }

    /* World */
    public void setWorldSeed(long seed) {
        this.worldSeed = seed;
    }
    public long getWorldSeed() {
        return worldSeed;
    }
    public void setWorldTime(long time) {
        this.worldTime = time;
    }
    public long getWorldTime() {
        return worldTime;
    }

    /* Player */
    public void setPlayerPos(Vector3f pos) {
        this.playerPos = pos;
    }
    public Vector3f getPlayerPos() {
        return playerPos;
    }

    public void setPlayerRotation(Vector3f rotation) {
        this.playerRotation = rotation;
    }
    public Vector3f getPlayerRotation() {
        return playerRotation;
    }

    /* Stats */
    public void addItem(String item) {
        if(!items.contains(item)) {
            items.add(item);
        }
    }
    public void removeItem(String item) {
        items.remove(item);
    }
    public List<String> getItems() {
        return new ArrayList<>(items);
    }

    /* Settings */
    public void setMusicVolume(float vol) {
        this.musicVolume = vol;
    }
    public float getMusicVol() {
        return musicVolume;
    }
    
    public void setSfxVolume(float vol) {
        this.sfxVolume = vol;
    }
    public float getSfxVolume() {
        return sfxVolume;
    }

    public void incrementPlayTime(int secs) {
        this.playTimeSecs += secs;
    }
    public int getPlayTimeSecs() {
        return playTimeSecs;
    }

    public String getFormattedPlayTime() {
        int hours = playTimeSecs / 3600;
        int minutes = (playTimeSecs % 3600) / 60;
        int secs = playTimeSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
