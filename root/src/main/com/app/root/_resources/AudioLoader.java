package main.com.app.root._resources;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class AudioLoader {
    public static final String SOUND_PATH = "root/src/main/com/app/root/_resources/sound/";

    private static AudioLoader instance;
    private Map<String, Clip> soundClips;
    private Map<String, AudioInputStream> soundStreams;
    private float globalVolume = 1.0f;
    private boolean muted = false;

    public AudioLoader() {
        this.soundClips = new HashMap<>();
        this.soundStreams = new HashMap<>();
    }

    public static AudioLoader getInstance() {
        if(instance == null) instance = new AudioLoader();
        return instance;
    }

    public void setGlobalVolume(float vol) {
        this.globalVolume = Math.max(0.0f, Math.min(1.0f, vol));
        for(Clip clip : soundClips.values()) {
            if(clip.isRunning()) {
                setClipVolume(clip, globalVolume);
            }
        }
    }

    public void setClipVolume(Clip clip, float volume) {
        try {
            if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                float dB = (float) (Math.log(volume == 0.0 ? 0.0001 : volume) / Math.log(10.0) * 20.0);
                dB = Math.max(min, Math.min(max, dB));

                gainControl.setValue(dB);
            }
        } catch(Exception err) {
            System.err.println("Error setting volume: " + err.getMessage());
        } 
    }

    /**
     * Volume
     */
    public void setVolume(String fileName, float volume) {
        if(soundClips.containsKey(fileName)) {
            Clip clip = soundClips.get(fileName);
            setClipVolume(clip, volume * globalVolume);
        }
    }

    public float getVolume(String fileName) {
        if(soundClips.containsKey(fileName)) {
            Clip clip = soundClips.get(fileName);
            try {
                if(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = gainControl.getValue();
                    return (float) Math.pow(10.0, dB / 20.0);
                }
            } catch(Exception err) {
                System.err.println("Error getting volume: " + err.getMessage());
            }
        }
        return 0.0f;
    }

    /**
     * 
     * Load
     * 
     */
    public boolean load(String fileName) {
        if(soundClips.containsKey(fileName)) {
            return true;
        }

        try {
            String filePath = AudioLoader.SOUND_PATH + fileName;
            File audioFile = new File(filePath);
            if(!audioFile.exists()) {
                System.err.println("Audio file not found: " + filePath);
                return false;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat sourceFormat = audioInputStream.getFormat();
            
            DataLine.Info info = new DataLine.Info(Clip.class, sourceFormat);
            if(!AudioSystem.isLineSupported(info)) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100,
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    44100,
                    false
                );
                
                audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                System.out.println("Converted audio format from " + sourceFormat + " to " + targetFormat);
            }

            AudioFormat format = audioInputStream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);

            clip.open(audioInputStream);
            soundClips.put(fileName, clip);
            soundStreams.put(fileName, audioInputStream);

            System.out.println("Loaded sound: " + fileName);
            return true;
        } catch(UnsupportedAudioFileException | IOException | LineUnavailableException err) {
            System.err.println("Error loading sound " + fileName + ": " + err.getMessage());
            return false;
        }
    }

    public boolean isLoaded(String fileName) {
        return soundClips.containsKey(fileName);
    }

    public void unload(String fileName) {
        if(soundClips.containsKey(fileName)) {
            Clip clip = soundClips.remove(fileName);
            clip.close();
        }
        if(soundStreams.containsKey(fileName)) {
            try {
                soundStreams.remove(fileName).close();
            } catch(IOException err) {
                System.err.println("Error unloading sound stream: " + err.getMessage());
            }
        }
    }

    /**
     * 
     * Play
     * 
     */
    public void play(String fileName) {
        play(fileName, 1.0f, false);
    }

    public void play(String fileName, float volume, boolean loop) {
        if(muted) return;

        if(!soundClips.containsKey(fileName)) {
            if(!load(fileName)) return;
        }

        try {
            Clip clip = soundClips.get(fileName);
            if(clip.isRunning()) clip.stop();

            clip.setFramePosition(0);

            setClipVolume(clip, volume * globalVolume);

            if(loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.loop(0);
            }

            clip.start();
        } catch (Exception err) {
            System.err.println("Error playing sound " + fileName + ": " + err.getMessage());
        }
    }

    public void play(String fileName, float volume) {
        if(muted) return;

        if(!soundClips.containsKey(fileName)) {
            if(!load(fileName)) return;
        }

        try {
            Clip clip = soundClips.get(fileName);
            if(clip.isRunning()) clip.stop();

            clip.setFramePosition(0);

            setClipVolume(clip, volume * globalVolume);

            clip.start();
        } catch (Exception err) {
            System.err.println("Error playing sound " + fileName + ": " + err.getMessage());
        }
    }

    public void play(String fileName, float volume, long durationMs) {
        if(muted) return;

        if(!soundClips.containsKey(fileName)) {
            if(!load(fileName)) return;
        }

        try {
            Clip clip = soundClips.get(fileName);
            if(clip.isRunning()) clip.stop();

            clip.setFramePosition(0);
            setClipVolume(clip, volume * globalVolume);
            clip.start();

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(clip.isRunning()) {
                        clip.stop();
                    }
                }
            }, durationMs);
        } catch (Exception err) {
            System.err.println("Error playing sound " + fileName + ": " + err.getMessage());
        }
    }

    /**
     * 
     * Stop
     * 
     */
    public void stop(String fileName) {
        if(soundClips.containsKey(fileName)) {
            Clip clip = soundClips.get(fileName);
            if(clip.isRunning()) {
                clip.stop();
            }
        }
    }

    public void stopAll() {
        for(Clip clip : soundClips.values()) {
            if(clip.isRunning()) {
                clip.stop();
            }
        }
    }

    /**
     * 
     * Resume
     * 
     */
    public void resume(String fileName) {
        if(muted) return;
        
        if(soundClips.containsKey(fileName)) {
            Clip clip = soundClips.get(fileName);
            if(!clip.isRunning()) {
                clip.start();
            }
        }
    }

    public void resumeAll() {
        if(muted) return;
        
        for(Clip clip : soundClips.values()) {
            if(!clip.isRunning() && 
                clip.getFramePosition() > 0
            ) {
                clip.start();
            }
        }
    }

    /**
     * 
     * Muted
     * 
     */
    public void setMuted(boolean muted) {
        this.muted = muted;
        if(muted) stopAll();
    }

    public boolean isMuted() {
        return muted;
    }

    public void cleanup() {
        stopAll();
        
        for(Clip clip : soundClips.values()) {
            clip.close();
        }
        for(AudioInputStream stream : soundStreams.values()) {
            try {
                stream.close();
            } catch(Exception err) {
                System.err.println("Error closing audio stream: " + err.getMessage());
            }
        }

        soundClips.clear();
        soundStreams.clear();
    }
}
