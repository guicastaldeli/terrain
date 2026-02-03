package main.com.app.root.ui.info;
import main.com.app.root.DocParser;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.ui.UI;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIElement;

import java.util.*;

public class Info extends UI {
    private final Window window;
    private final ShaderProgram shaderProgram;
    private final UIController uiController;
    private final InfoActions infoActions;

    private MessageData messageData;
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

        this.window = window;
        this.shaderProgram = shaderProgram;
        this.uiController = uiController;
        this.infoActions = new InfoActions(this);
        
        this.messageData = new MessageData(this);
        this.messageTimers = new HashMap<>();
        this.activeMessages = new HashMap<>();
        
        hideAllMessages();
    }

    public InfoActions getInfoActions() {
        return infoActions;
    }
    
    public MessageData getMessageData() {
        return messageData;
    }

    /**
     * 
     * Show Message
     * 
     */
    public void showMessage(String messageId, MessageData data) {
        this.visible = true;
        
        activeMessages.put(messageId, data);
        messageTimers.put(messageId, MESSAGE_DURATION);

        updateMessageElements(messageId, data);
        setElVisibility(messageId, true);
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
            if(el.id.equals(parentId)) {
                el.visible = visible;
                break;
            }
        }
        for(UIElement el : uiData.elements) {
            if(el.id.startsWith(parentId + "-")) {
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

    /**
     * 
     * Render
     * 
     */
    @Override
    public void render() {
        if(!visible || textRenderer == null) {
            return;
        }
        
        super.render();
    }

    /**
     * Window Resize
     */
    @Override
    public void onWindowResize(int width, int height) {
        if(getTextRenderer() != null) {
            getTextRenderer().updateScreenSize(width, height);
        }

        try {
            this.uiData = DocParser.parseUI(
                UI_PATH,
                width,
                height
            );
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }
}