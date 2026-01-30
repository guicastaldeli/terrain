package main.com.app.root.mesh;

public class ModelInfo {
    private final String name;
    private final String path;
    private final float[] size;
    private final String texture;

    public ModelInfo(
        String name, 
        String path, 
        String texture,
        float[] size
    ) {
        this.name = name;
        this.path = path;
        this.texture = texture;
        this.size = size;
    }
        
    public String getName() { 
        return name; 
    }
    public String getPath() { 
        return path; 
    }
    public float[] getSize() {
        return size; 
    }
    public float getWidth() { 
        return size[0]; 
    }
    public float getHeight() { 
        return size[1]; 
    }
    public float getDepth() { 
        return size[2]; 
    }
    public String getTexture() { 
        return texture; 
    }
}
