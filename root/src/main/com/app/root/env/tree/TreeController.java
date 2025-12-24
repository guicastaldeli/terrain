package main.com.app.root.env.tree;
import main.com.app.root.DependencyValue;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;

public class TreeController implements EnvInstance<TreeController> {
    @DependencyValue private Mesh mesh;

    private TreeGenerator treeGenerator;

    @Override
    public TreeController getInstance() {
        this.treeGenerator = new TreeGenerator();
        return this;
    }

    public TreeGenerator getGenerator() {
        return treeGenerator;
    }
}
