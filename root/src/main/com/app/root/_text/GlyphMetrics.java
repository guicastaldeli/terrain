package main.com.app.root._text;

public class GlyphMetrics {
    public final int glyphIndex;
    public final int width;
    public final int height;
    public final float advance;
    public final float leftSideBearing;
    public final int xOffset;
    public final int yOffset;
    public final int bitmapWidth;
    public final int bitmapHeight;
    public final float boundingWidth;
    public final float boundingHeight;
    public final float xHeight;
        
    public GlyphMetrics(
        int glyphIndex, 
        int width, 
        int height, 
        float advance, 
        float leftSideBearing, 
        int xOffset, 
        int yOffset, 
        int bitmapWidth, 
        int bitmapHeight
    ) {
        this.glyphIndex = glyphIndex;
        this.width = width;
        this.height = height;
        this.advance = advance;
        this.leftSideBearing = leftSideBearing;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;
        this.boundingWidth = width;
        this.boundingHeight = height;
        this.xHeight = height;
    }

    public GlyphMetrics(
        int glyphIndex, 
        float boundingWidth, 
        float boundingHeight, 
        float lineHeight, 
        float xHeight,
        float ascent, 
        float descent
    ) {
        this.glyphIndex = glyphIndex;
        this.width = 0;
        this.height = 0;
        this.advance = 0;
        this.leftSideBearing = 0;
        this.xOffset = 0;
        this.yOffset = 0;
        this.bitmapWidth = 0;
        this.bitmapHeight = 0;
        this.boundingWidth = boundingWidth;
        this.boundingHeight = boundingHeight;
        this.xHeight = xHeight;
    }
}
