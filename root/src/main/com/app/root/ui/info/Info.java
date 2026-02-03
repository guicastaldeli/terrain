package main.com.app.root.ui.info;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.ui.UI;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIElement;

import java.util.*;

public class Info extends UI {
    private static Info instance;

    private final Window window;
    private final ShaderProgram shaderProgram;
    private final UIController uiController;

    private Map<String, Float> messageTimers;
    private Map<String, MessageData> activeMessages;

    private static final String UI_PATH = DIR + "info/info.xml";
    private static final float MESSAGE_DURATION = 3.0f;

    public Info(
        Window window,
        ShaderProgram shaderProgram,
        UIController uiController
    ) {
        super(UI_PATH, "info");
        instance = this;

        this.window = window;
        this.shaderProgram = shaderProgram;
        this.uiController = uiController;
        
        this.messageTimers = new HashMap<>();
        this.activeMessages = new HashMap<>();
        
        hideAllMessages();
    }

    /**
     * 
     * Show Message
     * 
     */
    public static void showMessage(String messageId, MessageData data) {
        instance.activeMessages.put(messageId, data);
        instance.messageTimers.put(messageId, MESSAGE_DURATION);

        instance.updateMessageElements(messageId, data);
        instance.setElVisibility(messageId, true);
    }

    /**
     * 
     * Update Message Elements
     * 
     */
    public void updateMessageElements(String messageId, MessageData data) {
        if(uiData == null || uiData.elements == null) return;

        for(UIElement el : uiData.elements) {
            if(el.id.startsWith(messageId)) {
                String originalText = el.attr.get("text");
                if(originalText == null) {
                    originalText = el.text;
                }

                String updatedText = replacePlaceholders(originalText, data);
                el.text = updatedText;
            }
        }
    }

    private String replacePlaceholders(String text, MessageData data) {
        if(text == null) return "";

        String res = text;
        for(Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            res = res.replace(placeholder, entry.getValue());
        }

        return res;
    }

    /**
     * 
     * Set Element Visibility
     * 
     */
    private void setElVisibility(String parentId, boolean visible) {
        if(uiData == null || uiData.elements == null) return;

        for(UIElement el : uiData.elements) {
            if(el.id.equals(parentId) || el.id.startsWith(parentId + "-")) {
                el.visible = visible;
            }
        }
    }

    /**
     * Hide All Messages
     */
    private void hideAllMessages() {
        setElVisibility("wood-life", false);
        setElVisibility("axe-level-low", false);
    }

    /**
     * 
     * Update
     * 
     */
    @Override
    public void update() {
        super.update();
        messageTimers.entrySet().removeIf(e -> {
            String messageId = e.getKey();
            float timeLeft = e.getValue() - Tick.getIDeltaTime();
            if(timeLeft <= 0) {
                setElVisibility(messageId, false);
                activeMessages.remove(messageId);
                return true;
            } else {
                e.setValue(timeLeft);
                return false;
            }
        });
    }
}
