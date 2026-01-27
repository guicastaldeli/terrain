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
    
    public boolean hoverable = false;
    public boolean isHovered = false;
    public float[] hoverColor = null;
    public float[] hoverTextColor = null;
    public float[] hoverBorderColor = null;
    public float hoverScale = 1.0f;
    
    private float[] originalColor;
    private float[] originalBorderColor;
    private float originalScale;

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
        this.fontFamily = fontFamily != null ? fontFamily : "arial";
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.color = color;
        this.action = action;
        this.attr = new HashMap<>();
        this.visible = true;
        this.hasBackground = false;
        this.borderWidth = 0.0f;
        this.borderColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        
        this.originalColor = color != null ? color.clone() : new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        this.originalBorderColor = borderColor.clone();
        this.originalScale = scale;
        
        this.hoverColor = null;
        this.hoverTextColor = null;
        this.hoverBorderColor = null;
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

        this.originalColor = color != null ? color.clone() : new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        this.originalBorderColor = borderColor.clone();
        this.originalScale = scale;
        
        this.hoverColor = null;
        this.hoverTextColor = null;
        this.hoverBorderColor = null;
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
    
    /**
     * Apply Hover
     */
    public void applyHover() {
        if(!hoverable || isHovered) return;
        
        isHovered = true;
        
        if(hoverColor != null) {
            color = hoverColor;
        }
        if(hoverBorderColor != null) {
            borderColor = hoverBorderColor;
        }
        if(hoverScale > 0) {
            scale = hoverScale;
        }
    }
    
    /**
     * Remove Hover
     */
    public void removeHover() {
        if(!hoverable || !isHovered) return;
        
        isHovered = false;
        color = originalColor.clone();
        borderColor = originalBorderColor.clone();
        scale = originalScale;
    }
    
    /**
     * Contains Point
     */
    public boolean containsPoint(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width &&
               mouseY >= y && mouseY <= y + height;
    }
}