package main.com.app.root;

public class TimeCycle {
    /**
     * Time Period
     */
    public enum TimePeriod {
        DAWN(4, 6),
        MORNING(6, 12),
        AFTERNOON(12, 17),
        DUSK(17, 19),
        NIGHT(19, 4),
        MIDNIGHT(0, 0);

        public final int startHour;
        public final int endHour;
        
        TimePeriod(int startHour, int endHour) {
            this.startHour = startHour;
            this.endHour = endHour;
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

    public TimeCycle() {
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
} 
