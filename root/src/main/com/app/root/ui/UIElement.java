package main.com.app.root.ui;

import java.util.HashMap;
import java.util.Map;

public class UIElement {
    public String type;
    public String id;
    public String text;
    public int x;
    public int y;
    public int width;
    public int height;
    public float scale;
    public float[] color;
    public String action;
    public Map<String, String> attr;
    public boolean visible;
    public boolean hasBackground;
    public float borderWidth;
    public float[] borderColor;

    public UIElement(
        String type,
        String id,
        String text,
        int x,
        int y,
        int width,
        int height,
        float scale,
        float[] color,
        String action
    ) {
        this.type = type;
        this.id = id;
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.color = color;
        this.action = action;
        this.attr = new HashMap<>();
        this.visible = true;
        this.hasBackground = type.equals("div") || type.equals("button");
        this.borderWidth = 0.0f;
        this.borderColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    }
    
    public float getRed() { 
        return color.length > 0 ? color[0] : 1.0f; 
    }
    public float getGreen() { 
        return color.length > 1 ? color[1] : 1.0f; 
    }
    public float getBlue() { 
        return color.length > 2 ? color[2] : 1.0f; 
    }
    public float getAlpha() { 
        return color.length > 3 ? color[3] : 1.0f; 
    }
    public boolean hasBorder() { 
        return borderWidth > 0.0f; 
    }
}
