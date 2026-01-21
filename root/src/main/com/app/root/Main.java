package main.com.app.root;
import main.com.app.root._save.DataGetter;
import main.com.app.root._save.SaveGenerator;
import main.com.app.root._save.SaveLoader;
import main.com.app.root._shaders.ShaderModuleData;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.ui.UIController;
import org.lwjgl.opengl.GL;
import java.util.Arrays;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;

public class Main {
    private final Window window;
    private final Console console;
    private final Tick tick;
    private final DataController dataController;
    private final StateController stateController;
    private DataGetter dataGetter;
    private SaveGenerator saveGenerator;
    private SaveLoader saveLoader;

    private Scene scene;
    private ScreenController screenController;
    private UIController uiController;
    private InputController inputController;
    private ShaderProgram shaderProgram;

    public Main() {
        window = new Window();
        window.init();
        tick = new Tick(window);
        console = Console.getInstance();

        dataController = new DataController();
        stateController = new StateController();

        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        DocParser.initUIRendering();

        init();
    }

    /**
     * Init
     */
    private void init() {
        loadShaders();

        /* Scene */
        scene = new Scene(
            window, 
            tick,
            dataController,
            stateController, 
            shaderProgram
        );
        
        /* Data Getter */
        dataGetter = new DataGetter(
            dataController, 
            stateController
        );
        
        /* Save Generator */
        saveGenerator = new SaveGenerator(
            dataController, 
            stateController, 
            dataGetter
        );

        /* Save Loader */
        saveLoader = new SaveLoader(
            dataController, 
            stateController, 
            dataGetter
        );

        /* Screen */
        Screen.setScene(scene);
        screenController = new ScreenController(
            window, 
            shaderProgram,
            saveGenerator,
            saveLoader,
            dataController,
            dataGetter,
            stateController
        );

        console.init(this, window, screenController);
        console.setScene(scene);

        window.setScene(scene);
        window.setScreenController(screenController);

        inputController = new InputController(window);
        inputController.init(screenController);
        scene.setInputController(inputController);
        
        scene.setDataGetter(dataGetter);
        saveGenerator.setScene(scene);
        saveLoader.setScene(scene);
        
        System.out.println("Initialization complete - Upgrader: " + 
            (scene.getUpgrader() != null ? "Present" : "NULL"));
    }

    private void startAutoSaveThread() {
        Thread autoSaveThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    if(stateController.shouldAutoSave() &&
                        stateController.getCurrentSaveId() != null &&
                        !stateController.isSaveInProgress() &&
                        !stateController.isLoadInProgress()
                    ) {
                        stateController.setSaveInProgress(true);
                        saveGenerator.save(stateController.getCurrentSaveId());
                        stateController.resetAutoSaveTimer();
                        stateController.setSaveInProgress(false);
                        System.out.println("Auto-save completed");
                    }
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e) {
                    System.err.println("Auto-save failed: " + e.getMessage());
                    stateController.setSaveInProgress(false);
                }
            }
        });
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();
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
        if(!stateController.isPaused() && !stateController.isInMenu()) {
            dataController.incrementPlayTime(1);
        }
        
        inputController.update();
        
        if(scene.isInit() && 
            !stateController.isPaused() && 
            !stateController.isInMenu()
        ) {
        }
        scene.update();
    }
    
    /**
     * Render
     */

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        
        window.updateTitle(
            tick.getFps(), 
            tick.getTickCount(),
            tick.getTimeCycle().getFormattedTime(),
            tick.getTimeCycle().getCurrentTimePeriod()
        );

        if(scene.isInit()) {
            inputController.setPlayerInputMap(scene.getPlayerController().getInputMap());
            scene.render();  
            screenController.render();
            scene.getUIController().render();
        } else {
            screenController.render();
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
        DocParser.cleanup();
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.run();
    }
}
