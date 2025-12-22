package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenElement;
import java.util.*;


public class LoadSaveMenu extends Screen {
    public static final String MENU_PATH = DIR + "main/load_save_menu.xml";

    private final MainScreen mainScreen;
    public List<ScreenElement> saveMenuEl = new ArrayList<>();
    public boolean showSaveMenu = false;

    public LoadSaveMenu(MainScreen mainScreen) {
        super(MENU_PATH, "load_save_menu");
        this.mainScreen = mainScreen;
    }
    
    @Override
    public void render() {
        updateSaveSlots();
        super.render();
    }
    
    private void updateSaveSlots() {
        if(screenData == null) return;
        
        for(ScreenElement el : screenData.elements) {
            if(!el.id.startsWith("save_label_") && 
                !el.id.startsWith("load_") && 
                !el.id.startsWith("delete_")) {
                saveMenuEl.add(el);
            }
        }
        screenData.elements = saveMenuEl;
        
        ScreenElement noSavesLabel = getElementById("noSavesLabel");
        if(noSavesLabel != null) {
            noSavesLabel.visible = mainScreen.availableSaves.isEmpty();
        }
        
        int startY = 150;
        int slotHeight = 60;
        for(int i = 0; i < mainScreen.availableSaves.size(); i++) {
            SaveInfo save = mainScreen.availableSaves.get(i);
            int yPos = startY + (i * slotHeight);
            
            /* Save Info */
            ScreenElement saveLabel = new ScreenElement(
                "label",
                "save_label_" + save.saveId,
                String.format("%s - %s - %s", 
                    save.saveName, 
                    save.playTime, 
                    save.lastPlayed),
                100, yPos,
                0.9f,
                new float[]{1.0f, 1.0f, 1.0f},
                ""
            );
            screenData.elements.add(saveLabel);
            
            /* Load Save */
            ScreenElement loadButton = new ScreenElement(
                "button",
                "load_" + save.saveId,
                "Load",
                600, yPos,
                1.0f,
                new float[]{0.2f, 0.8f, 0.2f},
                "load_" + save.saveId
            );
            screenData.elements.add(loadButton);
            
            /* Load Save */
            ScreenElement deleteButton = new ScreenElement(
                "button",
                "delete_" + save.saveId,
                "Delete",
                700, yPos,
                1.0f,
                new float[]{0.8f, 0.2f, 0.2f},
                "delete_" + save.saveId
            );
            screenData.elements.add(deleteButton);
        }
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
            
            updateSaveSlots();
        } catch (Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }
}
