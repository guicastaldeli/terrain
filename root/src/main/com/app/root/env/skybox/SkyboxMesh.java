package main.com.app.root.env.skybox;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshData;
import main.com.app.root.mesh.MeshLoader;
import org.lwjgl.opengl.GL11;

public class SkyboxMesh {
    private final float[][][] timeColors = {
        //Night (0:00 - 4:00)
        {{0.0f, 0.0f, 0.1f, 1.0f}, {0.05f, 0.05f, 0.15f, 1.0f}},
        //Dawn (4:00 - 6:00)
        {{0.1f, 0.05f, 0.2f, 1.0f}, {0.2f, 0.1f, 0.3f, 1.0f}},
        //Morning (6:00 - 12:00)
        {{0.2f, 0.3f, 0.6f, 1.0f}, {0.4f, 0.6f, 0.9f, 1.0f}},
        //Afternoon (12:00 - 17:00)
        {{0.3f, 0.5f, 0.8f, 1.0f}, {0.5f, 0.7f, 1.0f, 1.0f}},
        //Dusk (17:00 - 19:00)
        {{0.6f, 0.3f, 0.2f, 1.0f}, {0.8f, 0.4f, 0.3f, 1.0f}},
        //Night (19:00 - 0:00)
        {{0.0f, 0.0f, 0.05f, 1.0f}, {0.1f, 0.1f, 0.2f, 1.0f}}
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
    private float[] getTimeBasedColor(float timePercent) {
        float hour = timePercent * 24.0f;

        int periodIndex;
        if (hour < 4.0f) {
            periodIndex = 0; //Night
        } else if (hour < 6.0f) {
            periodIndex = 1; //Dawn
        } else if (hour < 12.0f) {
            periodIndex = 2; //Morning
        } else if (hour < 17.0f) {
            periodIndex = 3; //Afternoon
        } else if (hour < 19.0f) {
            periodIndex = 4; //Dusk
        } else {
            periodIndex = 5; //Night
        }

        float[] startColor = timeColors[periodIndex][0];
        float[] endColor = timeColors[periodIndex][1];

        float periodStartHour = getPeriodStartHour(periodIndex);
        float periodEndHour = getPeriodEndHour(periodIndex);
        float periodProgress = 
            (hour - periodStartHour) / 
            (periodEndHour - periodStartHour);
        periodProgress = Math.max(0.0f, Math.min(1.0f, periodProgress));

        return interpolateColor(
            startColor, 
            endColor, 
            periodProgress
        );
    }

    private float getPeriodStartHour(int periodIndex) {
        switch(periodIndex) {
            case 0: return 0.0f;    // Night
            case 1: return 4.0f;    // Dawn
            case 2: return 6.0f;    // Morning
            case 3: return 12.0f;   // Afternoon
            case 4: return 17.0f;   // Dusk
            case 5: return 19.0f;   // Night
            default: return 0.0f;
        }
    }
    
    private float getPeriodEndHour(int periodIndex) {
        switch(periodIndex) {
            case 0: return 4.0f;    // Night
            case 1: return 6.0f;    // Dawn
            case 2: return 12.0f;   // Morning
            case 3: return 17.0f;   // Afternoon
            case 4: return 19.0f;   // Dusk
            case 5: return 24.0f;   // Night (wraps around)
            default: return 24.0f;
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

        data.setColorRgb(r, b, g, a);
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
