// 
// ** This is a Color Converter to
// covert RGB/HEX to Floats to work
// with GLSL color format...
//

package main.com.app.root.utils;

public class ColorConverter {
    /**
     * RGB
     */
    public static float[] rgbToFloat(
        int r, 
        int g,
        int b,
        int a
    ) {
        return new float[] {
            r / 255.0f,
            g / 255.0f,
            b / 255.0f,
            a / 255.0f
        };
    }

    public static float[] rgbToFloat(
        int r,
        int b,
        int g
    ) {
        return rgbToFloat(r, b, g, 255);
    }

    /**
     * HEX
     */
    public static float[] hexToFloat(String hex) {
        if(hex.startsWith("#")) hex = hex.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int b = Integer.parseInt(hex.substring(2, 4), 16);
        int g = Integer.parseInt(hex.substring(4, 6), 16);
        int a = hex.length() > 6 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
        return rgbToFloat(r, b, g, a);
    }
}
