package main.com.app.root.mesh;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class MeshInstance {
    public boolean isInstanced = false;
    public List<InstanceData> instances = new ArrayList<>();

    public void setInstanced(boolean instanced) {
        this.isInstanced = instanced;
    }

    public boolean isInstanced() {
        return isInstanced;
    }

    public void addInstance(
        Vector3f position,
        Vector3f rotation,
        float scale
    ) {
        instances.add(new InstanceData(position, rotation, scale));
    }

    public void updateInstance(int i, Vector3f position) {
        if(i >= 0 && i < instances.size()) {
            instances.get(i).position.set(position);
        }
    } 

    public void removeInstance(int i) {
        if(i >= 0 && i < instances.size()) {
            instances.remove(i);
        }
    }

    public void clearInstances() {
        instances.clear();
    }

    public List<InstanceData> getInstances() {
        return instances;
    }

    public int getInstanceCount() {
        return instances.size();
    }
}
