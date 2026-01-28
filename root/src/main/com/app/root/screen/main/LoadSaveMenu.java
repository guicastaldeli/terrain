package main.com.app.root.screen.main;
import main.com.app.root.DocParser;
import main.com.app.root._save.SaveInfo;
import main.com.app.root.screen.Screen;
import main.com.app.root.screen.ScreenController;
import main.com.app.root.screen.ScreenElement;
import main.com.app.root.utils.HexToVec3;

import java.util.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glScissor;

public class LoadSaveMenu extends Screen {
    public static final String MENU_PATH = DIR + "main/load_save_menu.xml";

    private final MainScreen mainScreen;
    public List<ScreenElement> saveMenuEl = new ArrayList<>();
    public boolean showSaveMenu = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 30;
    private static final int VISIBLE_AREA_HEIGHT = 500;

    public LoadSaveMenu(MainScreen mainScreen) {
        super(MENU_PATH, "load_save_menu");
        this.mainScreen = mainScreen;
        this.active = false;
        setupScrollCallback();
    }

    private void setupScrollCallback() {
        glfwSetScrollCallback(window.getWindow(), (windowHandle, xOffset, yOffset) -> {
            if(this.active) {
                handleScroll(xOffset, yOffset);
            }
        });
    }

    /**
     * Handle Mouse Scroll
     */
    public void handleScroll(double xoffset, double yoffset) {
        if(!active) return;
        
        scrollOffset -= (int)(yoffset * SCROLL_SPEED);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        updateElementPositions();
    }
    
    /**
     * Update Element Positions
     */
    private void updateElementPositions() {
        int windowHeight = window.getHeight();
        
        for(ScreenElement element : screenData.elements) {
            if(element.id.startsWith("save_name_") || 
                element.id.startsWith("play_time_") || 
                element.id.startsWith("last_played_") || 
                element.id.startsWith("load_") || 
                element.id.startsWith("delete_") ||
                element.id.startsWith("slot_bg_") ||
                element.id.equals("noSavesLabel")
            ) {
                if(!element.attr.containsKey("originalY")) {
                    element.attr.put("originalY", String.valueOf(element.y));
                }
                
                int originalY = Integer.parseInt(element.attr.get("originalY"));
                element.y = originalY - scrollOffset;
                element.visible = true;
            }
            
            if(element.id.equals("scrollbarThumb") && maxScroll > 0) {
                int scrollbarBgHeight = (int)(windowHeight * 0.65f);
                int scrollbarBgY = (int)(windowHeight * 0.25f);
                
                float visibleRatio = Math.min(1.0f, (float)VISIBLE_AREA_HEIGHT / (scrollbarBgHeight + maxScroll));
                int thumbHeight = Math.max(20, (int)(scrollbarBgHeight * visibleRatio));
                element.height = thumbHeight;
                
                float scrollProgress = maxScroll > 0 ? (float)scrollOffset / maxScroll : 0f;
                int thumbY = scrollbarBgY + (int)(scrollProgress * (scrollbarBgHeight - thumbHeight));
                element.y = thumbY;
            }
        }
    }

    /**
     * Show
     */
    public void show() {
        this.active = true;
        screenController.switchTo(ScreenController.SCREENS.LOAD_SAVE_MENU);
        resetHover();
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
        resetHover();
        if(returnToMain) {
            screenController.switchTo(ScreenController.SCREENS.MAIN);
        }
    }

    private void resetHover() {
        if(screenData == null) return;
        for(ScreenElement element : screenData.elements) {
            if(element.hoverable && element.isHovered) {
                element.removeHover();
            }
        }
    }
    
