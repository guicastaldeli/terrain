package main.com.app.root.collision.types;
import main.com.app.root.collision.BoundingBox;
import main.com.app.root.collision.Collider;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.CollisionResult;
import main.com.app.root.collision.CollisionManager.CollisionType;
import main.com.app.root.env.world.Water;
import main.com.app.root.env.world.WorldGenerator;
import main.com.app.root.player.RigidBody;
import org.joml.Vector3f;

public class StaticObject implements Collider {
    private BoundingBox bBox;
    private String type;

    private boolean isMap;
    private float[] heightMapData;
    private int mapWidth;
    private int mapHeight;

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

    /**
     * 
     * Get Height
     * 
     */
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
        int x = (int)(worldX + mapWidth / 2.0f);
        int z = (int)(worldZ + mapHeight / 2.0f);

        if(x < 0 || 
            x >= mapWidth ||
            z < 0 ||
            z >= mapHeight
        ) {
            return -100.0f;
        }

        return getHeightAt(x, z);
    }

    public WorldGenerator.TerrainType getTerrainTypeAt(float worldX, float worldZ) {
        float height = getHeightAtWorld(worldX, worldZ);
        
        float BEACH_LEVEL = 60.0f;
        float GRASS_LEVEL = 65.0f;
        float MOUNTAIN_LEVEL = 250.0f;
        float BLEND_RANGE = 10.0f;
        
        if(height < Water.LEVEL) {
            return WorldGenerator.TerrainType.WATER;
        }
        else if(height < BEACH_LEVEL) {
            return WorldGenerator.TerrainType.SAND;
        }
        else if(height < GRASS_LEVEL) {
            float t = (height - BEACH_LEVEL) / (GRASS_LEVEL - BEACH_LEVEL);
            return t > 0.5f ? WorldGenerator.TerrainType.GRASS : WorldGenerator.TerrainType.SAND;
        }
        else if(height < MOUNTAIN_LEVEL) {
            float grassEnd = GRASS_LEVEL + BLEND_RANGE;
            
            if(height < grassEnd) {
                float t = (height - GRASS_LEVEL) / BLEND_RANGE;
                return t > 0.5f ? WorldGenerator.TerrainType.ROCK : WorldGenerator.TerrainType.GRASS;
            } else {
                return WorldGenerator.TerrainType.ROCK;
            }
        }
        else {
            float t = Math.min((height - MOUNTAIN_LEVEL) / 20.0f, 1.0f);
            return t > 0.5f ? WorldGenerator.TerrainType.SNOW : WorldGenerator.TerrainType.ROCK;
        }
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
    public void onCollision(CollisionResult col) {}

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

        float centerX = (box.minX + box.maxX) / 2.0f;
        float centerZ = (box.minZ + box.maxZ) / 2.0f;

        float[] sampleX = {
            centerX,
            centerX - box.getSizeX() * 0.5f,
            centerX + box.getSizeX() * 0.5f,
            centerX,
            centerX
        };
        float[] sampleZ = {
            centerZ,
            centerZ,
            centerZ,
            centerZ - box.getSizeZ() * 0.5f,
            centerZ + box.getSizeZ() * 0.5f
        };

        float heightestTerrainHeight = Float.MIN_VALUE;
        float lowestTerrainHeight = Float.MAX_VALUE;

        for(int i = 0; i < sampleX.length; i++) {
            float height = getHeightAtWorld(sampleX[i], sampleZ[i]);
            if(height > heightestTerrainHeight) heightestTerrainHeight = height;
            if(height < lowestTerrainHeight) lowestTerrainHeight = height;
        }

        float playerBottom = box.minY;
        float groundMargin = 4.0f;
        float maxStepHeight = 1.0f;

        boolean isOnGround = false;
        float targetHeight = heightestTerrainHeight;

        if(playerBottom > heightestTerrainHeight &&
            playerBottom <= heightestTerrainHeight + maxStepHeight
        ) {
            isOnGround = true;
            targetHeight = heightestTerrainHeight;
        }
        else if(playerBottom <= heightestTerrainHeight + groundMargin) {
            isOnGround = true;
            targetHeight = heightestTerrainHeight;
        }

        if(isOnGround) {
            Vector3f normal = new Vector3f(0, 1, 0);
            float depth = Math.abs(playerBottom - targetHeight);
            return new CollisionResult(
                true,
                normal,
                depth,
                this,
                CollisionType.STATIC_OBJECT
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

    /**
     * Resolve Collision
     */
    public static void resolveCollision(
        Vector3f position,
        BoundingBox bBox,
        RigidBody body, 
        CollisionResult collision
    ) {
        StaticObject staticObj = (StaticObject) collision.otherCollider;
        
        if(staticObj.isMap()) {
            Vector3f terrainNormal = collision.normal;
            float terrainHeight = getHeightAtPos(position, staticObj);
            float playerBottom = bBox.minY;
            float playerHeight = bBox.getSizeY();
            
            boolean wasInWater = body.isInWater();
            boolean isAboveWater = playerBottom > Water.LEVEL;
            
            if(wasInWater && isAboveWater && terrainHeight > Water.LEVEL) {
                float targetY = terrainHeight + playerHeight / 2.0f;
                position.y = targetY;
                body.setPosition(position);
                body.setOnGround(true);
                body.setInWater(false, 0.0f);
                
                Vector3f vel = body.getVelocity();
                if(vel.y < 0) vel.y = 0;
                body.setVelocity(vel);
                return;
            }
            
            float slopeAngle = (float) Math.acos(terrainNormal.y);
            float maxWalkableSlope = (float) Math.toRadians(45.0f);
            

            if(slopeAngle > maxWalkableSlope && body.getVelocity().y <= 0) {
                Vector3f velocity = body.getVelocity();
                Vector3f slideVelocity = calculateSlideVelocity(velocity, terrainNormal);
                
                float slideFactor = 0.1f + (slopeAngle - maxWalkableSlope) * 2.0f;
                slideVelocity.mul(slideFactor);
                
                slideVelocity.y = Math.min(slideVelocity.y, -2.0f);
                body.setVelocity(slideVelocity);
                body.setOnGround(false);
                return;
            }
            
            if(playerBottom <= terrainHeight + 1.0f && body.getVelocity().y <= 0) {
                float targetHeight = terrainHeight + playerHeight / 2.0f;
                
                float currentY = position.y;
                float smoothFactor = 0.3f;
                position.y = currentY + (targetHeight - currentY) * smoothFactor;
                
                if(body.isOnGround()) {
                    Vector3f velocity = body.getVelocity();
                    velocity = projectOntoPlane(velocity, terrainNormal);
                    velocity.y = 0;
                    body.setVelocity(velocity);
                }
                
                body.setPosition(position);
                body.setOnGround(true);
                
                Vector3f vel = body.getVelocity();
                if(vel.y < 0) vel.y = 0;
                body.setVelocity(vel);
            }
            return;
        }
    }

    private static Vector3f projectOntoPlane(Vector3f vector, Vector3f planeNormal) {
        Vector3f projection = new Vector3f();
        float dot = vector.dot(planeNormal);
        projection.set(vector).sub(new Vector3f(planeNormal).mul(dot));
        return projection;
    }

    private static Vector3f calculateSlideVelocity(Vector3f velocity, Vector3f normal) {
        Vector3f slideVelocity = new Vector3f();
        
        float dot = velocity.dot(normal);
        if(dot < 0) {
            slideVelocity.set(velocity).sub(new Vector3f(normal).mul(dot));
        }
        
        slideVelocity.y -= 9.8f * 0.1f;
        
        return slideVelocity;
    }

    public static float getHeightAtPos(Vector3f position, StaticObject obj) {
        return obj.getHeightAtWorld(position.x, position.z);
    }
}