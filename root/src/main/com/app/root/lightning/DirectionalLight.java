package main.com.app.root.lightning;
import org.joml.Vector3f;
import main.com.app.root.utils.HexToVec3;

public class DirectionalLight implements Light {
    private Vector3f color;
    private float intensity;
    private Vector3f direction;
    
    private static final Vector3f DEFAULT_COLOR = HexToVec3.hexToVec3("#33ac2c");
    private static final float DEFAULT_INTENSITY = 1.0f;
    private static final Vector3f DEFAULT_DIRECTION = new Vector3f(0.0f, 450.0f, 150.0f);
    
    public DirectionalLight(
        Vector3f color, 
        float intensity, 
        Vector3f direction
    ) {
        this.color = new Vector3f(color);
        this.intensity = intensity;
        this.direction = new Vector3f(direction).normalize();
    }
    public DirectionalLight(
        String hexColor, 
        float intensity, 
        Vector3f direction
    ) {
        this(
            HexToVec3.hexToVec3(hexColor), 
            intensity, 
            direction
        );
    }
    public DirectionalLight() {
        this(
            DEFAULT_COLOR, 
            DEFAULT_INTENSITY, 
            DEFAULT_DIRECTION
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
    
    public void setColor(Vector3f color) {
        this.color.set(color);
    }
    
    // Add hex setter
    public void setColor(String hexColor) {
        this.color.set(HexToVec3.hexToVec3(hexColor));
    }
    
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
    
    public void setDirection(Vector3f direction) {
        this.direction.set(direction).normalize();
    }
}