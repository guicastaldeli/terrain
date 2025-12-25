package main.com.app.root.ui;
import java.util.*;

public class UIData {
    public String uiType;
    public List<UIElement> elements;
    public Map<String, String> uiAttr;

    public UIData(String uiType) {
        this.uiType = uiType;
        this.elements = new ArrayList<>();
        this.uiAttr = new HashMap<>();
    }
}