    public void updateSaveSlots() {
        if(screenData == null) return;
        
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        
        List<ScreenElement> newElements = new ArrayList<>();
        for(ScreenElement el : screenData.elements) {
            boolean isSaveElement = 
                el.id.startsWith("save_name_") || 
                el.id.startsWith("play_time_") || 
                el.id.startsWith("last_played_") || 
                el.id.startsWith("load_") || 
                el.id.startsWith("delete_") ||
                el.id.startsWith("slot_bg_") ||
                el.id.equals("noSavesLabel");
            if(!isSaveElement) {
                newElements.add(el);
            }
        }
        
        screenData.elements = newElements;
        
        updateResponsiveTitle(windowWidth);
        
        ScreenElement saveSlotsContainer = null;
        for(ScreenElement el : screenData.elements) {
            if("saveSlots".equals(el.id)) {
                saveSlotsContainer = el;
                break;
            }
        }
        
        if(saveSlotsContainer == null) return;
        
        if(mainScreen.availableSaves.isEmpty()) {
            ScreenElement noSavesLabel = new ScreenElement(
                "label",
                "noSavesLabel",
                "No saves found...",
                "comic_sans",
                saveSlotsContainer.x + (int)(windowWidth * 0.35f),
                saveSlotsContainer.y + (int)(windowHeight * 0.4f),
                200, 30,
                1.0f,
                HexToVec3.hexToRgbaArray("#ccccccff"),
                ""
            );
            noSavesLabel.visible = true;
            screenData.elements.add(noSavesLabel);
            maxScroll = 0;
        } else {
            int startY = saveSlotsContainer.y + (int)(windowHeight * 0.15f);
            int slotHeight = (int)(windowHeight * 0.2f);
            int infoSpacing = (int)(windowHeight * 0.055f);
            int infoSpacingTex = (int)(windowHeight * 0.06f);
            int labelWidth = (int)(windowWidth * 0.35f);
            int labelHeight = 30;
            int buttonWidth = (int)(windowWidth * 0.08f);
            int buttonHeight = (int)(windowHeight * 0.05f);
            
            int loadButtonX = saveSlotsContainer.x + (int)(windowWidth * 0.60f);
            int deleteButtonX = saveSlotsContainer.x + (int)(windowWidth * 0.70f);
            
            for(int i = 0; i < mainScreen.availableSaves.size(); i++) {
                SaveInfo save = mainScreen.availableSaves.get(i);
                int baseY = startY + (i * slotHeight);
                
                /* Slot Background */
                ScreenElement slotBackground = new ScreenElement(
                    "div",
                    "slot_bg_" + save.saveId,
                    "",
                    "arial",
                    saveSlotsContainer.x + (int)(windowWidth * 0.03f),
                    baseY - (int)(windowHeight * 0.02f),
                    (int)(windowWidth * 0.80f),
                    slotHeight - (int)(windowHeight * 0.01f),
                    1.0f,
                    HexToVec3.hexToRgbaArray("#000000b3"),
                    ""
                );
                slotBackground.hoverable = true;
                slotBackground.hasBackground = true;
                slotBackground.borderWidth = 1.0f;
                slotBackground.borderColor = HexToVec3.hexToRgbaArray("#4d4d4dcc");
                slotBackground.hoverColor = HexToVec3.hexToRgbaArray("#000000e6");
                screenData.elements.add(slotBackground);
                
                /* Save Name */
                ScreenElement saveNameLabel = new ScreenElement(
                    "label",
                    "save_name_" + save.saveId,
                    save.saveName,
                    "cocogooselightitalic",
                    saveSlotsContainer.x + (int)(windowWidth * 0.05f),
                    baseY,
                    labelWidth, labelHeight,
                    1.0f,
                    HexToVec3.hexToRgbaArray("#ffffffff"),
                    ""
                );
                screenData.elements.add(saveNameLabel);
                
                /* Play Time */
                ScreenElement playTimeLabel = new ScreenElement(
                    "label",
                    "play_time_" + save.saveId,
                    "Play Time: " + save.playTime,
                    "cocogooseultralightitalic",
                    saveSlotsContainer.x + (int)(windowWidth * 0.05f),
                    baseY + infoSpacingTex,
                    labelWidth, labelHeight,
                    1.0f,
                    HexToVec3.hexToRgbaArray("#ccccccff"),
                    ""
                );
                screenData.elements.add(playTimeLabel);
                
                /* Last Played */
                ScreenElement lastPlayedLabel = new ScreenElement(
                    "label",
                    "last_played_" + save.saveId,
                    "Last Played: " + save.lastPlayed,
                    "cocogooseultralightitalic",
                    saveSlotsContainer.x + (int)(windowWidth * 0.05f),
                    baseY + (infoSpacing * 2),
                    labelWidth, labelHeight,
                    1.0f,
                    HexToVec3.hexToRgbaArray("#b3b3b3ff"),
                    ""
                );
                screenData.elements.add(lastPlayedLabel);
                
                /* Load Button */
                ScreenElement loadButton = new ScreenElement(
                    "button",
                    "load_" + save.saveId,
                    "Load",
                    "cocogooselight",
                    loadButtonX,
                    baseY + (int)(slotHeight * 0.3f),
                    buttonWidth, buttonHeight,
                    1.0f,
                    HexToVec3.hexToRgbaArray("#50f270ff"),
                    "load_" + save.saveId
                );
                loadButton.hoverable = true;
                loadButton.hoverColor = HexToVec3.hexToRgbaArray("#d3e3d6ff");
                screenData.elements.add(loadButton);

                /* Delete Button */
                ScreenElement deleteButton = new ScreenElement(
                    "button",
                    "delete_" + save.saveId,
                    "Delete",
                    "cocogooselight",
                    deleteButtonX,
                    baseY + (int)(slotHeight * 0.3f),
                    buttonWidth, buttonHeight,
                    1.0f,
                    HexToVec3.hexToRgbaArray("#e3324aff"),
                    "delete_" + save.saveId
                );
                deleteButton.hoverable = true;
                deleteButton.hoverColor = HexToVec3.hexToRgbaArray("#d1a5abff");
                screenData.elements.add(deleteButton);
            }

            int visibleAreaHeight = (int)(windowHeight * 0.65f);
            int totalContentHeight = startY + (mainScreen.availableSaves.size() * slotHeight);
            maxScroll = Math.max(0, totalContentHeight - visibleAreaHeight);
        }

        scrollOffset = 0;
        updateElementPositions();
    }

