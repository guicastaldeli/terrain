package main.com.app.root.utils;
import main.com.app.root.Tick;
import main.com.app.root.TimeCycle;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.lightning.DirectionalLight;
import main.com.app.root.lightning.Light;
import main.com.app.root.lightning.LightningController;
import main.com.app.root.lightning.LightningData;
import java.util.List;
import org.joml.Vector3f;

public class SceneLight {
    private final Tick tick;
    private final EnvController envController;
    private final LightningController lightningController;
    
    public SceneLight(
        Tick tick, 
        LightningController lightningController,
        EnvController envController
    ) {
        this.tick = tick;
        this.lightningController = lightningController;
        this.envController = envController;
    }

    /**
     * Update Colors
     */
    public void updateColors() {
        if(envController == null || tick == null || tick.getTimeCycle() == null) return;
        
        try {
            TimeCycle.TimePeriod period = tick.getTimeCycle().getCurrentTimePeriod();
            
            Vector3f topLightColor;
            Vector3f bottomLightColor;
            float topIntensity;
            float bottomIntensity;
            
            switch(period) {
                case DAWN:
                    topLightColor = HexToVec3.hexToVec3("#5f5fb0");
                    bottomLightColor = HexToVec3.hexToVec3("#4A4A8C");
                    break;
                case MORNING:
                    topLightColor = HexToVec3.hexToVec3("#e4eaf0");
                    bottomLightColor = HexToVec3.hexToVec3("#d3dce5");
                    break;
                case AFTERNOON:
                    topLightColor = HexToVec3.hexToVec3("#d3dde7");
                    bottomLightColor = HexToVec3.hexToVec3("#b5c3d2");
                    break;
                case DUSK: 
                    topLightColor = HexToVec3.hexToVec3("#CC664D");
                    bottomLightColor = HexToVec3.hexToVec3("#994D33");
                    break;
                case NIGHT:
                    topLightColor = HexToVec3.hexToVec3("#242446");
                    bottomLightColor = HexToVec3.hexToVec3("#0D0D1A");
                    break;
                case MIDNIGHT:
                    topLightColor = HexToVec3.hexToVec3("#0F0F2E");
                    bottomLightColor = HexToVec3.hexToVec3("#0A0A1F");
                    break;
                default:
                    topLightColor = new Vector3f(1.0f, 1.0f, 1.0f);
                    bottomLightColor = new Vector3f(0.5f, 0.5f, 0.5f);
            }
            
            List<Light> directionalLights = lightningController.getLights(LightningData.DIRECTIONAL);
            
            if(directionalLights.size() >= 2) {
                DirectionalLight topLight = (DirectionalLight) directionalLights.get(0);
                DirectionalLight bottomLight = (DirectionalLight) directionalLights.get(1);
                topLight.setColor(topLightColor);
                bottomLight.setColor(bottomLightColor);
            }
        } catch(Exception e) {
            System.err.println("Failed to update light colors: " + e.getMessage());
        }
    }

    /**
     *
     * Set
     * 
     */
    public void set() {
        float posX = 50.0f;
        float posY = 1000.0f;
        float posZ = 50.0f;

        DirectionalLight directionalLightTop = new DirectionalLight(
            HexToVec3.hexToVec3("#ffffff"),
            1.0f,
            new Vector3f(posX, -posY, posZ),
            WorldGenerator.WORLD_SIZE
        );
        DirectionalLight directionalLightBottom = new DirectionalLight(
            HexToVec3.hexToVec3("#ffffff"),
            0.4f,
            new Vector3f(posX, posY, posZ),
            WorldGenerator.WORLD_SIZE
        );
        lightningController.add(LightningData.DIRECTIONAL, directionalLightTop);
        lightningController.add(LightningData.DIRECTIONAL, directionalLightBottom);
    }
}
