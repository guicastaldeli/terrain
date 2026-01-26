package main.com.app.root.mesh;
import java.util.HashMap;
import java.util.Map;

public class SpriteBatch {
    public final Map<String, SpriteInstance> sprites;
    public final int layer;

    public SpriteBatch(int layer) {
        this.sprites = new HashMap<>();
        this.layer = layer;
    }
}
