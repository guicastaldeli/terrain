package main.com.app.root.utils;
import org.joml.Vector3f;

// 
// ** This is a Color Converter to
// covert HEX to Vec3 format...
//

public class HexToVec3 {
    /**
     * HEX to Vector3f (vec3)
     */
    public static Vector3f hexToVec3(String hex) {
        float[] rgba = ColorConverter.hexToFloat(hex);
        return new Vector3f(rgba[0], rgba[1], rgba[2]);
    }

    /**
     * HEX to vec3 array (float[3])
     */
    public static float[] hexToVec3Array(String hex) {
        float[] rgba = ColorConverter.hexToFloat(hex);
        return new float[] { rgba[0], rgba[1], rgba[2] };
    }
}
