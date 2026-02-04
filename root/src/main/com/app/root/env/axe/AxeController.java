package main.com.app.root.env.axe;
import main.com.app.root.DependencyValue;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.env.EnvInstance;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.MeshRenderer;
import main.com.app.root.mesh.ModelInfo;
import main.com.app.root.mesh.ModelMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class AxeController implements EnvInstance<AxeController> {
    @DependencyValue private Mesh mesh;

    private static AxeController instance;
    private AxeData axeData;

    public static final int AXE_MIN_LEVEL = 0;
    public static final int AXE_MAX_LEVEL = 10;

    private final String AXE_ID = "AXE"; 

    private Vector3f handOffset = new Vector3f(-1.0f, 1.5f, 0.5f);
    private Vector3f handRotation = new Vector3f(0.0f, 0.0f, 0.0f);

    @Override
    public AxeController getInstance() {
        if(instance == null) {
            instance = this;
            this.axeData = new AxeData("axe0", 0, 10.0f, 1.0f, 1, 100);
            this.axeData.createDefaultConfigs();
            createMesh();
        }
        return instance;
    }

    public void setLevel(int level) {
        if(level < 0 || level > 10) return;

        this.axeData.level = level;
        AxeData newConfig = axeData.configs.get(level);
        if(newConfig != null) {
            axeData.damage = newConfig.damage;
            axeData.speed = newConfig.speed;
            axeData.woodMultiplier = newConfig.woodMultiplier;
            axeData.upgradeCost = newConfig.upgradeCost;
        }
        updateMesh();
    }

    private void createMesh() {
        try {
            String axeName = "axe" + axeData.level;
            mesh.addModel(AXE_ID, axeName);
            loadTex(AXE_ID, axeName);
            
            MeshRenderer renderer = mesh.getMeshRenderer(AXE_ID);
            if(renderer != null) {
                renderer.setIsDynamic(true);
                System.out.println("Axe mesh created and set to dynamic mode");
            } else {
                System.err.println("Failed to get axe renderer!");
            }
        } catch(Exception err) {
            System.err.println("Failed to load axe model: " + axeData + ": " + err.getMessage());
        }
    }

    /**
     * Load Texure
     */
    public void loadTex(String meshId, String name) {
        ModelMap modelMap = MeshLoader.getModelMap();
        ModelInfo info = modelMap.getModelInfo(name);
        String texPath = info.getTexture();
    
        int id = TextureLoader.load(texPath);
        if(id <= 0) {
            System.err.println("FAILED to load texture!");
            return;
        }
            
        mesh.setTex(meshId, id);
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
     * Set Bone Transform
     */
    public void setBoneTransform(Matrix4f boneTransform) {
        if(!mesh.hasMesh(AXE_ID)) {
            System.err.println("Cannot set bone transform - AXE mesh not found!");
            return;
        }

        Matrix4f finalTransform = new Matrix4f(boneTransform)
            .translate(handOffset)
            .rotateX((float) Math.toRadians(handRotation.x))
            .rotateY((float) Math.toRadians(handRotation.y))
            .rotateZ((float) Math.toRadians(handRotation.z));
            
        mesh.setModelMatrix(AXE_ID, finalTransform);
    }

    /**
     * Set Hand
     */
    public void setHandOffset(float x, float y, float z) {
        handOffset.set(x, y, z);
    }

    public void setHandRotation(float x, float y, float z) {
        handRotation.set(x, y, z);
    }

    /**
     * Upgrade
     */
    public void upgrade() {
        if(axeData.level < AXE_MAX_LEVEL) {
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

    public void updateMesh() {
        if(mesh.hasMesh(AXE_ID)) {
            mesh.remove(AXE_ID);
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

    public static void reset() {
        instance = null;
    }
}