package main.com.app.root.env.tree;
import main.com.app.root.DependencyValue;
import main.com.app.root.Spawner;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.ui.UIController;

import org.joml.Vector3f;

public class TreeController implements EnvInstance<TreeController> {
    @DependencyValue private Mesh mesh;
    @DependencyValue private Spawner spawner;
    @DependencyValue private UIController uiController;

    private TreeGenerator treeGenerator;

    @Override
    public TreeController getInstance() {
        return this;
    }

    public TreeGenerator getGenerator() {
        return treeGenerator;
    }

    public void createGenerator(
        TreeData treeData, 
        Vector3f position, 
        Mesh mesh, 
        Spawner spawner
    ) {
        this.treeGenerator = new TreeGenerator(
            treeData, 
            position, 
            mesh, 
            spawner
        );
        this.treeGenerator.setTreeController(this);
    }
    
    public void cleanup() {
        if(treeGenerator != null) {
            treeGenerator.cleanup();
        }
    }
}
