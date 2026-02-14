package main.com.app.root._font;

public class FontConfig {
    public final String name;
    public final int size;
    public final String path;

    public FontConfig(String name, int size, String path) {
        this.name = name;
        this.size = size;
        this.path = path;
    }

    @Override
    public String toString() {
        return name + " (" + size + "pt)";
    }
}
