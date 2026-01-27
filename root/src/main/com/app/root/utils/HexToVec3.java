package main.com.app.root.utils;
import org.joml.Vector3f;

// 
// ** This is a Color Converter to
// covert HEX to Vec3 format...
//

public class HexToVec3 {
    /**
     * HEX to Vec3
     */
    public static Vector3f hexToVec3(String hex) {
        float[] rgba = ColorConverter.hexToFloat(hex);
        return new Vector3f(rgba[0], rgba[1], rgba[2]);
    }

    /**
     * HEX to vec3 array 3 floats
     */
    public static float[] hexToVec3Array(String hex) {
        float[] rgba = ColorConverter.hexToFloat(hex);
        return new float[] { rgba[0], rgba[1], rgba[2] };
    }

    /**
     * HEX to rgba array 4 floats
     */
    public static float[] hexToRgbaArray(String hex) {
        return ColorConverter.hexToFloat(hex);
    }
}
