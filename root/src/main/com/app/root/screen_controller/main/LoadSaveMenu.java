package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenData;
import main.com.app.root.screen_controller.ScreenElement;

import java.util.*;

public class LoadSaveMenu extends Screen {
    private static final String MENU_PATH = DIR + "main/load_save_menu.xml";

    private final MainScreen mainScreen;
    public List<ScreenElement> saveMenuEl = new ArrayList<>();
    public boolean showSaveMenu = false;

    public LoadSaveMenu(MainScreen mainScreen) {
        super(MENU_PATH, "load_save_menu");
        this.mainScreen = mainScreen;
    }

    public void render() {
        saveMenuEl.clear();
        try {
            ScreenData saveMenuData = DocParser.parseScreen(
                MENU_PATH,
                window.getWidth(),
                window.getHeight()
            );
            saveMenuEl.addAll(saveMenuData.elements);

            int startY = 150;
            int slotHeight = 60;
            for(int i = 0; i < mainScreen.availableSaves.size(); i++) {
                SaveInfo save = mainScreen.availableSaves.get(i);

                /* Save Info */
                ScreenElement saveLabel = new ScreenElement(
                    "label",
                    "save_label" + i,
                    String.format(
                        "%s - %s - %s", 
                        save.saveName, 
                        save.playTime, 
                        save.lastPlayed
                    ),
                    100, startY + (i * slotHeight),
                    0.9f,
                    new float[]{1.0f, 1.0f, 1.0f},
                    ""
                );
                saveMenuEl.add(saveLabel);

                /* Load Save */
                ScreenElement loadButton = new ScreenElement(
                    "button",
                    "load_" + save.saveId,
                    "Load",
                    600, startY + (i * slotHeight),
                    1.0f,
                    new float[]{0.2f, 0.8f, 0.2f},
                    "load_" + save.saveId
                );
                saveMenuEl.add(loadButton);

                /* Delete Save */
                ScreenElement deleteButton = new ScreenElement(
                    "button",
                    "delete_" + save.saveId,
                    "Delete",
                    700, startY + (i * slotHeight),
                    1.0f,
                    new float[]{0.8f, 0.2f, 0.2f},
                    "delete_" + save.saveId
                );
                saveMenuEl.add(deleteButton);
            }

            if(mainScreen.availableSaves.isEmpty()) {
                ScreenElement noSaves = DocParser.getElementById(saveMenuData, "noSavesLabel");
                if (noSaves != null) {
                    noSaves.visible = true;
                }
            }
        } catch (Exception err) {
            System.err.println("Failed to render save menu: " + err.getMessage());
            err.printStackTrace();
        }
    }
}
