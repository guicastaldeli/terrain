package main.com.app.root.mesh;

public class Sprite {
    public final int texId;
    public final int width;
    public final int height;
    public final String key;

    public Sprite(
        int texId, 
        int width, 
        int height, 
        String key
    ) {
        this.texId = texId;
        this.width = width;
        this.height = height;
        this.key = key;
    }
}