    private void updateResponsiveTitle(int windowWidth) {
        ScreenElement titleElement = null;
        for(ScreenElement el : screenData.elements) {
            if("title".equals(el.id)) {
                titleElement = el;
                break;
            }
        }
        
        if(titleElement != null && textRenderer != null) {
            String baseText = "Load Game";
            float baseTextWidth = textRenderer.getTextWidth(baseText, titleElement.scale, titleElement.fontFamily);
            
            float availableWidth = windowWidth * 0.85f;
            float dashWidth = textRenderer.getTextWidth("—", titleElement.scale, titleElement.fontFamily);
            
            float remainingWidth = availableWidth - baseTextWidth;
            if(remainingWidth <= 0) {
                titleElement.text = baseText;
                return;
            }
            
            int dashesPerSide = Math.max(1, (int)(remainingWidth / (2 * dashWidth)));
            dashesPerSide = Math.min(dashesPerSide, 200);
            String dashes = "—".repeat(dashesPerSide);
            titleElement.text = dashes + " " + baseText + " " + dashes;
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
        if(!active || textRenderer == null) {
            return;
        }

        updateResponsiveTitle(window.getWidth());
        
        int windowHeight = window.getHeight();
        int clipX = (int)(window.getWidth() * 0.03f);
        int clipY = (int)(windowHeight * 0.25f);
        int clipWidth = (int)(window.getWidth() * 0.90f);
        int clipHeight = (int)(windowHeight * 0.68f);
        
        for(ScreenElement element : screenData.elements) {
            if(!element.id.startsWith("save_name_") && 
            !element.id.startsWith("play_time_") && 
            !element.id.startsWith("last_played_") && 
            !element.id.startsWith("load_") && 
            !element.id.startsWith("delete_") &&
            !element.id.startsWith("slot_bg_") &&
            !element.id.equals("noSavesLabel") &&
            !element.id.equals("scrollbarBg") &&
            !element.id.equals("scrollbarThumb")) {
                
                renderElement(element);
            }
        }
        
        glEnable(GL_SCISSOR_TEST);
        int scissorY = windowHeight - clipY - clipHeight;
        glScissor(clipX, scissorY, clipWidth, clipHeight);
        
        for(ScreenElement element : screenData.elements) {
            if(element.id.startsWith("save_name_") || 
            element.id.startsWith("play_time_") || 
            element.id.startsWith("last_played_") || 
            element.id.startsWith("load_") || 
            element.id.startsWith("delete_") ||
            element.id.startsWith("slot_bg_") ||
            element.id.equals("noSavesLabel")) {
                
                renderElement(element);
            }
        }
        
        glDisable(GL_SCISSOR_TEST);
        
        for(ScreenElement element : screenData.elements) {
            if(element.id.equals("scrollbarBg") || element.id.equals("scrollbarThumb")) {
                renderElement(element);
            }
        }
    }

    private void renderElement(ScreenElement element) {
        if(!element.visible) return;
        
        if(element.type.equals("div") || element.type.equals("button")) {
            DocParser.renderUIElement(element, window.getWidth(), window.getHeight(), shaderProgram);
        }
        
        if((element.type.equals("button") || element.type.equals("label")) && 
        element.text != null && !element.text.isEmpty()) {
            if(element.hasShadow) {
                textRenderer.renderTextWithShadow(
                    element.text,
                    element.x,
                    element.y,
                    element.scale,
                    element.color,
                    element.shadowOffsetX,
                    element.shadowOffsetY,
                    element.shadowBlur,
                    element.shadowColor,
                    element.fontFamily
                );
            } else {
                textRenderer.renderText(
                    element.text,
                    element.x,
                    element.y,
                    element.scale,
                    element.color,
                    element.fontFamily
                );
            }
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
            
            if(active) updateSaveSlots();
        } catch(Exception err) {
            System.err.println("Failed to re-parse save menu on resize: " + err.getMessage());
        }
    }

    @Override
    public void update() {
       if(lastMouseX >= 0 && lastMouseY >= 0) {
           handleMouseMove(lastMouseX, lastMouseY);
           System.out.println(lastMouseX);
        }
    }

    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        if(!active) return;
        
        for(ScreenElement element : screenData.elements) {
            if(element.visible && element.hoverable) {
                boolean wasHovered = element.isHovered;
                boolean isHovered = element.containsPoint(mouseX, mouseY);
                
                if(isHovered && !wasHovered) {
                    element.applyHover();
                } else if(!isHovered && wasHovered) {
                    element.removeHover();
                }
            }
        }
    }
}