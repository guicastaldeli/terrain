package main.com.app.root;

public class Tick {
    public static Tick instance;

    private final Window window;
    private final TimeCycle timeCycle;

    private int TICKS_PER_SECOND = 50;
    private float TICK_RATE = 1.0f / TICKS_PER_SECOND;

    private float accumulatedTime = 0.0f;
    private int tickCount = 0;

    public float deltaTime = 0.0f;
    public long lastFrameTime = 0;

    public int frameCount = 0;
    public long lastFpsUpdateTime = 0;
    public int fps;

    private boolean timeUpdatedThisFrame = false;

    public Tick(Window window) {
        this.window = window;
        this.timeCycle = new TimeCycle();
        this.timeCycle.setTimeSpeed(0.5f);
        instance = this;
    }

    private void tick() {
        timeCycle.update(TICK_RATE * timeCycle.getTimeSpeed());
        /*
        if(tickCount % 100 == 0) {
            System.out.println("Time: " + timeCycle.getFormattedTime() + 
                             " (" + timeCycle.getCurrentTimePeriod() + ")");
        }
                             */
    }

    public int getTickCount() {
        return tickCount;
    }
    
    public float getTickRate() {
        return TICK_RATE;
    }

    public float getTickDelta() {
        return TICK_RATE;
    }

    public float getTickBasedSpeed(float speed) {
        return speed * TICK_RATE;
    }
    
    public void setTicksPerSecond(int ticks) {
        TICKS_PER_SECOND = ticks;
        TICK_RATE = 1.0f / TICKS_PER_SECOND;
    }

    public int getTicksPerSecond() {
        return TICKS_PER_SECOND;
    }

    public int getFps() {
        return fps;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public static float getIDeltaTime() {
        if(instance != null) return instance.deltaTime;
        return 0.016f;
    }

    public TimeCycle getTimeCycle() {
        return timeCycle;
    }

    public void resetTiming() {
        lastFrameTime = System.nanoTime();
        lastFpsUpdateTime = System.currentTimeMillis();
        frameCount = 0;
        fps = 0;
        accumulatedTime = 0.0f;
        timeUpdatedThisFrame = false;
    }

    public void updateTime() {
        if(timeUpdatedThisFrame) return;

        long currentTime = System.nanoTime();
        if(lastFrameTime == 0) {
            lastFrameTime = currentTime;
            lastFpsUpdateTime = System.currentTimeMillis();
            timeUpdatedThisFrame = true;
            return;
        }

        float secs = 1_000_000_000.0f;
        deltaTime = (currentTime - lastFrameTime) / secs;

        lastFrameTime = currentTime;
        timeUpdatedThisFrame = true;

        long currentWallTime = System.currentTimeMillis();
        frameCount++;
        if(currentWallTime - lastFpsUpdateTime >= 1000) {
            fps = frameCount;
            frameCount = 0;
            lastFpsUpdateTime = currentWallTime;
        }
    }

    public void update() {
        updateTime();

        accumulatedTime += deltaTime;
        while(accumulatedTime >= TICK_RATE) {
            tick();
            accumulatedTime -= TICK_RATE;
            tickCount++;
        }

        timeUpdatedThisFrame = false;
    }
}
