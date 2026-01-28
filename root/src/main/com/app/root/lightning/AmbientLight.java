package main.com.app.root.lightning;
import org.joml.Vector3f;
import main.com.app.root.utils.HexToVec3;

public class AmbientLight implements Light {
    private Vector3f color;
    private float intensity;

    private static final Vector3f DEFAULT_COLOR = HexToVec3.hexToVec3("#2a2a2a");
    private static final float DEFAULT_INTENSITY = 1.0f;
    
    public AmbientLight(Vector3f color, float intensity) {
        this.color = new Vector3f(color);
        this.intensity = intensity;
    }
    public AmbientLight(String hexColor, float intensity) {
        this(HexToVec3.hexToVec3(hexColor), intensity);
    }
    
    public AmbientLight() {
        this(DEFAULT_COLOR, DEFAULT_INTENSITY);
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
        return LightningData.AMBIENT;
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
}