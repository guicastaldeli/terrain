package main.com.app.root.screen_controller.main;
import main.com.app.root.DocParser;
import main.com.app.root.screen_controller.Screen;
import main.com.app.root.screen_controller.ScreenData;
import main.com.app.root.screen_controller.ScreenElement;

import java.util.*;

public class LoadSaveMenu extends Screen {
    private static final String SCREEN_PATH = DIR + "title/title_screen.xml";
    private List<ScreenElement> saveMenuEl = new ArrayList<>();

    public LoadSaveMenu() {
        
    }

    public render() {
        saveMenuEl.clear();
        try {
            ScreenData saveMenuData = DocParser.parseScreen(

            )
        }
    }
}
