package main.com.app.root.screen;
import java.util.HashMap;
import java.util.Map;

public class ScreenElement {
    public String type;
    public String id;
    public String text;
    public String fontFamily;
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
    public boolean hasShadow = false;
    public float shadowOffsetX = 0f;
    public float shadowOffsetY = 0f;
    public float shadowBlur = 0;
    public float[] shadowColor = new float[]{0f, 0f, 0f, 0.5f};
    public int scrollOffsetY = 0;
    public int maxScrollY = 0;
    public boolean scrollable = false;

    public ScreenElement(
        String type,
        String id,
        String text,
        String fontFamily,
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
        this.fontFamily = fontFamily;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.color = color;
        this.action = action;
        this.attr = new HashMap<>();
        this.visible = true;
        this.borderWidth = 0.0f;
        this.borderColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    }
    public ScreenElement(
        String type,
        String id,
        String text,
        String fontFamily,
        int x,
        int y,
        int width,
        int height,
        float scale,
        float[] color,
        boolean hasBackground,
        String action
    ) {
        this.type = type;
        this.id = id;
        this.text = text;
        this.fontFamily = fontFamily;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.color = color;
        this.action = action;
        this.attr = new HashMap<>();
        this.visible = true;
        this.hasBackground = hasBackground;
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