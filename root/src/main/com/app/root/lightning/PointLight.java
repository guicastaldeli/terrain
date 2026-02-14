package main.com.app.root.lightning;
import org.joml.Vector3f;
import main.com.app.root.utils.HexToVec3;

public class PointLight implements Light, DynamicLight {
    public Vector3f color;
    public float intensity;
    public Vector3f position;
    public float radius;
    public float attenuation;
    
    public static final Vector3f DEFAULT_COLOR = HexToVec3.hexToVec3("#d40bcd");
    public static final float DEFAULT_INTENSITY = 10.0f;
    public static final Vector3f DEFAULT_POSITION = new Vector3f(0.0f, 90.0f, 0.0f);
    public static final float DEFAULT_RADIUS = 200.0f;

    public PointLight(
        Vector3f color,
        float intensity,
        Vector3f position,
        float radius
    ) {
        this.color = new Vector3f(color);
        this.intensity = intensity;
        this.position = new Vector3f(position);
        this.radius = radius;
        this.attenuation = 0.001f;
    }
    public PointLight(
        String hexColor,
        float intensity,
        Vector3f position,
        float radius
    ) {
        this(
            HexToVec3.hexToVec3(hexColor), 
            intensity, 
            position, 
            radius
        );
    }
    public PointLight() {
        this(
            DEFAULT_COLOR, 
            DEFAULT_INTENSITY, 
            DEFAULT_POSITION, 
            DEFAULT_RADIUS
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
        return LightningData.POINT;
    }
    
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    public float getRadius() {
        return radius;
    }
    
    public float getAttenuation() {
        return attenuation;
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
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public void setAttenuation(float attenuation) {
        this.attenuation = attenuation;
    }
    
    @Override
    public void update() {}
}