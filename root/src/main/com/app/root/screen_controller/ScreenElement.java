package main.com.app.root.screen_controller;
import java.util.HashMap;
import java.util.Map;

public class ScreenElement {
    public String type;
    public String id;
    public String text;
    public int x;
    public int y;
    public float scale;
    public float[] color;
    public String action;
    public Map<String, String> attr;

    public ScreenElement(
        String type,
        String id,
        String text,
        int x,
        int y,
        float scale,
        float[] color,
        String action
    ) {
        this.type = type;
        this.id = id;
        this.text = text;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.color = color;
        this.action = action;
        this.attr = new HashMap<>();
    }
}
