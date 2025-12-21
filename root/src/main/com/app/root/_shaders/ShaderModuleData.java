package main.com.app.root._shaders;

public class ShaderModuleData {
    private final int type;
    private final String file;
    
    public ShaderModuleData(int type, String file) {
        this.type = type;
        this.file = file;
    }

    public int getType() {
        return type;
    }

    public String getFile() {
        return file;
    }
}
