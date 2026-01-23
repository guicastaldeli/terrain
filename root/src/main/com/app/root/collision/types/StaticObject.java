package main.com.app.root.collision.types;
import org.joml.Vector3f;

import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.Collider;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.CollisionResult;
import main.com.app.root.player.RigidBody;

public class StaticObject implements Collider {
    private BoundingBox bBox;
    private String type;

    private boolean isMap;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;
    private float maxHeight = 1.0f;

    public StaticObject(BoundingBox bBox, String type) {
        this.bBox = bBox;
        this.type = type;
        this.isMap = false;
    }

    public StaticObject(
        float[] heightMapData,
        int width,
        int height,
        String type
    ) {
        this.heightMapData = heightMapData;
        this.mapWidth = width;
        this.mapHeight = height;
        this.type = type;
        this.isMap = true;

        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for(int i = 0; i < heightMapData.length; i++) {
            float heightVal = heightMapData[i];
            minY = Math.min(minY, heightVal);
            maxY = Math.max(maxY, heightVal);
        }

        this.maxHeight = maxY;

        this.bBox = calcMapBounds(minY, maxY);
    }

    /**
     * Calculate Map Bounds
     */
    private BoundingBox calcMapBounds(float minY, float maxY) {
        return new BoundingBox(
            -Float.MAX_VALUE, minY, -Float.MAX_VALUE,
            Float.MAX_VALUE, maxY, Float.MAX_VALUE
        );
    }

    private float getHeightAt(int x, int z) {
        if(x < 0 || 
            x >= mapWidth ||
            z < 0 ||
            z >= mapHeight
        ) {
            return 0.0f;
        }

        return heightMapData[x * mapHeight + z];
    }

    public float getHeightAtWorld(float worldX, float worldZ) {
        int x = (int)((worldX + mapWidth / 2.0f));
        int z = (int)((worldZ + mapHeight / 2.0f));
        if(x < 0 || 
            x >= mapWidth || 
            z < 0 || 
            z >= mapHeight
        ) {
            return 0.0f;
        }

        return getHeightAt(x, z);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bBox;
    }

    @Override
    public RigidBody getRigidBody() {
        return null;
    }

    @Override
    public void onCollision(CollisionResult col) {
        if(isMap) {
            //Sounds
        }
    }

    /**
     * Check Map Collison
     */
    public CollisionResult checkMapCollision(BoundingBox box) {
        if(!isMap) {
            if(box.intersects(bBox)) {
                return calcBoxCollision(box, bBox);
            }
            return new CollisionResult();
        }

        float heighestTerrainHeight = Float.MIN_VALUE;
        int samplePoints = 5;

        float[] sampleX = {
            box.minX,
            box.minX + (box.maxX - box.minX) / 2,
            box.maxX,
            box.minX,
            box.maxX
        };
        float[] sampleZ = {
            box.minZ,
            box.minZ + (box.maxZ - box.minZ) / 2,
            box.minZ,
            box.maxZ,
            box.maxZ
        };

        for(int i = 0; i < samplePoints; i++) {
            float height = getHeightAtWorld(sampleX[i], sampleZ[i]);
            if(height > heighestTerrainHeight) heighestTerrainHeight = height;
        }

        float playerBottom = box.minY;
        float groundMargin = 20.0f;

        if(playerBottom <= heighestTerrainHeight + groundMargin &&
            playerBottom >= heighestTerrainHeight - groundMargin
        ) {
            Vector3f normal = new Vector3f(0, 1, 0);
            float depth = Math.abs(playerBottom - heighestTerrainHeight);
            return new CollisionResult(
                true,
                normal,
                depth,
                this,
                CollisionManager.CollisionType.STATIC_OBJECT
            );
        }

        return new CollisionResult();
    }

    /**
     * Calculate Box Collision
     */
    private CollisionResult calcBoxCollision(BoundingBox a, BoundingBox b) {
        float xOverlap = Math.min(a.maxX, b.maxX) - Math.max(a.minX, b.minX);
        float yOverlap = Math.min(a.maxY, b.maxY) - Math.max(a.minY, b.minY);
        float zOverlap = Math.min(a.maxZ, b.maxZ) - Math.max(a.minZ, b.minZ);
        
        float minOverlap = Math.min(Math.min(xOverlap, yOverlap), zOverlap);
        Vector3f normal = new Vector3f();
        
        if(minOverlap == xOverlap) {
            normal.set(a.maxX > b.maxX ? 1 : -1, 0, 0);
        } else if(minOverlap == yOverlap) {
            normal.set(0, a.maxY > b.maxY ? 1 : -1, 0);
        } else {
            normal.set(0, 0, a.maxZ > b.maxZ ? 1 : -1);
        }
        
        return new CollisionResult(
            true,
            normal,
            minOverlap,
            this,
            CollisionManager.CollisionType.STATIC_OBJECT
        );
    }

    public String getType() {
        return type;
    }

    public boolean isMap() {
        return isMap;
    }
}
