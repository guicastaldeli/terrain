package main.com.app.root._text;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class FontLoader {
    private STBTTFontinfo fontInfo;
    private ByteBuffer fontData;
    private float fontSize;
    private float scale;
    private int ascent;
    private int descent;
    private int lineGap;
    private float lineHeight;
    private Map<Integer, GlyphMetrics> glyphMetrics;

    public FontLoader(String fontPath, float fontSize) throws IOException {
        this.fontSize = fontSize;
        this.glyphMetrics = new HashMap<>();
        load(fontPath);
        calculateMetrics();
    }

    /**
     * Load Font
     */
    private void load(String fontPath) throws IOException {
        try(FileChannel fc = FileChannel.open(Paths.get(fontPath), StandardOpenOption.READ)) {
            fontData = BufferUtils.createByteBuffer((int) fc.size() + 1);
            fc.read(fontData);
            fontData.flip();
        }

        fontInfo = STBTTFontinfo.create();
        if(!stbtt_InitFont(fontInfo, fontData)) {
            throw new IOException("Failed to init font!");
        }

        float rasterSize = fontSize * 2.0f;
        scale = stbtt_ScaleForPixelHeight(fontInfo, rasterSize);
        IntBuffer pAscent = BufferUtils.createIntBuffer(1);
        IntBuffer pDescent = BufferUtils.createIntBuffer(1);
        IntBuffer pLineGap = BufferUtils.createIntBuffer(1);
        stbtt_GetFontVMetrics(
            fontInfo, 
            pAscent, 
            pDescent, 
            pLineGap
        );
        ascent = pAscent.get(0);
        descent = pDescent.get(0);
        lineGap = pLineGap.get(0);
        lineHeight = (ascent - descent + lineGap) * scale;
    }

    /**
     * Calculate Metrics
     */
    private void calculateMetrics() {
        IntBuffer x0 = BufferUtils.createIntBuffer(1);
        IntBuffer y0 = BufferUtils.createIntBuffer(1);
        IntBuffer x1 = BufferUtils.createIntBuffer(1);
        IntBuffer y1 = BufferUtils.createIntBuffer(1);
        stbtt_GetFontBoundingBox(
            fontInfo,
            x0, y0,
            x1, y1 
        );

        float fontBoundingWidth = (x1.get(0) - x0.get(0)) * scale;
        float fontBoundingHeight = (y1.get(0) - y0.get(0)) * scale;

        int xGlyphIndex = stbtt_FindGlyphIndex(fontInfo, 'x');
        if(xGlyphIndex != 0) {
            stbtt_GetGlyphBitmapBox(
                fontInfo, 
                xGlyphIndex, 
                scale, scale,
                x0, y0,
                x1, y1 
            );
            float xHeight = (y1.get(0) - y0.get(0));

            glyphMetrics.put(
                -1,
                new GlyphMetrics(
                    -1,
                    fontBoundingWidth, fontBoundingHeight,
                    lineHeight, xHeight,
                    ascent * scale,
                    descent * scale
                )
            );
        }
    }

    public FontMetrics getFontMetrics() {
        return new FontMetrics(
            lineHeight,
            ascent * scale,
            Math.abs(descent * scale),
            glyphMetrics.get(-1).xHeight,
            glyphMetrics.get(-1).boundingWidth,
            glyphMetrics.get(-1).boundingHeight
        );
    }

    /**
     * Load Glyph
     */
    public Glyph loadGlyph(char c) {
        int codepoint = c;

        int glyphIndex = stbtt_FindGlyphIndex(fontInfo, codepoint);
        if(glyphIndex == 0) return null;

        Glyph glyph = new Glyph();
        glyph.codepoint = codepoint;
        glyph.glyphIndex = glyphIndex;

        IntBuffer x0 = BufferUtils.createIntBuffer(1);
        IntBuffer y0 = BufferUtils.createIntBuffer(1);
        IntBuffer x1 = BufferUtils.createIntBuffer(1);
        IntBuffer y1 = BufferUtils.createIntBuffer(1);

        stbtt_GetGlyphBitmapBox(
            fontInfo, 
            glyphIndex, 
            scale, scale, 
            x0, y0, 
            x1, y1
        );
        int width = x1.get(0) - x0.get(0);
        int height = y1.get(0) - y0.get(0);

        IntBuffer advance = BufferUtils.createIntBuffer(1);
        IntBuffer lsb = BufferUtils.createIntBuffer(1);
        stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, advance, lsb);

        float advanceWidth = advance.get(0) * scale;
        float leftSideBearing = lsb.get(0) * scale;

        GlyphMetrics metrics = new GlyphMetrics(
            glyphIndex,
            width,
            height,
            advanceWidth,
            leftSideBearing,
            x0.get(0),
            y0.get(0),
            width,
            height
        );
        glyphMetrics.put(codepoint, metrics);

        return createGlyphFromMetrics(codepoint, metrics);
    }

    /**
     * Create Glyph from Metrics
     */
    private Glyph createGlyphFromMetrics(int codepoint, GlyphMetrics metrics) {
        Glyph glyph = new Glyph();
        glyph.codepoint = codepoint;
        glyph.glyphIndex = metrics.glyphIndex;
        glyph.width = metrics.width;
        glyph.height = metrics.height;
        glyph.xOffset = metrics.xOffset;
        glyph.yOffset = metrics.yOffset;
        glyph.advance = metrics.advance;
        glyph.leftSideBearing = metrics.leftSideBearing;
        glyph.bitmapWidth = metrics.bitmapWidth;
        glyph.bitmapHeight = metrics.bitmapHeight;
        return glyph;
    }

    /**
     * Rasterize Glyph
     */
    public ByteBuffer rasterizeGlyph(Glyph glyph, int padding) {
        if(glyph == null) return null;

        IntBuffer x0 = BufferUtils.createIntBuffer(1);
        IntBuffer y0 = BufferUtils.createIntBuffer(1);
        IntBuffer x1 = BufferUtils.createIntBuffer(1);
        IntBuffer y1 = BufferUtils.createIntBuffer(1);
        stbtt_GetGlyphBitmapBox(
            fontInfo, 
            glyph.glyphIndex, 
            scale, scale, 
            x0, y0, 
            x1, y1
        );
        
        int width = x1.get(0) - x0.get(0);
        int height = y1.get(0) - y0.get(0);
        if(width <= 0 || height <= 0) {
            width = Math.max(1, width);
            height = Math.max(1, height);
        }

        ByteBuffer bitmap = BufferUtils.createByteBuffer(width * height);
        stbtt_MakeGlyphBitmap(
            fontInfo, 
            bitmap, 
            width, height, 
            width, 
            scale, scale, 
            glyph.glyphIndex
        );

        if(padding > 0) {
            int paddedWidth = width * padding;
            int paddedHeight = height * padding;
            ByteBuffer paddedBitmap = BufferUtils.createByteBuffer(paddedWidth * paddedHeight);
            for(int i = 0; i < paddedWidth * paddedHeight; i++) {
                paddedBitmap.put((byte) 0);
            }
            paddedBitmap.clear();
            
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    int srcPos = y * width + x;
                    if(srcPos >= bitmap.capacity()) {
                        continue;
                    }
                    
                    byte pixel = bitmap.get(srcPos);
                    for(int py = 0; py < padding; py++) {
                        for(int px = 0; px < padding; px++) {
                            int dstY = (y * padding) + py;
                            int dstX = (x * padding) + px;
                            int dstPos = dstY * paddedWidth + dstX;
                            if(dstPos < paddedBitmap.capacity()) {
                                paddedBitmap.put(dstPos, pixel);
                            }
                        }
                    }
                }
            }
            
            paddedBitmap.position(0);
            paddedBitmap.limit(paddedWidth * paddedHeight);
            
            glyph.bitmapWidth = paddedWidth;
            glyph.bitmapHeight = paddedHeight;
            
            return paddedBitmap;
        }
        
        bitmap.position(0);
        bitmap.limit(width * height);
        glyph.bitmapWidth = width;
        glyph.bitmapHeight = height;

        return bitmap;
    }

    public float getStringWidth(String text) {
        float width = 0;
        for(int i = 0; i < text.length(); i++) {
            Glyph glyph = loadGlyph(text.charAt(i));
            if(glyph != null) {
                width += glyph.advance;
                if(i < text.length() - 1) {
                    Glyph nextGlyph = loadGlyph(text.charAt(i + 1));
                    if(nextGlyph != null) {
                        width += getKerning(glyph.glyphIndex, nextGlyph.glyphIndex);
                    }
                }
            }
        }
        return width;
    }

    public float getKerning(int glyphIndex1, int glyphIndex2) {
        if(glyphIndex1 == 0 || glyphIndex2 == 0) {
            return 0.0f;
        }

        int kernAdvance = stbtt_GetGlyphKernAdvance(
            fontInfo, 
            glyphIndex1, 
            glyphIndex2
        );
        return kernAdvance * scale;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public void cleanup() {
        if(fontData != null) {
            memFree(fontData);
        }
    }
}
