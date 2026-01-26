package main.com.app.root.env.skybox;
import main.com.app.root.Tick;
import main.com.app.root.TimeCycle;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.utils.ColorConverter;

import org.lwjgl.opengl.GL11;

public class SkyboxMesh {
    private static final String[][] PERIOD_COLORS_HEX = {
        //Midnight
        { "#1a0010", "#0D0D26" },
        //Dawn
        { "#1A0D33", "#331A4D" },
        //Morning
        { "#334D99", "#6699E6" },
        //Afternoon
        {"#4D80CC", "#80B3FF" },
        //Dusk
        { "#994D33", "#CC664D" },
        //Night
        { "#0D0D1A", "#1A1A33" }
    };

    private static final String SKYBOX_ID = "skybox";
    private final Tick tick;
    private final Mesh mesh;
    private final ShaderProgram shaderProgram;

    public SkyboxMesh(Tick tick, Mesh mesh, ShaderProgram shaderProgram) {
        this.tick = tick;
        this.mesh = mesh;
        this.shaderProgram = shaderProgram;
        setMesh();
    }

    /**
     * Get Time Based Color
     */
    private float[][] getColorsForPeriod(TimeCycle.TimePeriod period) {
        int periodIndex = getPeriodIndex(period);
        String[] hexColors = PERIOD_COLORS_HEX[periodIndex];

        return new float[][] {
            ColorConverter.hexToFloat(hexColors[0]),
            ColorConverter.hexToFloat(hexColors[1])
        };
    }

    private int getPeriodIndex(TimeCycle.TimePeriod period) {
        switch(period) {
            case MIDNIGHT: return 0;
            case DAWN: return 1;
            case MORNING: return 2;
            case AFTERNOON: return 3;
            case DUSK: return 4;
            case NIGHT: return 5;
            default: return 5;
        }
    }

    private float[] getTimeBasedColor(float timePercent) {
        TimeCycle.TimePeriod currentPeriod = tick.getTimeCycle().getCurrentTimePeriod();
        float hour = timePercent * 24.0f;

        float[][] periodColors = getColorsForPeriod(currentPeriod);
        float[] startColor = periodColors[0];
        float[] endColor = periodColors[1];

        float progress = calcProgressInPeriod(currentPeriod, hour);
        TimeCycle.TimePeriod nextPeriod = getNextPeriod(currentPeriod);
        if(progress > 0.95 && nextPeriod != currentPeriod) {
            float[][] nextColors = getColorsForPeriod(nextPeriod);
            float transitionProgress = (progress - 0.95f) / 0.05f;
            endColor = interpolateColor(endColor, nextColors[0], transitionProgress);
        }
        return interpolateColor(startColor, endColor, progress);
    }

    private TimeCycle.TimePeriod getNextPeriod(TimeCycle.TimePeriod current) {
        switch(current) {
            case MIDNIGHT: return TimeCycle.TimePeriod.DAWN;
            case DAWN: return TimeCycle.TimePeriod.MORNING;
            case MORNING: return TimeCycle.TimePeriod.AFTERNOON;
            case AFTERNOON: return TimeCycle.TimePeriod.DUSK;
            case DUSK: return TimeCycle.TimePeriod.NIGHT;
            case NIGHT: return TimeCycle.TimePeriod.MIDNIGHT;
            default: return current;
        }
    }

    private float calcProgressInPeriod(TimeCycle.TimePeriod period, float hour) {
        int startHour = period.startHour;
        int endHour = period.endHour;

        if(startHour < endHour) {
            hour = Math.max(startHour, Math.min(hour, endHour - 0.00001f));
            return (hour - startHour) / (endHour - startHour);
        } else if(startHour > endHour) {
            if(hour >= startHour) {
                return (hour - startHour) / (24.0f - startHour);
            } else {
                return (hour + (24.0f - startHour)) / (endHour + (24.0f - startHour));
            }
        } else {
            return 0.5f;
        }
    }

    /**
     * Interpolate Color
     */
    private float[] interpolateColor(
        float[] c1,
        float[] c2,
        float progress
    ) {
        float[] res = new float[4];
        for(int i = 0; i < 4; i++) {
            res[i] = c1[i] + (c2[i] - c1[i]) * progress;
        }
        return res;
    }

    private float calcStarBrightness() {
        if(tick == null || tick.getTimeCycle() == null) {
            System.out.println("TimeCycle is null!");
            return 0.0f;
        }

        float timePercent = tick.getTimeCycle().getCurrentTime() / tick.getTimeCycle().DAY_DURATION;
        float hour = timePercent * 24.0f;
        if(hour >= 20.0f || hour < 4.0f) {
            return 1.0f;
        } else if(hour >= 4.0f && hour < 6.0f) {
            float progress = (hour - 4.0f) / 2.0f;
            return 1.0f - progress;
        } else if(hour >= 18.0f && hour < 20.0f) {
            float progress = (hour - 18.0f) / 2.0f;
            return progress;
        } else {
            return 0.0f;
        }
    }

    /**
     * Set Mesh
     */
    private void setMesh() {
        MeshData data = MeshLoader.load(MeshData.MeshType.SKYBOX, SKYBOX_ID);
        if(data == null) {
            System.err.println("Failed to load terrain mesh template");
            return;
        }
        data.setShaderType(2);
        mesh.add(SKYBOX_ID, data);
        if(data != null) updateSkyColor(data);
    }

    /**
     * Update
     */
    private void updateSkyColor(MeshData data) {
        if(tick == null || tick.getTimeCycle() == null) return;

        float currentTime = tick.getTimeCycle().getCurrentTime();
        float dayDuration = tick.getTimeCycle().DAY_DURATION;
        float timePercent = currentTime / dayDuration;
        
        float[] skyColor = getTimeBasedColor(timePercent);
        int r = (int)(skyColor[0] * 255);
        int g = (int)(skyColor[1] * 255);
        int b = (int)(skyColor[2] * 255);
        int a = (int)(skyColor[3] * 255);

        data.setColorRgb(r, g, b, a);
        if(tick.getTickCount() % 300 == 0) {
            /*
            System.out.println("Skybox color updated: " + 
                r + "," + g + "," + b + " at " + tick.getTimeCycle().getFormattedTime());
                */
        }
    }

    public void update() {
        if(tick == null || tick.getTimeCycle() == null) return;

        float currentTime = tick.getTimeCycle().getCurrentTime();
        float dayDuration = tick.getTimeCycle().DAY_DURATION;
        float timePercent = currentTime / dayDuration;

        float[] skyColor = getTimeBasedColor(timePercent);
        int r = (int)(skyColor[0] * 255);
        int g = (int)(skyColor[1] * 255);
        int b = (int)(skyColor[2] * 255);
        int a = (int)(skyColor[3] * 255);

        MeshData data = mesh.getData(SKYBOX_ID);
        if(data != null) {
            data.setColorRgb(r, g, b, a);
            float starBrightness = calcStarBrightness();
            data.setStarBrightness(starBrightness);
            mesh.updateColors(SKYBOX_ID, data.getColors());
        }
    }

    public void render() {
        try {
            GL11.glDepthMask(false);
            float starBrightness = calcStarBrightness();

            shaderProgram.setUniform("uStarBrightness", 1.0f);
            
            mesh.render(SKYBOX_ID, 2);
            GL11.glDepthMask(true);
        } catch(Exception err) {
            System.err.println("Skybox error!" + err.getMessage());
        }
    }
}
