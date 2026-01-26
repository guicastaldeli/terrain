package main.com.app.root.mesh;

public class SpriteInstance {
    public final Sprite sprite;
    public float x;
    public float y;
    public float width;
    public float height;
    public float rotation;
    public float[] color;
    public int layer;

    public SpriteInstance(
        Sprite sprite, 
        float x, 
        float y, 
        float width, 
        float height
    ) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = 0;
        this.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        this.layer = 0;
    }
}
