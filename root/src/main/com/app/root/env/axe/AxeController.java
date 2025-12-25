package main.com.app.root.env.axe;
import main.com.app.root.DependencyValue;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import org.joml.Vector3f;

public class AxeController implements EnvInstance<AxeController> {
    @DependencyValue private Mesh mesh;

    private final String AXE_ID = "AXE"; 
    private static final String TEX_PATH = "root/src/main/com/app/root/_resources/texture/item/";
    private AxeData axeData;

    @Override
    public AxeController getInstance() {
        this.axeData = new AxeData("axe0", 0, 10.0f, 1.0f, 1, 100);
        this.axeData.createDefaultConfigs();
        createMesh();
        return this;
    }

    /**
     * Load Texure
     */
    private void loadTex(String name) {
        int id = TextureLoader.load(TEX_PATH + name + ".png");
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
        mesh.setTex(AXE_ID, id);
    }

    private void createMesh() {
        try {
            String axeName = "axe" + axeData.getLevel();
            mesh.addModel(AXE_ID, axeName);
            loadTex(axeName);
        } catch(Exception err) {
            System.err.println("Failed to load axe model: " + axeData + ": " + err.getMessage());
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

    public int getLevel() {
        return axeData.getLevel();
    }
    
    public int getUpgradeCost() {
        return axeData.upgradeCost;
    }
    
    public float getDamage() {
        return axeData.getDamage();
    }
    
    public float getSpeed() {
        return axeData.speed;
    }
    
    public float getWoodMultiplier() {
        return axeData.woodMultiplier;
    }

    public void setPosition(Vector3f position) {
        if(mesh.hasMesh(AXE_ID)) {
            mesh.setPosition(AXE_ID, position);
        }
    }

    /**
     * Upgrade
     */
    public void upgrade() {
        if(axeData.level < 10) {
            axeData.level++;
            AxeData newConfig = axeData.configs.get(axeData.level);
            if(newConfig != null) {
                axeData.damage = newConfig.damage;
                axeData.speed = newConfig.speed;
                axeData.woodMultiplier = newConfig.woodMultiplier;
                axeData.upgradeCost = newConfig.upgradeCost;
            }
            updateMesh();
            System.out.println("Axe upgraded to level " + axeData.level);
        }
    }

     private void updateMesh() {
        if(mesh.hasMesh(AXE_ID)) {
            mesh.removeMesh(AXE_ID);
        }
        createMesh();
    }

    public AxeData getAxeData() {
        return axeData;
    }

    /**
     * Render
     */
    public void render() {
        if(mesh.hasMesh(AXE_ID)) {
            mesh.render(AXE_ID, 0);
        }
    }
}
