package main.com.app.root.mesh;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.lightning.LightningRenderer;
import main.com.app.root.player.Camera;
import main.com.app.root.player.PlayerController;
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
    private EnvController envController;
    private MeshData meshData;
    private PlayerController playerController;
    private LightningRenderer lightningRenderer;
    private Camera camera;

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
    private int normalVbo;
    private int texCoordsVbo;
    private int texId = -1;
    private boolean hasTex = false;
    private boolean hasColors = false;

    public MeshRenderer(Tick tick, ShaderProgram shaderProgram) {
        this.tick = tick;
        this.shaderProgram = shaderProgram;
    }

    public void setLightningRenderer(LightningRenderer lightningRenderer) {
        this.lightningRenderer = lightningRenderer;
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
     * Set Env Controller
     */
    public void setEnvController(EnvController envController) {
        this.envController = envController;
    }

    private void checkGLError(String location) {
        int error = glGetError();
        if(error != GL_NO_ERROR) {
            System.err.println("OpenGL error at " + location + ": " + error + 
                " (mesh: " + (meshData != null ? meshData.getId() : "null") + ")");
        }
    }

    /**
     * Create Buffers
     * 
     */
    private void createBuffers() {
        while(glGetError() != GL_NO_ERROR);
        
        vao = glGenVertexArrays();
        checkGLError("after glGenVertexArrays");
        
        glBindVertexArray(vao);
        checkGLError("after glBindVertexArray");

        /* Vertices */
        float[] vertices = meshData.getVertices();
        if(vertices != null && vertices.length > 0) {
            vbo = glGenBuffers();
            checkGLError("after glGenBuffers (vertices)");
            
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            checkGLError("after glBindBuffer (vertices)");
            
            FloatBuffer vertexBuffer = memAllocFloat(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
            checkGLError("after glBufferData (vertices)");
            memFree(vertexBuffer);
            
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            checkGLError("after glVertexAttribPointer (vertices)");
            
            glEnableVertexAttribArray(0);
            checkGLError("after glEnableVertexAttribArray (vertices)");
            
            vertexCount = vertices.length / 3;
        }

        /* Normals */
        float[] normals = meshData.getNormals();
        if(normals != null && normals.length > 0) {
            normalVbo = glGenBuffers();
            checkGLError("after glGenBuffers (normals)");
            
            glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
            checkGLError("after glBindBuffer (normals)");
            
            FloatBuffer normalBuffer = memAllocFloat(normals.length);
            normalBuffer.put(normals).flip();
            glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
            checkGLError("after glBufferData (normals)");
            memFree(normalBuffer);
            
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            checkGLError("after glVertexAttribPointer (normals)");
            
            glEnableVertexAttribArray(1);
            checkGLError("after glEnableVertexAttribArray (normals)");
        }

        /* Colors */
        float[] colors = meshData.getColors();
        if(colors != null && colors.length > 0) {
            colorVbo = glGenBuffers();
            checkGLError("after glGenBuffers (colors)");
            
            glBindBuffer(GL_ARRAY_BUFFER, colorVbo);
            checkGLError("after glBindBuffer (colors)");
            
            FloatBuffer colorBuffer = memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_DYNAMIC_DRAW);
            checkGLError("after glBufferData (colors)");
            memFree(colorBuffer);
            
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
            checkGLError("after glVertexAttribPointer (colors)");
            
            glEnableVertexAttribArray(2);
            checkGLError("after glEnableVertexAttribArray (colors)");
            
            hasColors = true;
        }

        /* Texture Coords */
        float[] texCoords = meshData.getTexCoords();
        if(texCoords != null && texCoords.length > 0) {
            texCoordsVbo = glGenBuffers();
            checkGLError("after glGenBuffers (texCoords)");
            
            glBindBuffer(GL_ARRAY_BUFFER, texCoordsVbo);
            checkGLError("after glBindBuffer (texCoords)");
            
            FloatBuffer texCoordsBuffer = memAllocFloat(texCoords.length);
            texCoordsBuffer.put(texCoords).flip();
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            checkGLError("after glBufferData (texCoords)");
            memFree(texCoordsBuffer);
            
            glVertexAttribPointer(3, 2, GL_FLOAT, false, 0, 0);
            checkGLError("after glVertexAttribPointer (texCoords)");
            
            glEnableVertexAttribArray(3);
            checkGLError("after glEnableVertexAttribArray (texCoords)");
        }

        /* Indices */
        int[] indices = meshData.getIndices();
        if(indices != null && indices.length > 0) {
            ebo = glGenBuffers();
            checkGLError("after glGenBuffers (indices)");
            
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            checkGLError("after glBindBuffer (indices)");
            
            IntBuffer indexBuffer = memAllocInt(indices.length);
            indexBuffer.put(indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            checkGLError("after glBufferData (indices)");
            memFree(indexBuffer);
            
            vertexCount = indices.length;
        }

        glBindVertexArray(0);
        checkGLError("after unbind VAO");
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        checkGLError("after unbind VBO");
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
        if(id > 0) {
            texId = id;
            hasTex = true;
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

    /**
     * Player Controller
     */
    public void setPlayerController(PlayerController playerController) {
        this.playerController = playerController;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    /**
     * Camera
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
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
                 
            Camera renderCamera;
            if(playerController != null) {
                renderCamera = playerController.getCamera();
            } else if(camera != null) {
                renderCamera = camera;
            } else {
                throw new IllegalStateException("No camera available for rendering");
            }
            
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
            if(shaderType == 0 && lightningRenderer != null) {
                lightningRenderer.updateShaderUniforms();
            }
            
            float starBrightness = meshData.getStarBrightness();
            shaderProgram.setUniform("uStarBrightness", starBrightness);
            
            shaderProgram.setUniform("model", modelMatrix);
            shaderProgram.setUniform("view", renderCamera.getViewMatrix());
            shaderProgram.setUniform("projection", renderCamera.getProjectionMatrix());
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

    /**
     * 
     * Skybox Color / Fog
     * 
     */
    public void applyFog() {
        float[] skyColor = getSkyboxColor();
        Vector3f fogColor = new Vector3f(skyColor[0], skyColor[1], skyColor[2]);

        shaderProgram.setUniform("uRenderDistance", Camera.FOG);
        shaderProgram.setUniform("uFogColor", fogColor.x, fogColor.y, fogColor.z);
        shaderProgram.setUniform("uFogDensity", 1.0f);
    }

    public float[] getSkyboxColor() {
        if(envController == null) return new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        
        try {
            Object skyboxInstance = envController.getEnv(EnvData.SKYBOX);
            Object skyboxMesh = EnvCall.callReturn(skyboxInstance, "getMesh");
            
            if(skyboxMesh != null) {
                Object colorObj = EnvCall.callReturn(skyboxMesh, "getCurrentSkyColor");
                if(colorObj instanceof float[]) return (float[]) colorObj;
            }
        } catch(Exception e) {
            System.err.println("Failed to get skybox color: " + e.getMessage());
        }
        
        return new float[]{0.5f, 0.5f, 0.5f, 1.0f};
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        if(vbo != 0) glDeleteBuffers(vbo);
        if(normalVbo != 0) glDeleteBuffers(normalVbo);
        if(colorVbo != 0) glDeleteBuffers(colorVbo);
        if(texCoordsVbo != 0) glDeleteBuffers(texCoordsVbo);
        if(ebo != 0) glDeleteBuffers(ebo);
        if(vao != 0) glDeleteVertexArrays(vao);
    }
}