package main.com.app.root.ui.info;
import main.com.app.root.DocParser;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.ui.UI;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIElement;
import main.com.app.root.utils.TreeColors;

import java.util.*;

import org.joml.Vector3f;

public class Info extends UI {
    private final Window window;
    private final ShaderProgram shaderProgram;
    private final UIController uiController;
    private final InfoActions infoActions;

    private MessageData messageData;
    private Map<String, Float> messageTimers;
    private Map<String, MessageData> activeMessages;
    private Map<String, String> originalTemplates;

    private int level;

    private static final String UI_PATH = DIR + "info/info.xml";
    private static final float MESSAGE_DURATION = 200.0f;

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

        this.originalTemplates = new HashMap<>();
        storeOriginalTemplates();
        
        hideAllMessages();
        setupResizeCallback();
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public InfoActions getInfoActions() {
        return infoActions;
    }
    
    public MessageData getMessageData() {
        return messageData;
    }

    private void storeOriginalTemplates() {
        if(uiData == null || uiData.elements == null) return;

        for(UIElement el : uiData.elements) {
            if(el.text != null && !el.text.isEmpty()) {
                originalTemplates.put(el.id, el.text);
            }
        }
    }

    /**
     * 
     * Show Message
     * 
     */
    public void showMessage(String messageId, MessageData data) {
        clearAllMessages();
        this.visible = true;
        
        activeMessages.put(messageId, data);
        messageTimers.put(messageId, MESSAGE_DURATION * Tick.getIDeltaTime());

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
                String template = originalTemplates.get(el.id);
                if(template == null) template = el.text; 

                String updatedText = replacePlaceholders(template, data);
                el.text = updatedText;

                if(el.type.equals("div")) {
                    for(UIElement label : uiData.elements) {
                        if(label.id.contains("-c")) {
                            Vector3f color = TreeColors.getColorForLevel(level);
                            float[] colorArr = {color.x, color.y, color.z};
                            label.color = colorArr;
                        }
                    }
                }
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
     * Hide Messages
     */
    private void hideMessages() {
        hideAllMessages();
        for(String messageId : activeMessages.keySet()) {
            setElVisibility(messageId, true);
        }
    }
    
    private void hideAllMessages() {
        for(UIElement el : uiData.elements) {
            setElVisibility(el.id, false);
        }
        for(String messageId : activeMessages.keySet()) {
            setElVisibility(messageId, false);
        }
    }

    /**
     * Clear
     */
    public void clearMessage(String messageId) {
        setElVisibility(messageId, false);
        activeMessages.remove(messageId);
        messageTimers.remove(messageId);
        if(activeMessages.isEmpty()) {
            this.visible = false;
        }
    }

    public void clearAllMessages() {
        List<String> toRemove = new ArrayList<>(activeMessages.keySet());
        for(String messageId : toRemove) {
            clearMessage(messageId);
        }
    }

    /**
     * 
     * Update
     * 
     */
    @Override
    public void update() {
        super.update();

        List<String> toRemove = new ArrayList<>();
        for(Map.Entry<String, Float> entry : messageTimers.entrySet()) {
            String messageId = entry.getKey();
            float timeLeft = entry.getValue() - Tick.getIDeltaTime();
            
            if(timeLeft <= 0) {
                toRemove.add(messageId);
            } else {
                entry.setValue(timeLeft);
            }
        }
        for(String messageId : toRemove) {
            clearMessage(messageId);
        }
    }

    /**
     * 
     * Render
     * 
     */
    @Override
    public void render() {
        if(!this.visible) return;

        for(UIElement element : uiData.elements) {
            if(element.type.equals("div")) {
                for(UIElement label : uiData.elements) {
                    label.hasBackground = false;
                }
            } 
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
            storeOriginalTemplates();

            for(Map.Entry<String, MessageData> entry : activeMessages.entrySet()) {
                updateMessageElements(entry.getKey(), entry.getValue());
            }
            hideMessages();
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }

    private void setupResizeCallback() {
        window.addResizeCallback(() -> {
            onWindowResize(window.getWidth(), window.getHeight());
        });
    }
}