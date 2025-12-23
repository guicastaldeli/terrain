package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenController;
import main.com.app.root.screen_controller.ScreenElement;
import java.util.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class LoadSaveMenu extends Screen {
    public static final String MENU_PATH = DIR + "main/load_save_menu.xml";

    private final MainScreen mainScreen;
    public List<ScreenElement> saveMenuEl = new ArrayList<>();
    public boolean showSaveMenu = false;

    public LoadSaveMenu(MainScreen mainScreen) {
        super(MENU_PATH, "load_save_menu");
        this.mainScreen = mainScreen;
        this.active = false;
    }

    /**
     * Show
     */
    public void show() {
        this.active = true;
        screenController.switchTo(ScreenController.SCREENS.LOAD_SAVE_MENU);
        updateSaveSlots();
    }

    /**
     * Hide
     */
    public void hide() {
        hide(true);
    }

    public void hide(boolean returnToMain) {
        this.active = false;
        if(returnToMain) {
            screenController.switchTo(ScreenController.SCREENS.MAIN);
        }
    }
    
    public void updateSaveSlots() {
        if(screenData == null) return;
        
        List<ScreenElement> newElements = new ArrayList<>();
        for(ScreenElement el : screenData.elements) {
            boolean isSaveElement = 
                el.id.startsWith("save_name_") || 
                el.id.startsWith("play_time_") || 
                el.id.startsWith("last_played_") || 
                el.id.startsWith("load_") || 
                el.id.startsWith("delete_") ||
                el.id.equals("noSavesLabel");
            if(!isSaveElement) {
                newElements.add(el);
            }
        }
        
        screenData.elements = newElements;
        if(mainScreen.availableSaves.isEmpty()) {
            ScreenElement noSavesLabel = new ScreenElement(
                "label",
                "noSavesLabel",
                "No save games found",
                50, 300,
                1.0f,
                new float[]{0.8f, 0.8f, 0.8f},
                ""
            );
            noSavesLabel.visible = true;
            screenData.elements.add(noSavesLabel);
        } else {
            int startY = 300;
            int slotHeight = 100;
            int infoSpacing = 60;
            
            for(int i = 0; i < mainScreen.availableSaves.size(); i++) {
                SaveInfo save = mainScreen.availableSaves.get(i);
                int baseY = startY + (i * slotHeight);
                
                /* Save Name */
                ScreenElement saveNameLabel = new ScreenElement(
                    "label",
                    "save_name_" + save.saveId,
                    save.saveName,
                    50, baseY,
                    1.0f,
                    new float[]{1.0f, 1.0f, 1.0f},
                    ""
                );
                screenData.elements.add(saveNameLabel);
                
                /* Play Time */
                ScreenElement playTimeLabel = new ScreenElement(
                    "label",
                    "play_time_" + save.saveId,
                    "Play Time: " + save.playTime,
                    50, baseY + infoSpacing,
                    1.0f,
                    new float[]{0.8f, 0.8f, 0.8f},
                    ""
                );
                screenData.elements.add(playTimeLabel);
                
                /* Last Played */
                ScreenElement lastPlayedLabel = new ScreenElement(
                    "label",
                    "last_played_" + save.saveId,
                    "Last Played: " + save.lastPlayed,
                    50, baseY + (infoSpacing * 2),
                    1.0f,
                    new float[]{0.7f, 0.7f, 0.7f},
                    ""
                );
                screenData.elements.add(lastPlayedLabel);
                
                /* Load Button */
                ScreenElement loadButton = new ScreenElement(
                    "button",
                    "load_" + save.saveId,
                    "Load",
                    800, baseY + 60,
                    1.0f,
                    new float[]{0.2f, 0.8f, 0.2f},
                    "load_" + save.saveId
                );
                screenData.elements.add(loadButton);
                
                /* Delete Button */
                ScreenElement deleteButton = new ScreenElement(
                    "button",
                    "delete_" + save.saveId,
                    "Delete",
                    950, baseY + 60,
                    1.0f,
                    new float[]{0.8f, 0.2f, 0.2f},
                    "delete_" + save.saveId
                );
                screenData.elements.add(deleteButton);
            }
        }
    }

    @Override
    public void handleAction(String action) {
        if(action.startsWith("load_") || action.startsWith("delete_")) {
            String[] parts = action.split("_", 2);
            if(parts.length < 2) return;
            
            String actionType = parts[0];
            String saveId = parts[1];
            
            switch(actionType) {
                case "load":
                    mainScreen.mainScreenAction.load(saveId);
                    hide(false);
                    break;
                case "delete":
                    mainScreen.mainScreenAction.deleteSave(saveId);
                    mainScreen.refreshSaveList();
                    updateSaveSlots();
                    break;
            }
            return;
        }
        switch(action) {
            case "back":
                hide();
                break;
        }
    }

    @Override
    public void handleKeyPress(int key, int action) {
        if(key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            hide();
        }
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void onWindowResize(int width, int height) {
        if(getTextRenderer() != null) {
            getTextRenderer().updateScreenSize(width, height);
        }
        
        try {
            this.screenData = DocParser.parseScreen(
                MENU_PATH,
                width,
                height
            );
            
            if(active) updateSaveSlots();
        } catch (Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }
}
