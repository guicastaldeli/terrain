package main.com.app.root.env.tree;
import main.com.app.root.DependencyValue;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import org.joml.Vector3f;

public class TreeController implements EnvInstance<TreeController> {
    @DependencyValue private Mesh mesh;

    private TreeGenerator treeGenerator;

    @Override
    public TreeController getInstance() {
        return this;
    }

    public TreeGenerator getGenerator() {
        return treeGenerator;
    }

    public void createGenerator(TreeData treeData, Vector3f position, Mesh mesh) {
        this.treeGenerator = new TreeGenerator(treeData, position, mesh);
    }
    
    public void cleanup() {
        if(treeGenerator != null) {
            treeGenerator.cleanup();
        }
    }
}
