package main.com.app.root.mesh;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.player_controller.Camera;
import main.com.app.root.player_controller.PlayerController;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class MeshRenderer {
    private final Tick tick;
    private final ShaderProgram shaderProgram;
    private MeshData meshData;
    private PlayerController playerController;

    private int vao;
    private int vbo;
    private int ebo;
    private int vertexCount;
    private float currentRotation = 0.0f;
    private Matrix4f modelMatrix = new Matrix4f();
    public boolean isDynamic = false;
    private Vector3f position = new Vector3f();
    private Vector3f scale = new Vector3f(1, 1, 1);
    private boolean hasCustomScale = false;

    private int colorVbo;
    private int texCoordsVbo;
    private int texId = -1;
    private boolean hasTex = false;
    private boolean hasColors = false;

    public MeshRenderer(Tick tick, ShaderProgram shaderProgram) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
    }

    /**
     * Set Data
     
     */
    public void setData(MeshData data) {
        this.meshData = data;
        this.isDynamic = data.isDynamic();
        createBuffers();
    }

    /**
     * Create Buffers
     */
    private void createBuffers() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        /* Vertices */
        float[] vertices = meshData.getVertices();
        if(vertices != null && vertices.length > 0) {
            vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            
            FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            memFree(vertexBuffer);
            
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);
            
            vertexCount = vertices.length / 3;
        }

        /* Colors */
        float[] colors = meshData.getColors();
        if(colors != null && colors.length > 0) {
            colorVbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, colorVbo);
            
            FloatBuffer colorBuffer = memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_DYNAMIC_DRAW);
            memFree(colorBuffer);
            
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
            
            hasColors = true;
        }

        /* Texture Coords */
        float[] texCoords = meshData.getTexCoords();
        if(texCoords != null && texCoords.length > 0) {
            texCoordsVbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, texCoordsVbo);
            
            FloatBuffer texCoordsBuffer = memAllocFloat(texCoords.length);
            texCoordsBuffer.put(texCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            memFree(texCoordsBuffer);
            
            glVertexAttribPointer(3, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(3);
        }

        /* Indices */
        int[] indices = meshData.getIndices();
        if(indices != null && indices.length > 0) {
            ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            
            IntBuffer indexBuffer = memAllocInt(indices.length);
            indexBuffer.put(indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);
            
            vertexCount = indices.length;
        }

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int error = glGetError();
        if(error != GL_NO_ERROR) {
            System.err.println("OpenGL error during buffer creation: " + error);
        }
    }

    /**
     * Color
     */
    public boolean hasColors() {
        return hasColors;
    }

    /**
     * Texture
     */
    public synchronized void setTex(int id) {
        //System.out.println("setTex() called for renderer of: " + meshData.getId());
        if(id > 0) {
            texId = id;
            hasTex = true;
            System.out.println("Texture set with ID: " + id);
        } else {
            texId = -1;
            hasTex = false;
            System.out.println("Invalid texture ID: " + id);
        }
    }

    public boolean hasTex() {
        return hasTex;
    }

    /**
     * Set Position
     */
    public void setPosition(Vector3f position) {
        this.position.set(position);
    }
    public void setPosition(float x, float y, float z) {
        modelMatrix.translation(x, y, z);
    }

    /**
     * Set Scale
     */
    public void setScale() {
        if(meshData.hasScale()) {
            float[] scale = meshData.getScale();
            modelMatrix.scale(scale[0], scale[1], scale[2]);
        }
    }
    public void setScale(Vector3f scale) {
        this.scale.set(scale);
        hasCustomScale = true;
    }
    public void setScale(float x, float y, float z) {
        modelMatrix.scale(x, y, z);
    }

    /**
     * Set Model Matrix
     */
    public void setModelMatrix(Matrix4f matrix) {
        this.modelMatrix.set(matrix);
    }

    public void setIsDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public void updateRotation() {
        if(meshData.hasRotation()) {
            float rotationSpeed = meshData.getRotationSpeed();
            float rotationAmount = rotationSpeed * tick.getDeltaTime();
            currentRotation += rotationAmount;
            if(currentRotation > 360.0f) currentRotation -= 360.0f;
        }
    }

    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public void updateColors(float[] colors) {
        if(colorVbo == 0) return;

        glBindBuffer(GL_ARRAY_BUFFER, colorVbo);

        FloatBuffer colorBuffer = memAllocFloat(colors.length);
        colorBuffer.put(colors).flip();

        glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_DYNAMIC_DRAW);
        memFree(colorBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Render
     */
    public void render(int shaderType) {
        try {            
            Camera camera = playerController.getCamera();
            
            if(!isDynamic) {
                modelMatrix
                    .identity()
                    .translate(position);
                if(hasCustomScale) {
                    modelMatrix.scale(scale);
                } else if(meshData.hasScale()) {
                    float[] dataScale = meshData.getScale();
                    modelMatrix.scale(
                        dataScale[0],
                        dataScale[1],
                        dataScale[2]
                    );
                }
            }
            
            shaderProgram.bind();
            shaderProgram.setUniform("shaderType", shaderType);

            float starBrightness = meshData.getStarBrightness();
            shaderProgram.setUniform("uStarBrightness", starBrightness);
            
            shaderProgram.setUniform("model", modelMatrix);
            shaderProgram.setUniform("view", camera.getViewMatrix());
            shaderProgram.setUniform("projection", camera.getProjectionMatrix());
            shaderProgram.setUniform("hasTex", hasTex ? 1 : 0);
            shaderProgram.setUniform("hasColors", hasColors ? 1 : 0);
            shaderProgram.setUniform("texSampler", 0);
            if(hasTex) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, texId);
            }
            
            glBindVertexArray(vao);
            
            int[] indices = meshData.getIndices();
            if(indices != null) {
                glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
            } else {
                glDrawArrays(GL_TRIANGLES, 0, vertexCount);
            }
            
            glBindVertexArray(0);
            if(hasTex) glBindTexture(GL_TEXTURE_2D, 0);
            
        } catch(Exception err) {
            err.printStackTrace();
        }
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        if(vbo != 0) glDeleteBuffers(vbo);
        if(colorVbo != 0) glDeleteBuffers(colorVbo);
        if(texCoordsVbo != 0) glDeleteBuffers(texCoordsVbo);
        if(ebo != 0) glDeleteBuffers(ebo);
        if(vao != 0) glDeleteVertexArrays(vao);
    }
}