package main.com.app.root.mesh;

import org.joml.Vector3f;

public class InstanceData {
    public Vector3f position;
    public Vector3f rotation;
    public float scale;
        
    public InstanceData(
        Vector3f position, 
        Vector3f rotation, 
        float scale
    ) {
        this.position = new Vector3f(position);
        this.rotation = new Vector3f(rotation);
        this.scale = scale;
    }
}
