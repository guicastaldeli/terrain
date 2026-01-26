package main.com.app.root.mesh;
import main.com.app.root._shaders.ShaderProgram;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;


public class SpriteRenderer {
    public static SpriteRenderer instance;
    private boolean initialized = false;
    private ShaderProgram shaderProgram;

    private final Map<String, Sprite> spriteCache;
    private final Map<Integer, SpriteBatch> spriteBatches;
    
    private int vao;
    private int vbo;
    private int ebo;

    private SpriteRenderer() {
        this.spriteCache = new HashMap<>();
        this.spriteBatches = new HashMap<>();
        init();
    }

    public static SpriteRenderer getInstance() {
        if(instance == null) instance = new SpriteRenderer();
        return instance;
    }

    private void init() {
        if(initialized) return;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 8 * 6 * 4 * 100, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 8 * 4, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * 4, 2 * 4);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 4, GL_FLOAT, false, 8 * 4, 4 * 4);
        glEnableVertexAttribArray(2);

        int[] indices = new int[6 * 100];
        for(int i = 0; i < 100; i++) {
            int base = i * 6;
            int vertexBase = i * 4;
            indices[base] = vertexBase;
            indices[base+1] = vertexBase + 1;
            indices[base+2] = vertexBase + 2;
            indices[base+3] = vertexBase + 2;
            indices[base+4] = vertexBase + 3;
            indices[base+5] = vertexBase;
        }

        ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length * 4);
        for(int i : indices) indicesBuffer.putInt(i);
        indicesBuffer.flip();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        glBindVertexArray(0);
        initialized = true;
    }

    /**
     * Create Sprite from Mesh
     */
    public Sprite createSpriteFromMesh(
        MeshRenderer meshRenderer,
        String spriteKey,
        int spriteSize,
        float rotationAngle
    ) {
        if(spriteCache.containsKey(spriteKey)) {
            return spriteCache.get(spriteKey);
        }

        int fbo = glGenFramebuffers();
        int rbo = glGenRenderbuffers();
        int texId = glGenTextures();

        try {
            glBindFramebuffer(GL_FRAMEBUFFER, fbo);

            glBindTexture(GL_TEXTURE_2D, texId);
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                spriteSize, spriteSize,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                (ByteBuffer) null
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glBindRenderbuffer(GL_RENDERBUFFER, rbo);
            glRenderbufferStorage(
                GL_RENDERBUFFER,
                GL_DEPTH_COMPONENT,
                spriteSize, spriteSize
            );
            glFramebufferTexture2D(
                GL_FRAMEBUFFER,
                GL_COLOR_ATTACHMENT0,
                GL_TEXTURE_2D,
                texId,
                0
            );
            if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Framebuffer not complete! :(");
            }

            glViewport(0, 0, spriteSize, spriteSize);
            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glEnable(GL_DEPTH_TEST);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(-2, 2, -2, 2, 0.1, 10);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            glTranslatef(0, 0, -5);
            glRotatef(rotationAngle, 0, 1, 0);
            glRotatef(-30, 1, 0, 0);

            if(meshRenderer != null) meshRenderer.render(0);

            ByteBuffer buffer = BufferUtils.createByteBuffer(spriteSize * spriteSize * 4);
            glReadPixels(
                0, 0, 
                spriteSize, spriteSize,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer
            );

            int finalTexId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, finalTexId);
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA,
                spriteSize, spriteSize,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                buffer
            );
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            Sprite sprite = new Sprite(
                finalTexId, 
                spriteSize, spriteSize, 
                spriteKey
            );

            return sprite;
        } finally {
            glDeleteTextures(texId);
            glDeleteFramebuffers(fbo);
            glDeleteRenderbuffers(rbo);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    /**
     * Add Sprite
     */
    public void addSprite(SpriteInstance spriteInstance) {
        SpriteBatch batch = spriteBatches.computeIfAbsent(spriteInstance.layer, k -> new SpriteBatch(spriteInstance.layer));
        String instanceKey = spriteInstance.sprite.key + "_" + System.identityHashCode(spriteInstance);
        batch.sprites.put(instanceKey, spriteInstance);
    }

    /**
     * Remove Sprite
     */
    public void removeSprite(SpriteInstance spriteInstance) {
        SpriteBatch batch = spriteBatches.get(spriteInstance.layer);
        if(batch != null) {
            String instanceKey = spriteInstance.sprite.key + "_" + System.identityHashCode(spriteInstance);
            batch.sprites.remove(instanceKey);
        }
    }

    /**
     * Render All
     */
    public void renderAll(
        ShaderProgram shaderProgram,
        int screenWidth,
        int screenHeight
    ) {
        if(!initialized || spriteBatches.isEmpty()) return;

        boolean depthTest = glGetBoolean(GL_DEPTH_TEST);
        if(depthTest) glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.bind();
        shaderProgram.setUniform("screenSize", (float) screenHeight, (float) screenHeight);
        shaderProgram.setUniform("shaderType", 4);

        glBindVertexArray(vao);

        spriteBatches.keySet().stream()
            .sorted()
            .forEach(layer -> renderBatch(
                spriteBatches.get(layer),
                screenWidth,
                screenHeight
            ));

        glBindVertexArray(0);
        shaderProgram.unbind();

        if(depthTest) glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    /**
     * Render Batch
     */
    private void renderBatch(
        SpriteBatch batch,
        int screenWidth,
        int screenHeight
    ) {
        if(batch.sprites.isEmpty()) return;

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(batch.sprites.size() * 4 * 8);

        for(SpriteInstance instance : batch.sprites.values()) {
            float x1 = instance.x;
            float y1 = screenHeight - instance.y - instance.height;
            float x2 = instance.x + instance.width;
            float y2 = screenHeight - instance.y;
            
            float nx1 = (x1 / screenWidth) * 2 - 1;
            float ny1 = (y1 / screenHeight) * 2 - 1;
            float nx2 = (x2 / screenWidth) * 2 - 1;
            float ny2 = (y2 / screenHeight) * 2 - 1;
            
            vertexBuffer.put(nx1).put(ny1).put(0f).put(1f)
                .put(instance.color[0]).put(instance.color[1])
                .put(instance.color[2]).put(instance.color[3]);
              
            vertexBuffer.put(nx1).put(ny2).put(0f).put(0f)
                .put(instance.color[0]).put(instance.color[1])
                .put(instance.color[2]).put(instance.color[3]);
            
            vertexBuffer.put(nx2).put(ny2).put(1f).put(0f)
                .put(instance.color[0]).put(instance.color[1])
                .put(instance.color[2]).put(instance.color[3]);
            
            vertexBuffer.put(nx2).put(ny1).put(1f).put(1f)
                .put(instance.color[0]).put(instance.color[1])
                .put(instance.color[2]).put(instance.color[3]);
        }

        vertexBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        int spriteIndex = 0;
        for(SpriteInstance instance : batch.sprites.values()) {
            glBindTexture(GL_TEXTURE_2D, instance.sprite.texId);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, spriteIndex * 6 * 4L);
            spriteIndex++;
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if(!initialized) return;
        
        for(Sprite sprite : spriteCache.values()) {
            glDeleteTextures(sprite.texId);
        }
        spriteCache.clear();
        
        if(vao != 0) glDeleteVertexArrays(vao);
        if(vbo != 0) glDeleteBuffers(vbo);
        if(ebo != 0) glDeleteBuffers(ebo);
        
        spriteBatches.clear();
        initialized = false;
    }
}
