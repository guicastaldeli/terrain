package main.com.app.root._text_renderer;

public class FontMetrics {
    public final float lineHeight;
    public final float ascent;
    public final float descent;
    public final float xHeight;
    public final float boundingWidth;
    public final float boundingHeight;
        
    public FontMetrics(
        float lineHeight, 
        float ascent, 
        float descent, 
        float xHeight, 
        float boundingWidth, 
        float boundingHeight
    ) {
        this.lineHeight = lineHeight;
        this.ascent = ascent;
        this.descent = descent;
        this.xHeight = xHeight;
        this.boundingWidth = boundingWidth;
        this.boundingHeight = boundingHeight;
    }
}
