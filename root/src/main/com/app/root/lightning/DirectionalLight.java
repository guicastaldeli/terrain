package main.com.app.root.lightning;
import org.joml.Vector3f;
import main.com.app.root.utils.HexToVec3;

public class DirectionalLight implements Light {
    public Vector3f color;
    public float intensity;
    public Vector3f direction;
    public float range;
    
    public static final Vector3f DEFAULT_COLOR = HexToVec3.hexToVec3("#00ff51");
    public static final float DEFAULT_INTENSITY = 10.0f;
    public static final Vector3f DEFAULT_DIRECTION = new Vector3f(10.0f, 80.0f, 0.0f);
    public static final float DEFAULT_RANGE = 100.0f;
    
    public DirectionalLight(
        Vector3f color, 
        float intensity, 
        Vector3f direction,
        float range
    ) {
        this.color = new Vector3f(color);
        this.intensity = intensity;
        this.direction = new Vector3f(direction).normalize();
        this.range = range;
    }
    
    public DirectionalLight(
        String hexColor, 
        float intensity, 
        Vector3f direction,
        float range
    ) {
        this(
            HexToVec3.hexToVec3(hexColor), 
            intensity, 
            direction,
            range
        );
    }
    
    public DirectionalLight() {
        this(
            DEFAULT_COLOR, 
            DEFAULT_INTENSITY, 
            DEFAULT_DIRECTION,
            DEFAULT_RANGE
        );
    }
    
    @Override
    public Vector3f getColor() {
        return new Vector3f(color);
    }
    
    @Override
    public float getIntensity() {
        return intensity;
    }
    
    @Override
    public LightningData getType() {
        return LightningData.DIRECTIONAL;
    }
    
    public Vector3f getDirection() {
        return new Vector3f(direction);
    }
    
    public float getRange() {
        return range;
    }
    
    public void setColor(Vector3f color) {
        this.color.set(color);
    }
    
    public void setColor(String hexColor) {
        this.color.set(HexToVec3.hexToVec3(hexColor));
    }
    
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
    
    public void setDirection(Vector3f direction) {
        this.direction.set(direction).normalize();
    }
    
    public void setRange(float range) {
        this.range = range;
    }
}