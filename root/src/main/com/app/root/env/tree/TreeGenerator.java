package main.com.app.root.env.tree;
import main.com.app.root.mesh.Mesh;
import java.util.Random;
import org.joml.Vector3f;

public class TreeGenerator {
    private final TreeData treeData;
    private final Vector3f position;
    private final Mesh mesh;
    private final String MESH_ID;
    
    private String id;
    private float currHealth;
    private boolean isAlive;
    private float respawnTimer;
    private Random random;

    public TreeGenerator(TreeData treeData, Vector3f position, Mesh mesh) {
        this.treeData = treeData;
        this.position = position;
        this.mesh = mesh;
        this.MESH_ID = "tree_" + System.currentTimeMillis() + "_" + treeData.getIndexTo();

        this.currHealth = treeData.getHealth();
        this.isAlive = true;
        this.respawnTimer = 0;
        this.random = new Random();

        createMesh();
    }

    /**
     * Mesh
     */
    private void createMesh() {
        try {
            String treeName = "tree" + treeData.getLevel();
            mesh.addModel(MESH_ID, treeName);
            mesh.setPosition(MESH_ID, position);
            
            System.out.println("Created mesh for " + treeName);
        } catch(Exception err) {
            System.err.println("Failed to create mesh for " + treeData.getIndexTo() + ": " + err.getMessage());
            err.printStackTrace();
        }
    }

    private void destroyMesh() {
        if(mesh.hasMesh(MESH_ID)) {
            mesh.removeMesh(MESH_ID);
            System.out.println("Mesh destroyed for " + MESH_ID);
        }
    }

    public int takeDamage(int damage, int axeLevel) {
        if(!isAlive) return 0;

        if(axeLevel < treeData.getLevel()) {
            System.out.println("Axe level " + axeLevel + " too low for tree level " + treeData.getLevel());
            return 0;
        }

        currHealth -= damage;
        System.out.println(treeData.getIndexTo() + " took " + damage + " damage. Health: " + currHealth + "/" + treeData.getHealth());
        if(currHealth <= 0) {
            isAlive = false;
            respawnTimer = treeData.getRespawnTime();
            destroyMesh();

            int woodDrop = 
                treeData.getWoodMin() +
                random.nextInt(
                    treeData.getWoodMax() - treeData.getWoodMin() + 1
                );

            System.out.println(treeData.getIndexTo() + " destroyed! Dropping " + woodDrop + " wood.");
            return woodDrop;
        }

        return 0;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
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
        destroyMesh();
        isAlive = false;
    }

    /**
     * Respawn
     */
    private void respawn() {
        isAlive = true;
        currHealth = treeData.getHealth();
        createMesh();
        System.out.println(treeData.getIndexTo() + " has respawned at [" + position.x + ", " + position.z + "]");
    }

    /**
     * Update
     */
    public void update(float deltaTime) {
        if(!isAlive) {
            respawnTimer -= deltaTime;
            if(respawnTimer <= 0) {
                respawn();
            }
        }
    }

    /**
     * Render
     */
    public void render() {
        if(isAlive) {
            mesh.render(MESH_ID, getLevel());
            System.out.println("Rendering tree " + id);
        }
    }
}
