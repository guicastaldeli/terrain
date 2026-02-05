package main.com.app.root.env.tree;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerData;
import main.com.app.root.SpawnerHandler;
import main.com.app.root._resources.AudioLoader;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.StaticObject;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshLoader;
import main.com.app.root.mesh.ModelInfo;
import main.com.app.root.mesh.ModelMap;
import main.com.app.root.player.Camera;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIController.UIType;
import main.com.app.root.ui.info.Info;
import java.util.List;
import java.util.Random;
import org.joml.Vector3f;

public class TreeGenerator {
    public TreeController treeController;
    public TreeData treeData;
    public Spawner spawner;
    private UIController uiController;
    public Mesh mesh;
    private StaticObject collider;
    private CollisionManager collisionManager;
    
    public final Vector3f position;
    public String MESH_ID;
    public String id;
    public float currHealth;
    public boolean isAlive;
    public float respawnTimer;
    public Random random;

    public TreeGenerator(
        TreeData treeData, 
        Vector3f position, 
        Mesh mesh, 
        Spawner spawner,
        CollisionManager collisionManager
    ) {
        this.treeData = treeData;
        this.position = position;
        this.mesh = mesh;
        this.spawner = spawner;

        this.MESH_ID = null;
        this.currHealth = treeData.getHealth();
        this.isAlive = true;
        this.respawnTimer = 0;
        this.random = new Random();
        this.collisionManager = collisionManager;
    }

    public void setTreeController(TreeController controller) {
        this.treeController = controller;
    }

    public void setUIController(UIController uiController) {
        this.uiController = uiController;
    }

    /**
     * 
     * Collider
     * 
     */
    public void createCollider(CollisionManager collisionManager) {
        if(collisionManager == null || position == null) return;
        
        this.collisionManager = collisionManager;
        
        float baseWidth = 10.0f;
        float baseHeight = 10.0f;
        float baseDepth = 10.0f;
        
        float scale = 1.0f + (treeData.getLevel() * 0.3f);
        float colliderWidth = baseWidth * scale;
        float colliderHeight = baseHeight * scale;
        float colliderDepth = baseDepth * scale;
        
        BoundingBox bbox = new BoundingBox(
            position.x - colliderWidth / 2.0f,
            position.y,
            position.z - colliderDepth / 2.0f,
            position.x + colliderWidth / 2.0f,
            position.y + colliderHeight,
            position.z + colliderDepth / 2.0f
        );
        
        collider = new StaticObject(bbox, MESH_ID);
        collisionManager.addStaticCollider(collider);
    }

    public void removeCollider() {
        if(collisionManager != null && collider != null) {
            collisionManager.removeCollider(collider);
            collider = null;
        }
    }

    /**
     * Mesh
     */
    public void createMesh() {
        try {
            String treeName = "tree" + treeData.getLevel();
            mesh.addModel(MESH_ID, treeName);
            mesh.setPosition(MESH_ID, position);
            loadTex(MESH_ID, treeName);
        } catch(Exception err) {
            System.err.println("Failed to create mesh for " + treeData.getIndexTo() + 
                            ": " + err.getMessage());
            err.printStackTrace();
        }
    }

    public void destroyMesh() {
        if(mesh.hasMesh(MESH_ID)) {
            mesh.remove(MESH_ID);
            //System.out.println("Mesh destroyed for " + MESH_ID);
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

    public int takeDamage(int damage, int axeLevel) {
        if(!isAlive) return 0;
        if(axeLevel < treeData.getLevel()) return 0;

        currHealth -= damage;
        if(uiController != null) {
            Info info = (Info) uiController.get(UIType.INFO);
            info.setLevel(getLevel());
            info.getInfoActions().showTreeDamage(
                getLevel(),
                damage,
                currHealth,
                treeData.getHealth()
            );
        }
        
        Camera camera = mesh.getMeshRenderer().getPlayerController().getCamera();
        Vector3f cameraFront = camera.getFront().normalize();
        Vector3f particleOffset = new Vector3f(cameraFront).mul(-10.0f);
        Vector3f particlePosition = new Vector3f(position).add(particleOffset);
    
        TreeInteractor.createTreeBreakEffect(mesh, particlePosition, axeLevel);
        AudioLoader.getInstance().play("test.wav");

        if(currHealth <= 0) {
            isAlive = false;
            destroyMesh();

            int woodDrop = 
                treeData.getWoodMin() +
                random.nextInt(
                    treeData.getWoodMax() - treeData.getWoodMin() + 1
                );

            System.out.println(treeData.getIndexTo() + " destroyed! Dropping " + woodDrop + " wood.");
            if(spawner != null && treeController != null) {
                List<SpawnerHandler> treeHandlers = spawner.spawnerData.get(SpawnerData.TREE);
                if(!treeHandlers.isEmpty()) {
                    TreeSpawner treeSpawner = (TreeSpawner) treeHandlers.get(0);
                    treeSpawner.handleTreeBreak(
                        treeController, 
                        new Vector3f(position), 
                        treeData.getLevel()
                    );
                }
            }
            return woodDrop;
        }

        return 0;
    }

    public void setId(String id) {
        this.id = id;
        this.MESH_ID = id + "_mesh_lvl" + treeData.getLevel();
    }

    public boolean isAlive() { 
        return isAlive; 
    }
    
    public int getLevel() { 
        return treeData.getLevel(); 
    }
    
    public Vector3f getPosition() { 
        return new Vector3f(position); 
    }
    
    public TreeData getData() { 
        return treeData; 
    }
    
    public String getMeshInstanceId() {
        return MESH_ID;
    }
    
    public float getHealthPercentage() {
        return (currHealth / treeData.getHealth()) * 100.0f;
    }

    public void cleanup() {
        removeCollider();
        destroyMesh();
        isAlive = false;
    }

    /**
     * Respawn
     */
    public void respawn() {
        isAlive = true;
        currHealth = treeData.getHealth();
        createMesh();
        System.out.println(treeData.getIndexTo() + " has respawned at [" + position.x + ", " + position.z + "]");
    }

    /**
     * Update
     */
    public void update(float deltaTime) {
        if(!isAlive && respawnTimer > 0) {
            respawnTimer -= deltaTime;
        }
    }

    /**
     * Render
     */
    public void render() {
        if(isAlive) {
            //mesh.render(MESH_ID, getLevel());
            //System.out.println("Rendering tree " + id);
        }
    }

    public float getRespawnTimer() {
        return this.respawnTimer;
    }
}