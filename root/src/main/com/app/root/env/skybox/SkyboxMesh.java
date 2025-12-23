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
        {"#00001A", "#0D0D26"},
        //Dawn
        {"#1A0D33", "#331A4D"},
        //Morning
        {"#334D99", "#6699E6"},
        //Afternoon
        {"#4D80CC", "#80B3FF"},
        //Dusk
        {"#994D33", "#CC664D"},
        //Night
        {"#0D0D1A", "#1A1A33"}
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
        return interpolateColor(
            startColor, 
            endColor, 
            progress
        );
    }

    private float calcProgressInPeriod(TimeCycle.TimePeriod period, float hour) {
        int startHour = period.startHour;
        int endHour = period.endHour;
        if(!period.isActive(hour)) return 0.0f;

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

    /**
     * Set Mesh
     */
    private void setMesh() {
        MeshData data = MeshLoader.load(MeshData.MeshType.SKYBOX, SKYBOX_ID);
        if(data == null) {
            System.err.println("Failed to load terrain mesh template");
            return;
        }
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
        if (tick.getTickCount() % 300 == 0) {
            System.out.println("Skybox color updated: " + 
                r + "," + g + "," + b + " at " + tick.getTimeCycle().getFormattedTime());
        }
    }
    private void updateSkyColor() {
        if(tick == null || tick.getTimeCycle() == null) return;

        float currentTime = tick.getTimeCycle().getCurrentTime();
        float dayDuration = tick.getTimeCycle().DAY_DURATION;
        float timePercent = currentTime / dayDuration;
        
        float[] skyColor = getTimeBasedColor(timePercent);
        int r = (int)(skyColor[0] * 255);
        int g = (int)(skyColor[1] * 255);
        int b = (int)(skyColor[2] * 255);
        int a = (int)(skyColor[3] * 255);
    }

    public void update() {
        updateSkyColor();
    }

    public void render() {
        try {
            GL11.glDepthMask(false);
            shaderProgram.setUniform("shaderType", 2);
            mesh.render(SKYBOX_ID);
            GL11.glDepthMask(true);
        } catch(Exception err) {
            System.err.println("Skybox error!" + err.getMessage());
        }
    }
}
