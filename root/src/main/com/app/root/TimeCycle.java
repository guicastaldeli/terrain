package main.com.app.root;
import main.com.app.root._resources.AudioLoader;

public class TimeCycle {
    /**
     * Time Period
     */
    public enum TimePeriod {
        MIDNIGHT(0, 4, "env/night.wav"),
        DAWN(4, 6, "env/dawn.wav"),
        MORNING(6, 12, "env/morning.wav"),
        AFTERNOON(12, 17, "env/afternoon.wav"),
        DUSK(17, 19, "env/dusk.wav"),
        NIGHT(19, 24, "env/night.wav");

        public final int startHour;
        public final int endHour;
        public final String soundFile;
        
        TimePeriod(
            int startHour, 
            int endHour,
            String soundFile
        ) {
            this.startHour = startHour;
            this.endHour = endHour;
            this.soundFile = soundFile;
        }

        public boolean isActive(float hour) {
            if(startHour < endHour) {
                return hour >= startHour && hour < endHour;
            } else {
                return hour >= startHour || hour < endHour;
            }
        }
    }

    public final float DAY_DURATION = 120.0f;
    public final float HOUR_DURATION = DAY_DURATION / 24.0f;

    private float currentTime = 6.0f * HOUR_DURATION;
    private float timeSpeed = 60.0f;
    private float timeDayPercentage = 0.25f;

    private EnvSounds envSounds;

    public TimeCycle() {
        this.envSounds = new EnvSounds(this);
        envSounds.currentPeriod = getCurrentTimePeriod();
        setTime(7, 0);
        updateTime();
    }

    /**
     * Update
     */
    public void update(float deltaTime) {
        currentTime += deltaTime * timeSpeed;
        if(currentTime >= DAY_DURATION) {
            currentTime -= DAY_DURATION;
        } else if(currentTime < 0) {
            currentTime += DAY_DURATION;
        }
        updateTime();
        envSounds.update();
    }

    private void updateTime() {
        timeDayPercentage = currentTime / DAY_DURATION;
    }

    /**
     * Get Hour
     */
    public int getHour() {
        return (int)((currentTime / DAY_DURATION) * 24.0f) % 24;
    }

    /**
     * Get Minute
     */
    public int getMinute() {
        float hourFraction = (currentTime / DAY_DURATION) * 24.0f;
        float minuteFraction = hourFraction - (int)hourFraction;
        return (int)(minuteFraction * 60.0f);
    }

    /**
     * Formatted Time
     */
    public String getFormattedTime() {
        return String.format("%02d:%02d", getHour(), getMinute());
    }

