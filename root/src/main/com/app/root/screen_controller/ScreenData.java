package main.com.app.root.screen_controller;
import java.util.*;

public class ScreenData {
    public String screenType;
    public List<ScreenElement> elements;
    public Map<String, String> screenAttr;

    public ScreenData(String screenType) {
        this.screenType = screenType;
        this.elements = new ArrayList<>();
        this.screenAttr = new HashMap<>();
    }
}
