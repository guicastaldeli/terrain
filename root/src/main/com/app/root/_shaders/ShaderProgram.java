package main.com.app.root._shaders;
import java.io.IOException;
import java.util.*;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    public final int programId;
    private Map<String, Integer> uniformLocations = new HashMap<>();
    private Set<String> activeUniformNames = new HashSet<>();

    public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
        programId = glCreateProgram();
        if(programId == 0) throw new RuntimeException("Could not create Shader");

        List<Integer> shaderModules = new ArrayList<>();
        Map<Integer, StringBuilder> shaderSources = new HashMap<>();
        sortTypes(shaderModuleDataList);

        for(ShaderModuleData data : shaderModuleDataList) {
            try {
                String content = ShaderLoader.load(data.getFile());
                shaderSources
                    .computeIfAbsent(data.getType(), k -> new StringBuilder())
                    .append(content).append("\n");
            } catch(IOException err) {
                throw new RuntimeException("Failed to load shader: " + data.getFile(), err);
            }
        }

        for(Map.Entry<Integer, StringBuilder> entry : shaderSources.entrySet()) {
            int shaderType = entry.getKey();
            String source = entry.getValue().toString();

            int shaderId = createShader(source, shaderType);
            glAttachShader(programId, shaderId);
            shaderModules.add(shaderId);
        }

        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Error linking code: " + glGetProgramInfoLog(programId, 1024));
        }
        
        storeActiveUniformNames();
        cacheAllUniformLocations();
        //printProgramInfo();
        
        shaderModules.forEach(sId -> glDeleteShader(sId));
    }

    private void sortTypes(List<ShaderModuleData> data) {
        data.sort((a, b) -> {
            if(a.getType() == GL_VERTEX_SHADER && b.getType() != GL_VERTEX_SHADER) return 1;
            if(a.getType() != GL_VERTEX_SHADER && b.getType() == GL_VERTEX_SHADER) return -1;
            return 0;
        });
    }

    /**
     * Create Shader
     */
    private int createShader(String content, int type) {
        int id = glCreateShader(type);
        if(id == 0) throw new RuntimeException("Error creating shader. Type: " + type);

        glShaderSource(id, content);
        glCompileShader(id);
        if(glGetShaderi(id, GL_COMPILE_STATUS) == 0) {
            String error = glGetShaderInfoLog(id, 1024);
            System.err.println("Shader compilation error: " + error);
            //System.err.println("Shader source: " + content);
            throw new RuntimeException("Error compiling shader: " + error);
        }

        return id;
    }

    /**
     * Uniform Location
     */
    public void setUniform(String name, Matrix4f matrix) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            float[] matrixArr = new float[16];
            matrix.get(matrixArr);
            glUniformMatrix4fv(loc, false, matrixArr);
        }
    }
    public void setUniform(String name, float x, float y) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            glUniform2f(loc, x, y);
        }
    }
    public void setUniform(String name, float x, float y, float z) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            glUniform3f(loc, x, y, z);
        }
    }
    public void setUniform(String name, float[] matrix) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            glUniformMatrix4fv(loc, false, matrix);
        }
    }
    public void setUniform(String name, int value) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            glUniform1i(loc, value);
        }
    }
    public void setUniform(String name, float value) {
        int loc = getUniformLocation(name);
        if(loc != -1) {
            glUniform1f(loc, value);
        }
    }

    public int getUniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, k -> {
            int location = glGetUniformLocation(programId, name);
            if(location == -1 && !isUniformActive(name)) {
                //System.err.println("Warning: Uniform '" + name + "' not found in shader!");
            } else if(location == -1 && isUniformActive(name)) {
                System.err.println("Warning: Uniform '" + name + "' is active but location is -1");
            }
            return location;
        });
    }

    private void cacheAllUniformLocations() {
        for(String uniformName : activeUniformNames) {
            int location = glGetUniformLocation(programId, uniformName);
            uniformLocations.put(uniformName, location);
            if(location == -1) {
                System.err.println("Uniform '" + uniformName + "' is active but location is -1");
            }
        }
    }

    private void storeActiveUniformNames() {
        IntBuffer count = BufferUtils.createIntBuffer(1);
        glGetProgramiv(programId, GL_ACTIVE_UNIFORMS, count);

        for(int i = 0; i < count.get(0); i++) {
            IntBuffer size = BufferUtils.createIntBuffer(1);
            IntBuffer type = BufferUtils.createIntBuffer(1);
            String name = glGetActiveUniform(programId, i, 256, size, type);
            activeUniformNames.add(name);
        }
    }

    private boolean isUniformActive(String name) {
        return activeUniformNames.contains(name);
    }

    /**
     * Validate
     */
    public boolean validate() {
        glValidateProgram(programId);
        IntBuffer status = BufferUtils.createIntBuffer(1);
        glGetProgramiv(programId, GL_VALIDATE_STATUS, status);
        
        if(status.get(0) == GL_FALSE) {
            int logLength = glGetProgrami(programId, GL_INFO_LOG_LENGTH);
            if(logLength > 0) {
                String log = glGetProgramInfoLog(programId);
                System.err.println("Shader program validation failed: " + log);
            }
            return false;
        }
        return true;
    }

    public int getProgramId() {
        return programId;
    }

    private void printProgramInfo() {
        System.out.println("=== Shader Program Info ===");
        
        IntBuffer count = BufferUtils.createIntBuffer(1);
        glGetProgramiv(programId, GL_ACTIVE_UNIFORMS, count);
        System.out.println("Active uniforms: " + count.get(0));
        
        for(int i = 0; i < count.get(0); i++) {
            IntBuffer size = BufferUtils.createIntBuffer(1);
            IntBuffer type = BufferUtils.createIntBuffer(1);
            String name = glGetActiveUniform(programId, i, 256, size, type);
            System.out.println("  Uniform " + i + ": " + name + " (type: " + type.get(0) + ", size: " + size.get(0) + ")");
        }
        
        glGetProgramiv(programId, GL_ACTIVE_ATTRIBUTES, count);
        System.out.println("Active attributes: " + count.get(0));
        
        for(int i = 0; i < count.get(0); i++) {
            IntBuffer size = BufferUtils.createIntBuffer(1);
            IntBuffer type = BufferUtils.createIntBuffer(1);
            String name = glGetActiveAttrib(programId, i, 256, size, type);
            System.out.println("  Attribute " + i + ": " + name + " (type: " + type.get(0) + ", size: " + size.get(0) + ")");
        }
        
        System.out.println("===========================");
    }

    public void bind() {
        glUseProgram(programId);

        int error = glGetError();
        if(error != GL_NO_ERROR) {
            System.err.println("OpenGL error during shader bind: " + error);
        }
        if(!validate()) {
            System.err.println("Shader program validation failed during bind!");
        }
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if(programId != 0) glDeleteProgram(programId);
        uniformLocations.clear();
    }
}