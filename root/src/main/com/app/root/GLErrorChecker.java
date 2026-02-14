package main.com.app.root;

public class GLErrorChecker {
    public static void check(String location) {
        int error;
        while((error = org.lwjgl.opengl.GL11.glGetError()) != org.lwjgl.opengl.GL11.GL_NO_ERROR) {
            System.err.println("OpenGL Error at " + location + ": " + getErrorString(error));
        }
    }
    
    private static String getErrorString(int error) {
        switch (error) {
            case org.lwjgl.opengl.GL11.GL_INVALID_ENUM: return "GL_INVALID_ENUM";
            case org.lwjgl.opengl.GL11.GL_INVALID_VALUE: return "GL_INVALID_VALUE";
            case org.lwjgl.opengl.GL11.GL_INVALID_OPERATION: return "GL_INVALID_OPERATION";
            case org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW: return "GL_STACK_OVERFLOW";
            case org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW: return "GL_STACK_UNDERFLOW";
            case org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY: return "GL_OUT_OF_MEMORY";
            default: return "Unknown error: " + error;
        }
    }
}