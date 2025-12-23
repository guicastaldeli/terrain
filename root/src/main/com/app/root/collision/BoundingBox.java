package main.com.app.root.collision;

public class BoundingBox {
    public float minX;
    public float minY;
    public float minZ;

    public float maxX;
    public float maxY;
    public float maxZ;

    public BoundingBox(
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
    ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean intersects(BoundingBox other) {
        return 
            (minX <= other.maxX && maxX >= other.minX) &&
            (minY <= other.maxY && maxY >= other.minY) &&
            (minZ <= other.maxZ && maxZ >= other.minZ);
    }
}
