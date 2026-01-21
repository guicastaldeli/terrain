package main.com.app.root._text_renderer;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import java.io.FileOutputStream;
import java.io.IOException;

public class Atlas {
    private int textureId;
    private int width;
    private int height;
    private List<Glyph> glyphs;
    private ByteBuffer atlasBuffer;
    
    private int cursorX = 0;
    private int cursorY = 0;
    private int rowHeight = 0;
    private int padding = 1;

    public Atlas(int width, int height) {
        this.width = width;
        this.height = height;
        this.glyphs = new ArrayList<>();

        atlasBuffer = BufferUtils.createByteBuffer(width * height * 4);
        for(int i = 0; i < width * height * 4; i++) {
            atlasBuffer.put((byte)0);
        }
        atlasBuffer.flip();

        createTex();
    }

    /**
     * Create Texture
     */
    public void createTex() {
        textureId = glGenTextures();
        
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA8,
            width,
            height,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            atlasBuffer
        );
        
        int error = glGetError();
        if(error != GL_NO_ERROR) {
            System.err.println("OpenGL error after texture creation: " + error);
        }
        
        glBindTexture(GL_TEXTURE_2D, 0);
        System.out.println("Texture created successfully");
    }

    public void saveAtlasAsPPM(String filename) {
        try {
            System.out.println("Saving atlas as PPM: " + filename);
            
            glBindTexture(GL_TEXTURE_2D, textureId);
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glBindTexture(GL_TEXTURE_2D, 0);
            
            FileOutputStream fos = new FileOutputStream(filename);
            
            String header = "P3\n" + width + " " + height + "\n255\n";
            fos.write(header.getBytes());
            
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    int index = (y * width + x) * 4;
                    
                    int r = pixels.get(index) & 0xFF;
                    int g = pixels.get(index + 1) & 0xFF;
                    int b = pixels.get(index + 2) & 0xFF;
                    int a = pixels.get(index + 3) & 0xFF;
                    
                    fos.write((a + " " + a + " " + a + " ").getBytes());
                }
                fos.write("\n".getBytes());
            }
            
            fos.close();
            System.out.println("Atlas saved successfully to " + filename);
            
        } catch(IOException e) {
            System.err.println("Failed to save atlas: " + e.getMessage());
        }
    }

    /**
     * Update Texture
     */
    private void updateTex() {
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexSubImage2D(
            GL_TEXTURE_2D,
            0, 0, 0,
            width, height,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            atlasBuffer
        );
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /**
     * Add Glyph
     */
    public boolean addGlyph(FontLoader fontLoader, char c, int padding) {
        Glyph glyph = fontLoader.loadGlyph(c);
        if(glyph == null) {
            System.out.println("Failed: glyph is null");
            return false;
        }

        int rectWidth = glyph.bitmapWidth;
        int rectHeight = glyph.bitmapHeight;

        if(cursorX + rectWidth > width) {
            cursorX = 0;
            cursorY += rowHeight;
            rowHeight = 0;
            //System.out.println("Moved to next row: " + cursorX + "," + cursorY);
        }
        if(cursorY + rectHeight > height) {
            System.out.println("ERROR: Atlas is full!");
            return false;
        }
        rowHeight = Math.max(rowHeight, rectHeight);
        
        int x = cursorX;
        int y = cursorY;
        cursorX += rectWidth;

        ByteBuffer grayscaleBitmap = fontLoader.rasterizeGlyph(glyph, padding);
        if(grayscaleBitmap == null) {
            System.out.println("Failed: bitmap is null");
            return false;
        }
        ByteBuffer rgbaBitmap = convertGrayscaleToRGBA(grayscaleBitmap, rectWidth, rectHeight);

        copyToAtlas(
            rgbaBitmap,
            x, y,
            rectWidth, rectHeight 
        );
        glyph.texCoordX = (float)x / width;
        glyph.texCoordY = (float)y / height;
        glyph.texWidth = (float)rectWidth / width;
        glyph.texHeight = (float)rectHeight / height;
        
        glyph.textureId = textureId;
        glyphs.add(glyph);
        updateTex();
        
        return true;
    }

    /**
     * Convert Grayscale 
     */
    private ByteBuffer convertGrayscaleToRGBA(ByteBuffer grayscale, int width, int height) {
        ByteBuffer rgba = BufferUtils.createByteBuffer(width * height * 4);
        
        for(int i = 0; i < width * height; i++) {
            byte grayValue = grayscale.get(i);
            rgba.put((byte)0xFF);
            rgba.put((byte)0xFF);
            rgba.put((byte)0xFF);
            rgba.put(grayValue);
        }
        
        rgba.flip();
        return rgba;
    }

    /**
     * Copy to Atlas
     */
    private void copyToAtlas(
        ByteBuffer source,
        int x,
        int y,
        int width,
        int height
    ) {
        for(int row = 0; row < height; row++) {
            for(int col = 0; col < width; col++) {
                int sourcePos = (row * width + col) * 4;
                int atlasPos = ((y + row) * this.width + (x + col)) * 4;
                
                if(sourcePos < source.capacity() - 3 && atlasPos < atlasBuffer.capacity() - 3) {
                    atlasBuffer.put(atlasPos, source.get(sourcePos));
                    atlasBuffer.put(atlasPos + 1, source.get(sourcePos + 1));
                    atlasBuffer.put(atlasPos + 2, source.get(sourcePos + 2));
                    atlasBuffer.put(atlasPos + 3, source.get(sourcePos + 3));
                }
            }
        }
    }

    public Glyph getGlyph(char c) {
        for(Glyph g : glyphs) {
            if(g.codepoint == c) {
                return g;
            }
        }
        return null;
    }

    public int getTextureId() {
        return textureId;
    }

    public void cleanup() {
        if(textureId != 0) {
            glDeleteTextures(textureId);
        }
    }
}