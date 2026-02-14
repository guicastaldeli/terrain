package main.com.app.root.lightning;

public enum LightningData {
    AMBIENT("ambient"),
    DIRECTIONAL("directional"),
    POINT("point");

    private final String type;

    LightningData(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getShaderFile() {
        return "lightning" + type + ".glsl";
    }
}
