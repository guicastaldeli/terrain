package main.com.app.root.env.axe;
import org.joml.Vector3f;

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
        this.axeData = new AxeData("axe0", 0, 10.0f, 1.0f, 1, 100);
        this.axeData.createDefaultConfigs();
        createMesh();
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
                axeData.currModel = newConfig.currModel;
                axeData.texturePath = newConfig.texturePath;
                axeData.scale = newConfig.scale;
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

    /**
     * Render
     */
    public void render() {
        if(mesh.hasMesh(AXE_ID)) {
            mesh.render(AXE_ID, 0);
        }
    }
}
