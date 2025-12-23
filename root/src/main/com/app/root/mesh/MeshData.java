package main.com.app.root.mesh;
import java.util.HashMap;
import java.util.Map;

import main.com.app.root.utils.ColorConverter;

public class MeshData {
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
        SKYBOX
    }

    /**
     * Data Type
    */
    public enum DataType {
        VERTICES,
        INDICES,
        COLORS,
        NORMALS,
        TEX_COORDS,
        ROTATION_AXIS,
        ROTATION_SPEED
    }

    private final Map<DataType, Object> data;
    private final MeshType type;
    private final String id;
    private float[] colors;

    public MeshData(String id, MeshType type) {
        this.id = id;
        this.type = type;
        this.data = new HashMap<>();
        this.colors = null;
    }

    /**
     * Vertices
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
     * Indices
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
     * Colors
     */
    public void setColors(float[] c) {
        this.colors = c;
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
        float[] res = colors != null ?
            colors : 
            (float[]) data.get(DataType.COLORS);
        return res;
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

    /**
     * Normals
     */
    public void setNormals(float[] normals) {
        addData(DataType.NORMALS, normals);
    }

    public float[] getNormals() {
        return (float[]) data.get(DataType.NORMALS);
    }

    /**
     * Texture
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
     * Id
     */
    public String getId() {
        return id;
    }

    /**
     * Rotation
     */
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
     * Data
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
