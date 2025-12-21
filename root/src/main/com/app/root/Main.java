package main.com.app.root;
import main.com.app.root._shaders.ShaderModuleData;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.screen_controller.ScreenController;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import java.util.Arrays;
import java.util.List;

public class Main {
    private final Window window;
    private Console console;
    private Tick tick;
    private Scene scene;
    private ScreenController screenController;
    private InputController inputController;
    private ShaderProgram shaderProgram;

    public Main() {
        window = new Window();
        window.init();
        tick = new Tick(window);
        console = Console.getInstance();

        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        glEnable(GL_DEPTH_TEST);

        init();
    }

    /**
     * Init
     */
    private void init() {
        loadShaders();

        scene = new Scene(window, tick, shaderProgram);
        screenController = new ScreenController(window, shaderProgram);

        console.init(this, window, screenController);
        console.setScene(scene);

        window.setScene(scene);
        window.setScreenController(screenController);

        inputController = new InputController(window);
        inputController.init(screenController);
    }

    /**
     * Load Shaders
     */
    private void loadShaders() {
        try {
            List<ShaderModuleData> shaderModules = Arrays.asList(
                new ShaderModuleData(GL_VERTEX_SHADER, "main/vert.glsl"),
                new ShaderModuleData(GL_FRAGMENT_SHADER, "main/frag.glsl")
            );
    
            shaderProgram = new ShaderProgram(shaderModules);
            System.out.println("Main shader loaded!");
        } catch(Exception err) {
            throw new RuntimeException("Failed to load shaders", err);
        }
    }

    /**
     * Update
     */
    private void update() {
        tick.update();
        
        boolean screenActive = screenController.isScreenActive(ScreenController.SCREENS.PAUSE);
        if(console.isRunning()) {
            if(!screenActive && scene.isInit()) {
                scene.update();
                inputController.update();
            }
        }
    }
    
    /**
     * Render
     */
    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        
        window.updateTitle(tick.getFps(), tick.getTickCount());
        screenController.render();

        if(console.isRunning() && scene.isInit()) {
            inputController.setPlayerInputMap(scene.getPlayerController().getInputMap());
            scene.render();  
        }
    }

    private void loop() {
        tick.resetTiming();
        
        while(!glfwWindowShouldClose(window.getWindow())) {
            update();
            render();

            glfwSwapBuffers(window.getWindow());
            glfwPollEvents();
        }
    }

    private void run() {
        loop();
        window.cleanup();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }
}
