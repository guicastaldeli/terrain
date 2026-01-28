package main.com.app.root.lightning;
import org.joml.Vector3f;

public interface Light {
    Vector3f getColor();
    float getIntensity();
    LightningData getType();
}

interface DynamicLight {
    void update();
}
