package main.com.app.root;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import main.com.app.root.screen.ScreenController;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public long window;
    private Scene scene;
    private ScreenController screenController;

    private int WIDTH = 1280;
    private int HEIGHT = 720;
    private List<Runnable> resizeCallbacks = new ArrayList<>();
    public final String WINDOW_TITLE = "build";

    public long getWindow() {
        return window;
    }

    public int getWidth() {
        return WIDTH;
    } 

    public int getHeight() {
        return HEIGHT;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setScreenController(ScreenController screenController) {
        this.screenController = screenController;
    }

    public void addResizeCallback(Runnable cb) {
        resizeCallbacks.add(cb);
    }

    /**
     * Set Window
     */
    private void set() {
        window = glfwCreateWindow(
            WIDTH, HEIGHT, 
            WINDOW_TITLE, 
            NULL, 
            NULL
        );
        if(window == NULL) {
            throw new RuntimeException("Failed to create GL window!");
        }
    }

    public void updateTitle(
        int fps, 
        int tickCount, 
        String time,
        TimeCycle.TimePeriod period
    ) {
        String title = 
            WINDOW_TITLE + 
            " / " 
            + "FPS: " + fps + 
            " / " + 
            "Tick: " + tickCount +
            " / " +
            "Time: " + time + " (" + period + ")";
        glfwSetWindowTitle(window, title);
    }

    /**
     * Memory Stack
     */
    private void stack() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                window,
                (vidMode.width() - pWidth.get(0)) / 2,
                (vidMode.height() - pHeight.get(0)) / 2 
            );
        }
    }

    public float getAspectRatio() {
        try(MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(window, width, height);
            return (float) width.get(0) / (float) height.get(0);
        }
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if(!glfwInit()) throw new IllegalStateException("Unable to init GL!");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        set();
        stack();
        setupResizeCallback();
        
        int vSyncEnabled = 0;
        glfwMakeContextCurrent(window);
        glfwSwapInterval(vSyncEnabled);
        glfwShowWindow(window);
    }

    private void setupResizeCallback() {
        glfwSetFramebufferSizeCallback(window, (windowHandle, newWidth, newHeight) -> {
            WIDTH = newWidth;
            HEIGHT = newHeight;
            glViewport(0, 0, newWidth, newHeight);

            if(scene != null && scene.getPlayerController() != null) {
                main.com.app.root.player.Camera camera = scene.getPlayerController().getCamera();
                if (camera != null) {
                    camera.setAspectRatio(getAspectRatio());
                }
            }
            
            for(Runnable cb : resizeCallbacks) {
                try {
                    cb.run();
                } catch(Exception err) {
                    System.err.println("Error in resize callback: " + err.getMessage());
                }
            }
        });
    }

    public void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
