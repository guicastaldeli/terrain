package main.com.app.root.ui;
import main.com.app.root.Upgrader;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.ui.upgrade_menu.UpgradeMenu;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import static org.lwjgl.glfw.GLFW.*;

public class UIController {
    public enum UIType {
        UPGRADE_MENU,
        ALERTS
    }

    private final Window window;
    private final ShaderProgram shaderProgram;
    private final Upgrader upgrader;
    private final Mesh mesh;

    private Map<UIType, UI> uis;
    private UIType active;
    private UI currentUI;

    private boolean isVisible = false;
    private boolean[] keyPresed = new boolean[GLFW_KEY_LAST + 1];

    public UIController(
        Window window, 
        ShaderProgram shaderProgram,
        Upgrader upgrader,
        Mesh mesh
    ) {
        this.window = window;
        this.shaderProgram = shaderProgram;
        this.upgrader = upgrader;
        this.mesh = mesh;
        UI.init(
            window, 
            shaderProgram, 
            this,
            upgrader,
            mesh
        );

        this.uis = new HashMap<>();
        
        initUI();
    }

    public boolean handleKeyPress(int key, int action) {
        //if(!isVisible || currentUI == null) return false;

        if(key == GLFW_KEY_E && action == GLFW_PRESS) {
            toggle(UIType.UPGRADE_MENU);
            return true;
        }
        if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS && active != null) {
            hide();
            return true;
        }
        if(currentUI instanceof KeyHandler) {
            return ((KeyHandler) currentUI).handleKey(key, action);
        }

        return false;
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button, int action) {
        if(!isVisible || currentUI == null) return false;
        
        if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
            String clickedElement = currentUI.checkClick((int) mouseX, (int) mouseY);
            if(clickedElement != null) {
                currentUI.handleAction(clickedElement);
                return true;
            }
        }
        
        return false;
    }

    public UIHandler getCurrentInputHandler() {
        return currentUI;
    }

    /**
     * Init UIs
     */
    private void initUI() {
        UpgradeMenu upgradeMenu = new UpgradeMenu(
            window, 
            shaderProgram, 
            this, 
            upgrader
        );
        uis.put(UIType.UPGRADE_MENU, upgradeMenu);
    }

    /**
     * Show UI
     */
    public void show(UIType uiType) {
        if(active != null && active != uiType) {
            hide();
        }

        active = uiType;
        currentUI = uis.get(uiType);

        if(currentUI != null) {
            currentUI.onShow();
            isVisible = true;
            glfwSetInputMode(window.getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    } 

    /**
     * Hide UI
     */
    public void hide() {
        if(currentUI != null) currentUI.onHide();
        active = null;
        currentUI = null;
        isVisible = false;
        glfwSetInputMode(window.getWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void toggle(UIType uiType) {
        if(active == uiType) {
            hide();
        } else {
            show(uiType);
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public UIType getActive() {
        return active;
    }

    /**
     * Get UI
     */
    public UI get(UIType uiType) {
        return uis.get(uiType);
    }
    
    /**
     * Render
     */
    public void render() {
        if(currentUI != null) {
            currentUI.render();
        }
    }

    /**
     * Update
     */
    public void update() {
        if(currentUI != null) {
            currentUI.update();
        }
    }


    public void onWindowResize(int width, int height) {
        for(UI ui : uis.values()) {
            ui.onWindowResize(width, height);
        }
    }

    public interface KeyHandler {
        boolean handleKey(int key, int action);
    }

    /**
     * Setup Mouse
     */
    public void setupMouse() {
        long windowHandle = window.getWindow();
        GLFWCursorPosCallback existingCallback = glfwSetCursorPosCallback(windowHandle, null);
        
        glfwSetCursorPosCallback(windowHandle, (windowHandle2, xPos, yPos) -> {
            if(existingCallback != null) existingCallback.invoke(windowHandle2, xPos, yPos);
            
            int mouseX = (int)xPos;
            int mouseY = (int)yPos;
            if(currentUI != null && isVisible) {
                currentUI.handleMouseMove(mouseX, mouseY);
            }
        });
    }
}