    /**
     * Get Current Time
     */
    public TimePeriod getCurrentTimePeriod() {
        float hour = (currentTime / DAY_DURATION) * 24.0f;
        for(TimePeriod period : TimePeriod.values()) {
            if(period.isActive(hour)) {
                return period;
            }
        }
        return TimePeriod.MIDNIGHT;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    /**
     * Get Time of Day Percentage
     */
    public float getTimeOfDayPercentage() {
        return timeDayPercentage;
    }

    /**
     * Set Time
     */
    public void setTime(int hour, int min) {
        float totalHours = hour + (min / 60.0f);
        currentTime = (totalHours / 24.0f) * DAY_DURATION;
        updateTime();

        TimePeriod newPeriod = getCurrentTimePeriod();
        if(newPeriod != envSounds.currentPeriod) {
            envSounds.stop();
            envSounds.currentPeriod = newPeriod;
            envSounds.play(envSounds.currentPeriod, EnvSounds.VOLUME);
        }
    }

    public void setTimeSpeed(float speed) {
        this.timeSpeed = Math.max(0.0f, speed);
    }

    /**
     * Get Time Speed
     */
    public float getTimeSpeed() {
        return timeSpeed;
    }

    /**
     * Set Pause
     */
    public void setPause(boolean paused) {
        if(paused) {
            timeSpeed = 0.0f;
        } else {
            timeSpeed = 60.0f;
        }
    }

    public void playSound(StateController stateController) {
        if(stateController != null && !stateController.isInMenu()) {
            envSounds.play(envSounds.currentPeriod, EnvSounds.VOLUME);
        }
    }

    public void stopSounds() {
        if(envSounds != null) {
            envSounds.stop();
        }
    }

    /**
     * 
     * Env Sounds
     * 
     */
    private class EnvSounds {
        private TimeCycle timeCycle;

        public TimePeriod currentPeriod;
        public TimePeriod nextPeriod;

        public boolean isTransitioning = false;
        public float transitionProgress = 0.0f;
        public static final float TRANSITION_DURATION = 5.0f;
        public static final float VOLUME = 0.3f;

        public EnvSounds(TimeCycle timeCycle) {
            this.timeCycle = timeCycle;
        }

        /**
         * 
         * Start Transition
         * 
         */
        public void startTransition(TimePeriod newPeriod) {
            boolean sameSoundFile = 
                currentPeriod != null && 
                currentPeriod.soundFile.equals(newPeriod.soundFile);

            if(!sameSoundFile) {
                isTransitioning = true;
                transitionProgress = 0.0f;
                nextPeriod = newPeriod;
                play(nextPeriod, 0.0f);
            } else {
                currentPeriod = newPeriod;
                isTransitioning = false;
                AudioLoader.getInstance().setVolume(currentPeriod.soundFile, VOLUME);
            }
        }

        /**
         * 
         * Play
         * 
         */
        public void play(TimePeriod period, float volume) {
            String soundFile = period.soundFile;

            if(currentPeriod == null || !currentPeriod.soundFile.equals(soundFile)) {
                AudioLoader.getInstance().stop(soundFile);
            }
            AudioLoader.getInstance().play(soundFile, volume, true);
        }

        /**
         * 
         * Stop
         * 
         */
        public void stop() {
            for(TimePeriod period : TimePeriod.values()) {
                AudioLoader.getInstance().stop(period.soundFile);
            }
            isTransitioning = false;
        }

        /**
         * Complete Transition
         */
        public void completeTransition() {
            if(currentPeriod != null && nextPeriod != null && 
                !currentPeriod.soundFile.equals(nextPeriod.soundFile)
            ) {
                AudioLoader.getInstance().stop(currentPeriod.soundFile);
            }
            
            AudioLoader.getInstance().setVolume(nextPeriod.soundFile, VOLUME);
            
            currentPeriod = nextPeriod;
            nextPeriod = null;
            isTransitioning = false;
            transitionProgress = 0.0f;
        }

        /**
         * 
         * Update
         * 
         */
        public void update() {
            TimePeriod newPeriod = timeCycle.getCurrentTimePeriod();
            
            if(newPeriod != currentPeriod) {
                boolean sameSoundFile = 
                    currentPeriod != null && 
                    currentPeriod.soundFile.equals(newPeriod.soundFile);
                if(!sameSoundFile) {
                    if(!isTransitioning) {
                        startTransition(newPeriod);
                    }
                } else {
                    currentPeriod = newPeriod;
                    if(isTransitioning) {
                        isTransitioning = false;
                        transitionProgress = 0.0f;
                        nextPeriod = null;
                    }
                }
            }

            if(isTransitioning) {
                transitionProgress += Tick.getIDeltaTime();
                if(transitionProgress >= TRANSITION_DURATION) {
                    completeTransition();
                } else {
                    float progress = transitionProgress / TRANSITION_DURATION;
                    updateTransitionVolumes(progress);
                }
            }
        }

        public void updateTransitionVolumes(float progress) {
            float currentVolume = VOLUME * (1.0f - progress);
            AudioLoader.getInstance().setVolume(currentPeriod.soundFile, currentVolume);

            float nextVolume = VOLUME * progress;
            AudioLoader.getInstance().setVolume(nextPeriod.soundFile, nextVolume);
        }
    }
} 
