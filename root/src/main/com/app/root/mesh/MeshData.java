package main.com.app.root.mesh;
import main.com.app.root.utils.ColorConverter;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

public class MeshData {
    /**
     * Data Type
    */
    public enum DataType {
        VERTICES,
        INDICES,
        COLORS,
        POSITION,
        NORMALS,
        TEX_COORDS,
        ROTATION,
        ROTATION_AXIS,
        ROTATION_SPEED,
        SCALE
    }
    /**
     * Mesh Type
    */
    public enum MeshType {
        TRIANGLE,
        QUAD,
        CUBE,
        SPHERE,
        RECTANGLE,
        MAP,
        SKYBOX,
        CLOUDS,
        OBJ
    }

    private final Map<DataType, Object> data;
    private final MeshType type;
    private final String id;
    private boolean isDynamic = false;

    private float[] colors;
    private int shaderType = 0;
    private float starBrightness = 0.0f;

    private final MeshInstance meshInstance;

    public MeshData(String id, MeshType type) {
        this.id = id;
        this.type = type;
        this.data = new HashMap<>();
        this.colors = null;
        this.meshInstance = new MeshInstance();
    }

    public MeshInstance getMeshInstance() {
        return meshInstance;
    }

    /**
     * 
     * Vertices
     * 
     */
    public void setVertices(float[] vertices) {
        addData(DataType.VERTICES, vertices);
    }

    public float[] getVertices() {
        return (float[]) data.get(DataType.VERTICES);
    }

    public int getVertexCount() {
        float[] vertices = getVertices();
        return vertices != null ? vertices.length / 3 : 0;
    }

    /**
     * 
     * Indices
     * 
     */
    public void setIndices(int[] indices) {
        addData(DataType.INDICES, indices);
    }

    public int[] getIndices() {
        return (int[]) data.get(DataType.INDICES);
    }

    public int getIndexCount() {
        int[] indices = getIndices();
        return indices != null ? indices.length : 0;
    }

    /**
     * 
     * Colors
     * 
     */
    public void setColors(float[] c) {
        this.colors = c;
        if(c != null) addData(DataType.COLORS, c);
    }
    
    public void setColor(
        float r,
        float g,
        float b,
        float a
    ) {
        float[] c = new float[getVertexCount() * 4];
        for(int i = 0; i < c.length; i += 4) {
            c[i] = r;
            c[i+1] = g;
            c[i+2] = b;
            c[i+3] = a;
        }
        setColors(c);
    }

    public float[] getColors() {
        if(colors != null) {
            return colors;
        }
        
        Object colorsData = data.get(DataType.COLORS);
        if(colorsData instanceof float[]) {
            return (float[]) colorsData;
        }
        
        return null;
    }

    public void setColorRgb(
        int r, 
        int g, 
        int b, 
        int a
    ) {
        float[] color = ColorConverter.rgbToFloat(r, g, b, a);
        setColor(
            color[0],
            color[1],
            color[2],
            color[3]
        );
    }

    public void setColorRgb(int r, int b, int g) {
        setColorRgb(r, b, g);
    }

    public void setColorHex(String hex) {
        float[] color = ColorConverter.hexToFloat(hex);
        setColor(
            color[0],
            color[1],
            color[2],
            color[3]
        );
    }

    public void setTransparentColor(float alpha) {
        setColor(1.0f, 1.0f, 1.0f, alpha);
    }

    public void setTransparentColor(float r, float g, float b, float alpha) {
        setColor(r, g, b, alpha);
    }

    /**
     * 
     * Normals
     * 
     */
    public void setNormals(float[] normals) {
        addData(DataType.NORMALS, normals);
    }

    public float[] getNormals() {
        return (float[]) data.get(DataType.NORMALS);
    }

    /**
     * 
     * Texture
     * 
     */
    public void setTexCoords(float[] texCoords) {
        addData(DataType.TEX_COORDS, texCoords);
    }

    public float[] getTexCoords() {
        return (float[]) data.get(DataType.TEX_COORDS);
    }

    /**
     * Type
     */
    public MeshType getType() {
        return type;
    }

    /**
     * 
     * Shader Type
     * 
     */
    public void setShaderType(int type) {
        this.shaderType = type;
    }
    
    public int getShaderType() {
        return this.shaderType;
    }

    /**
     * Id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * Position
     * 
     */
    public void setPosition(Vector3f position) {
        addData(DataType.POSITION, position);
    }

    public Vector3f getPosition() {
        Object position = data.get(DataType.POSITION);
        if (position instanceof Vector3f) {
            return (Vector3f) position;
        }
        return new Vector3f(0, 0, 0);
    }

    /**
     * 
     * Rotation
     * 
     */
    public void setRotation(Vector3f rotation) {
        addData(DataType.ROTATION, rotation);
    }

    public Vector3f getRotation() {
        Object rotation = data.get(DataType.ROTATION);
        if (rotation instanceof Vector3f) {
            return (Vector3f) rotation;
        }
        return new Vector3f(0, 0, 0);
    }

    public String getRotationAxis() {
        Object axis = data.get(DataType.ROTATION_AXIS);
        return axis instanceof String ? (String) axis : null;
    }

    public float getRotationSpeed() {
        Object speed = data.get(DataType.ROTATION_SPEED);
        return speed instanceof Float ? (Float) speed : 0.0f;
    }

    public boolean hasRotation() {
        return getRotationAxis() != null && getRotationSpeed() > 0.0f;
    }

    /**
     * 
     * Star Brightness
     * 
     */
    public void setStarBrightness(float brightness) {
        this.starBrightness = brightness;
    }
    
    public float getStarBrightness() {
        return this.starBrightness;
    }

    /**
     * 
     * Scale
     * 
     */
    public void setScale(float[] scale) {
        addData(DataType.SCALE, scale);
    }

    public void setScale(float scale) {
        setScale(new float[]{scale, scale, scale});
    }

    public void setScale(float x, float y, float z) {
        setScale(new float[]{x, y, z});
    }

    public float[] getScale() {
        Object scaleData = data.get(DataType.SCALE);
        if(scaleData instanceof float[]) {
            return (float[]) scaleData;
        }
        return new float[]{ 1.0f, 1.0f, 1.0f };
    }

    public boolean hasScale() {
        return data.containsKey(DataType.SCALE);
    }

    /**
     * 
     * Dynamic
     * 
     */
    public void setIsDynamic(boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    /**
     * 
     * Data
     * 
     */
    public boolean hasData(DataType dataType) {
        return data.containsKey(dataType);
    }

    public <T> T getData(DataType dataType, Class<T> type) {
        return type.cast(data.get(dataType));
    }

    public void addData(DataType dataType, Object data) {
        this.data.put(dataType, data);
    }
}
