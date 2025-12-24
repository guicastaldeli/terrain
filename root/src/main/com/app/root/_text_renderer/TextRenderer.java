package main.com.app.root._text_renderer;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
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
    private FontLoader fontLoader;
    private Atlas atlas;

    private int vao;
    private int vbo;
    private int ebo;

    private Map<Character, Glyph> glyphCache;
    private static final int ATLAS_SIZE = 300;
    private static final int PADDING = 1;

    public int currentWidth;
    public int currentHeight;

    public TextRenderer(
        Window window,
        ShaderProgram shaderProgram,
        String fontPath, 
        float fontSize,
        int screenWidth,
        int screenHeight
    ) throws Exception {
        this.window = window;
        this.shaderProgram = shaderProgram;
        this.currentWidth = screenWidth;
        this.currentHeight = screenHeight;
        this.fontLoader = new FontLoader(fontPath, fontSize);
        this.atlas = new Atlas(ATLAS_SIZE, ATLAS_SIZE);
        this.glyphCache = new HashMap<>();

        setupBuffers();
        
        preloadChars();
        
        //atlas.saveAtlasAsPPM("atlas_debug.ppm");
        //System.out.println("Atlas saved");
    }

    public void updateScreenSize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
    }

    /**
     * Setup Buffers
     */
    private void setupBuffers() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * 4, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(3, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
        glEnableVertexAttribArray(3);

        int[] indices = {
            0, 1, 2, 
            2, 3, 0
        };
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices);
        indicesBuffer.flip();
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindVertexArray(0);
    }

    /**
     * Preload Chars
     */
    private void preloadChars() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (char c : chars.toCharArray()) {
            Glyph glyph = loadGlyphToAtlas(c);
        }
    }

    /**
     * Render Text
     */
    public void renderText(
        String text,
        int x,
        int y,
        float scale,
        float[] color
    ) {
        if(text == null || text.isEmpty()) {
            //System.out.println("TextRenderer: Empty text, skipping");
            return;
        }
        
        boolean depthTest = glGetBoolean(GL_DEPTH_TEST);
        if(depthTest) glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.bind();
        shaderProgram.setUniform("shaderType", 1);
        shaderProgram.setUniform(
            "screenSize",
            (float)currentWidth,
            (float)currentHeight
        );
        shaderProgram.setUniform(
            "textColor",
            color[0],
            color[1],
            color[2]
        );
        shaderProgram.setUniform("texSampler", 0);

        int colorLoc = glGetUniformLocation(shaderProgram.programId, "textColor");
        int textureLoc = glGetUniformLocation(shaderProgram.programId, "textureSampler");

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, atlas.getTextureId());
        
        int[] boundTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, boundTexture);
        glBindVertexArray(vao);

        float cursorX = x;
        FontMetrics fontMetrics = fontLoader.getFontMetrics();
        float baseline = y + fontMetrics.ascent * scale;
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = loadGlyphToAtlas(c);
            if(glyph == null) {
                System.out.println("  Glyph not found!");
                continue;
            }

            float xPos = cursorX + glyph.leftSideBearing * scale;
            float yPos = baseline + glyph.yOffset;
            float w = glyph.bitmapWidth * scale;
            float h = glyph.bitmapHeight * scale;
            updateQuad(xPos, yPos, w, h, glyph);
            
            int error = glGetError();
            if (error != GL_NO_ERROR) {
                System.err.println("  OpenGL error before draw: " + error);
            }
            
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            
            error = glGetError();
            if (error != GL_NO_ERROR) {
                System.err.println("  OpenGL error after draw: " + error);
            }

            cursorX += glyph.advance * scale;
            if(i < text.length() - 1) {
                char nextChar = text.charAt(i + 1);
                Glyph nextGlyph = glyphCache.get(nextChar);
                
                if(nextGlyph != null) {
                    float kerning = fontLoader.getKerning(
                        glyph.glyphIndex,
                        nextGlyph.glyphIndex
                    );
                    cursorX += kerning * scale;
                }
            }
        }

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);

        if(depthTest) glEnable(GL_DEPTH_TEST);
        shaderProgram.unbind();
        glDisable(GL_BLEND);
    }

    private void updateQuad(
        float x,
        float y,
        float w,
        float h,
        Glyph glyph
    ) {
        float[] vertices = {
            x,     y + h, glyph.texCoordX, glyph.texCoordY + glyph.texHeight,
            x,     y,     glyph.texCoordX, glyph.texCoordY,
            x + w, y,     glyph.texCoordX + glyph.texWidth, glyph.texCoordY,
            x + w, y + h, glyph.texCoordX + glyph.texWidth, glyph.texCoordY + glyph.texHeight
        };
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
        vertexBuffer.put(vertices);
        vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private Glyph loadGlyphToAtlas(char c) {
        if(glyphCache.containsKey(c)) {
            return glyphCache.get(c);
        }
        
        boolean success = atlas.addGlyph(fontLoader, c, PADDING);
        if(success) {
            Glyph glyph = atlas.getGlyph(c);
            if(glyph != null) {
                glyphCache.put(c, glyph);
                return glyph;
            } else {
                System.out.println("ERROR: Atlas.getGlyph returned null for '" + c + "'");
            }
        } else {
            System.out.println("ERROR: Atlas.addGlyph failed for '" + c + "'");
        }
        
        return null;
    }

    public float getTextWidth(String text, float scale) {
        float width = 0;

        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            Glyph glyph = glyphCache.get(c);
            if(glyph != null) {
                width += glyph.advance * scale;
                if(i < text.length() - 1) {
                    char nextChar = text.charAt(i + 1);
                    Glyph next = glyphCache.get(nextChar);
                    if(next != null) {
                        width += fontLoader.getKerning(
                            glyph.glyphIndex,
                            next.glyphIndex
                        ) * scale;
                    }
                }
            }
        }

        return width;
    }

    public FontMetrics getFontMetrics() {
        return fontLoader.getFontMetrics();
    }

    public void cleanup() {
        if(shaderProgram != null) shaderProgram.cleanup();
        if(vao != 0) glDeleteVertexArrays(vao);
        if(vbo != 0) glDeleteBuffers(vbo);
        if(ebo != 0) glDeleteBuffers(ebo);
        if(atlas != null) atlas.cleanup();
        if(fontLoader != null) fontLoader.cleanup();
    }
}
