package main.com.app.root._text_renderer;

import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root._font.FontMap;
import main.com.app.root._font.FontConfig;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TextRenderer {
    private Window window;
    private ShaderProgram shaderProgram;
    
    private Map<String, FontLoader> fontLoaders;
    private Map<String, Atlas> atlases;
    private Map<String, Map<Character, Glyph>> glyphCaches;
    
    private String currentFont = "arial";

    private int vao;
    private int vbo;
    private int ebo;

    private static final int ATLAS_SIZE = 300;
    private static final int PADDING = 1;

    public int currentWidth;
    public int currentHeight;

    public TextRenderer(
        Window window,
        ShaderProgram shaderProgram,
        int screenWidth,
        int screenHeight
    ) throws Exception {
        this.window = window;
        this.shaderProgram = shaderProgram;
        this.currentWidth = screenWidth;
        this.currentHeight = screenHeight;
        
        this.fontLoaders = new HashMap<>();
        this.atlases = new HashMap<>();
        this.glyphCaches = new HashMap<>();

        setupBuffers();
        
        FontMap.init("root/src/main/com/app/root/_font/list.lua");
        loadFont("arial");
    }
    
    /**
     * Load Font
     */
    public void loadFont(String fontKey) {
        if(fontLoaders.containsKey(fontKey)) {
            return;
        }
        
        FontConfig config = FontMap.getFont(fontKey);
        if(config == null) {
            System.err.println("Font not found: " + fontKey + ", using arial");
            fontKey = "arial";
            config = FontMap.getFont(fontKey);
            if(config == null) {
                throw new RuntimeException("Default font 'arial' not found!");
            }
        }
        
        try {
            FontLoader loader = new FontLoader(config.path, config.size);
            Atlas atlas = new Atlas(ATLAS_SIZE, ATLAS_SIZE);
            Map<Character, Glyph> cache = new HashMap<>();
            
            fontLoaders.put(fontKey, loader);
            atlases.put(fontKey, atlas);
            glyphCaches.put(fontKey, cache);
            
            preloadChars(fontKey);
            
            System.out.println("Loaded font: " + config.name + " (" + fontKey + ")");
        } catch(Exception e) {
            System.err.println("Failed to load font: " + fontKey);
            e.printStackTrace();
        }
    }

    public void updateScreenSize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
    }

    private void setupBuffers() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 8 * 4, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(1);
 
        glVertexAttribPointer(3, 2, GL_FLOAT, false, 8 * 4, 2 * 4);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(2, 4, GL_FLOAT, false, 8 * 4, 4 * 4);
        glEnableVertexAttribArray(2);

        int[] indices = {0, 1, 2, 2, 3, 0};
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    private void preloadChars(String fontKey) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for(char c : chars.toCharArray()) {
            loadGlyphToAtlas(c, fontKey);
        }
    }

    private void updateQuad(float x, float y, float w, float h, Glyph glyph, float[] color) {
        float[] vertices = {
            x,     y + h, glyph.texCoordX, glyph.texCoordY + glyph.texHeight, color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f,
            x,     y,     glyph.texCoordX, glyph.texCoordY, color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f,
            x + w, y,     glyph.texCoordX + glyph.texWidth, glyph.texCoordY, color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f,
            x + w, y + h, glyph.texCoordX + glyph.texWidth, glyph.texCoordY + glyph.texHeight, color[0], color[1], color[2], color.length > 3 ? color[3] : 1.0f
        };
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Render Text With Shadow
     */
    public void renderTextWithShadow(
        String text,
        int x, int y,
        float scale,
        float[] color,
        float shadowOffsetX, float shadowOffsetY,
        float shadowBlur,
        float[] shadowColor,
        String fontKey
    ) {
        if(text == null || text.isEmpty()) return;
        
        if(!fontLoaders.containsKey(fontKey)) {
            loadFont(fontKey);
        }
        
        currentFont = fontKey;
        
        boolean depthTest = glGetBoolean(GL_DEPTH_TEST);
        if(depthTest) glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.bind();
        shaderProgram.setUniform("shaderType", 1);
        shaderProgram.setUniform("screenSize", (float)currentWidth, (float)currentHeight);
        shaderProgram.setUniform("texSampler", 0);
        shaderProgram.setUniform("textColor", color[0], color[1], color[2]);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlases.get(currentFont).getTextureId());
        
        glBindVertexArray(vao);

        float cursorX = x;
        FontMetrics fontMetrics = fontLoaders.get(currentFont).getFontMetrics();
        float baseline = y + fontMetrics.ascent * scale;
        
        if(shadowBlur > 0) {
            int blurPasses = Math.min(5, (int)shadowBlur);
            for(int pass = 0; pass < blurPasses; pass++) {
                float blurOffset = pass * 0.5f;
                renderTextPass(text, cursorX + shadowOffsetX - blurOffset, baseline + shadowOffsetY - blurOffset, 
                            scale, shadowColor, true);
                renderTextPass(text, cursorX + shadowOffsetX + blurOffset, baseline + shadowOffsetY - blurOffset, 
                            scale, shadowColor, true);
                renderTextPass(text, cursorX + shadowOffsetX - blurOffset, baseline + shadowOffsetY + blurOffset, 
                            scale, shadowColor, true);
                renderTextPass(text, cursorX + shadowOffsetX + blurOffset, baseline + shadowOffsetY + blurOffset, 
                            scale, shadowColor, true);
            }
        } else {
            renderTextPass(text, cursorX + shadowOffsetX, baseline + shadowOffsetY, scale, shadowColor, true);
        }

        renderTextPass(text, cursorX, baseline, scale, color, false);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);

        if(depthTest) glEnable(GL_DEPTH_TEST);
        shaderProgram.unbind();
        glDisable(GL_BLEND);
    }

    private void renderTextPass(String text, float startX, float baseline, float scale, float[] color, boolean isShadow) {
        float cursorX = startX;
        Map<Character, Glyph> cache = glyphCaches.get(currentFont);
        FontLoader loader = fontLoaders.get(currentFont);
        
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = loadGlyphToAtlas(c, currentFont);
            if(glyph == null) continue;

            float xPos = cursorX + glyph.leftSideBearing * scale;
            float yPos = baseline + glyph.yOffset;
            float w = glyph.bitmapWidth * scale;
            float h = glyph.bitmapHeight * scale;
            
            updateQuad(xPos, yPos, w, h, glyph, color);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            cursorX += glyph.advance * scale;
            if(i < text.length() - 1) {
                char nextChar = text.charAt(i + 1);
                Glyph nextGlyph = cache.get(nextChar);
                
                if(nextGlyph != null) {
                    float kerning = loader.getKerning(glyph.glyphIndex, nextGlyph.glyphIndex);
                    cursorX += kerning * scale;
                }
            }
        }
    }

    public void renderText(String text, int x, int y, float scale, float[] color, String fontKey) {
        renderTextWithShadow(text, x, y, scale, color, 0, 0, 0, new float[]{0,0,0,0}, fontKey);
    }
    
    public void renderText(String text, int x, int y, float scale, float[] color) {
        renderText(text, x, y, scale, color, "arial");
    }

    private Glyph loadGlyphToAtlas(char c, String fontKey) {
        Map<Character, Glyph> cache = glyphCaches.get(fontKey);
        if(cache.containsKey(c)) {
            return cache.get(c);
        }
        
        Atlas atlas = atlases.get(fontKey);
        FontLoader loader = fontLoaders.get(fontKey);
        
        boolean success = atlas.addGlyph(loader, c, PADDING);
        if(success) {
            Glyph glyph = atlas.getGlyph(c);
            if(glyph != null) {
                cache.put(c, glyph);
                return glyph;
            }
        }
        
        return null;
    }

    public float getTextWidth(String text, float scale, String fontKey) {
        if(!fontLoaders.containsKey(fontKey)) {
            loadFont(fontKey);
        }
        
        Map<Character, Glyph> cache = glyphCaches.get(fontKey);
        FontLoader loader = fontLoaders.get(fontKey);
        float width = 0;

        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = cache.get(c);
            if(glyph != null) {
                width += glyph.advance * scale;
                if(i < text.length() - 1) {
                    char nextChar = text.charAt(i + 1);
                    Glyph next = cache.get(nextChar);
                    if(next != null) {
                        width += loader.getKerning(glyph.glyphIndex, next.glyphIndex) * scale;
                    }
                }
            }
        }

        return width;
    }

    public void cleanup() {
        if(shaderProgram != null) shaderProgram.cleanup();
        if(vao != 0) glDeleteVertexArrays(vao);
        if(vbo != 0) glDeleteBuffers(vbo);
        if(ebo != 0) glDeleteBuffers(ebo);
        
        for(Atlas atlas : atlases.values()) {
            if(atlas != null) atlas.cleanup();
        }
        for(FontLoader loader : fontLoaders.values()) {
            if(loader != null) loader.cleanup();
        }
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics("arial");
    }

    public FontMetrics getFontMetrics(String fontKey) {
        if(!fontLoaders.containsKey(fontKey)) {
            loadFont(fontKey);
        }
        return fontLoaders.get(fontKey).getFontMetrics();
    }

    public float getTextWidth(String text, float scale) {
        return getTextWidth(text, scale, "arial");
    }
}