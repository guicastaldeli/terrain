package main.com.app.root.env.axe;
import main.com.app.root.DependencyValue;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshLoader;

public class AxeController implements EnvInstance<AxeController> {
    @DependencyValue private Mesh mesh;

    private final String AXE_ID = "AXE"; 
    private AxeData axeData;

    public AxeController(Mesh mesh) {
        this.mesh = mesh;

    }

    @Override
    public AxeController getInstance() {
        return this;
    }

    private void createMesh() {
        try {
            MeshLoader.loadModel(AXE_ID, axeData.getCurrentModel());
            mesh.addModel(AXE_ID, axeData.getCurrentModel());
        } catch(Exception e) {
            System.err.println("Failed to load axe model: " + axeData.getCurrentModel());
        }
    }

    public int calcDamage() {
        return (int) axeData.damage;
    }

    public int calcWoodDrop(int baseWood) {
        return (int) (baseWood * axeData.woodMultiplier);
    }

    public float getSwingSpeed() {
        return axeData.speed;
    }

    public boolean canBreakTree(int treeLevel) {
        return axeData.level >= treeLevel;
    }

    /**
     * Upgrade
     */
    public void upgrade() {
        if(axeData.level < 10) {
            axeData.level++;
            axeData.loadConfigData();
        }
    }

    private void updateMesh() {
        //Remove old mesh too
        createMesh();
    }
}
