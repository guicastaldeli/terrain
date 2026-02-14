package main.com.app.root.lightning;
import java.util.*;

public class LightningController {
    private final Map<LightningData, List<Light>> activeLights;    
    private boolean enabled = true;

    public LightningController() {
        this.activeLights = new EnumMap<>(LightningData.class);
        for(LightningData type : LightningData.values()) {
            activeLights.put(type, new ArrayList<>());
        }
    }

    /**
     * Add
     */
    public void add(LightningData type, Light light) {
        if(light != null) activeLights.get(type).add(light);
    }

    /**
     * Remove
     */
    public void remove(LightningData type, Light light) {
        activeLights.get(type).remove(light);
    }

    /**
     * Clear
     */
    public void clear(LightningData type) {
        activeLights.get(type).clear();
    }

    public void clearAll() {
        for(LightningData type : LightningData.values()) {
            clear(type);
        }
    }

    /**
     * Get Lights
     */
    public List<Light> getLights(LightningData type) {
        return new ArrayList<>(activeLights.get(type));
    }

    /**
     * Get Light Count
     */
    public int getLightCount(LightningData type) {
        return activeLights.get(type).size();
    }

    /**
     * Enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Update
     */
    public void update() {
        for(List<Light> lights : activeLights.values()) {
            for(Light light : lights) {
                if(light instanceof DynamicLight) {
                    ((DynamicLight) light).update();
                }
            }
        }
    }
}
