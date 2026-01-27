package main.com.app.root.ui.upgrade_menu;
import main.com.app.root.ui.UI;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIElement;
import main.com.app.root.Window;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.axe.AxeSlot;
import main.com.app.root.DocParser;
import main.com.app.root.Upgrader;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glScissor;

public class UpgradeMenu extends UI {
    private static final String UI_PATH = DIR + "upgrade_menu/upgrade_menu.xml";

    private final Window window;
    private final ShaderProgram shaderProgram;
    private final UIController uiController;
    private final Upgrader upgrader;

    private UpgradeMenuActions upgradeMenuActions;
    
    private int currentAxeLevel = 0;
    private List<AxeSlot> axeSlots;
    
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private static final int SCROLL_SPEED = 30;

    public UpgradeMenu(
        Window window,
        ShaderProgram shaderProgram,
        UIController uiController,
        Upgrader upgrader
    ) {
        super(UI_PATH, "upgrade");

        this.window = window;
        this.shaderProgram = shaderProgram;
        this.uiController = uiController;
        this.upgrader = upgrader;

        this.axeSlots = new ArrayList<>();
        this.currentAxeLevel = upgrader.getAxeLevel();

        refreshAxeSlots();
        
        this.upgradeMenuActions = new UpgradeMenuActions(this);
        setupScrollCallback();
    }

    private void setupScrollCallback() {
        glfwSetScrollCallback(window.getWindow(), (windowHandle, xOffset, yOffset) -> {
            if(this.visible) {
                handleScroll(xOffset, yOffset);
            }
        });
    }

    public void handleScroll(double xoffset, double yoffset) {
        if(!visible) return;
        
        scrollOffset -= (int)(yoffset * SCROLL_SPEED);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        updateElementPositions();
    }
    
    /**
     * Update Element Positions
     */
    private void updateElementPositions() {
        int windowHeight = window.getHeight();
        int movedCount = 0;
        
        for(UIElement element : uiData.elements) {
            if(element.id.startsWith("axe_") || 
                element.id.startsWith("upgrade_") ||
                element.id.startsWith("equip_") ||
                element.id.startsWith("wood_cost_") ||
                element.id.startsWith("locked_") ||
                element.id.startsWith("equipped_") ||
                element.id.startsWith("slot_bg_")
            ) {
                if(!element.attr.containsKey("originalY")) {
                    element.attr.put("originalY", String.valueOf(element.y));
                }
                
                int originalY = Integer.parseInt(element.attr.get("originalY"));
                int newY = originalY - scrollOffset;
                
                if(element.y != newY) {
                    element.y = newY;
                    movedCount++;
                }
            }
            if(element.id.equals("scrollbarThumb")) {
                if(maxScroll > 0) {
                    element.visible = true;
                    
                    UIElement scrollbarBg = null;
                    for(UIElement el : uiData.elements) {
                        if(el.id.equals("scrollbarBg")) {
                            scrollbarBg = el;
                            break;
                        }
                    }
                    
                    if(scrollbarBg != null) {
                        int scrollbarBgHeight = scrollbarBg.height;
                        int scrollbarBgY = scrollbarBg.y;
                        
                        int visibleHeight = (int)(windowHeight * 0.65f);
                        int totalContentHeight = visibleHeight + maxScroll;
                        float visibleRatio = (float)visibleHeight / totalContentHeight;
                        int thumbHeight = Math.max(30, (int)(scrollbarBgHeight * visibleRatio));
                        
                        float scrollProgress = (float)scrollOffset / maxScroll;
                        int maxThumbTravel = scrollbarBgHeight - thumbHeight;
                        int thumbY = scrollbarBgY + (int)(scrollProgress * maxThumbTravel);
                        
                        element.y = thumbY;
                        element.height = thumbHeight;
                    }
                } else {
                    element.visible = false;
                }
            }
            if(element.id.equals("scrollbarBg")) {
                element.visible = maxScroll > 0;
            }
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        if(upgrader != null) {
            this.visible = true;
            currentAxeLevel = upgrader.getAxeLevel();
            scrollOffset = 0;
            refreshAxeSlots();
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        this.visible = false;
    }

    /**
     * Refresh Axe Slots
     */
    public void refreshAxeSlots() {
        if(upgrader == null) return;
        axeSlots.clear();
        
        int playerWood = upgrader.getWood();
        currentAxeLevel = upgrader.getEquippedAxeLevel();

        for(int level = 0; level <= 10; level++) {
            AxeSlot slot = new AxeSlot(level, upgrader.getMaxUnlockedAxeLevel());
            axeSlots.add(slot);
        }

        updateEl();
    }

    /**
     * Update Elements
     */
    private void updateEl() {
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        
        List<UIElement> toRemove = new ArrayList<>();
        for(UIElement el : uiData.elements) {
            if(el.id.startsWith("axe_") || 
                el.id.startsWith("upgrade_") ||
                el.id.startsWith("equip_") ||
                el.id.startsWith("wood_cost_") ||
                el.id.startsWith("locked_") ||
                el.id.startsWith("equipped_") ||
                el.id.startsWith("slot_bg_") ||
                el.id.equals("wood_count") ||
                el.id.equals("current_axe")
            ) {
                toRemove.add(el);
            }
        }
        uiData.elements.removeAll(toRemove);

        int containerX = (int)(windowWidth * 0.05f);
        int containerY = (int)(windowHeight * 0.20f);

        /* Wood Count */
        int playerWood = upgrader != null ? upgrader.getWood() : 0;
        UIElement woodCountLabel = new UIElement(
            "label",
            "wood_count",
            "Wood: " + playerWood,
            (int)(windowWidth * 0.08f), 
            (int)(windowHeight * 0.10f),
            200, 30,
            1.0f,
            new float[]{0.9f, 0.8f, 0.6f, 1.0f},
            ""
        );
        woodCountLabel.fontFamily = "comic_sans";
        uiData.elements.add(woodCountLabel);
        
        /* Current Axe */
        UIElement currentAxeLabel = new UIElement(
            "label",
            "current_axe",
            "Current Axe: Level " + currentAxeLevel,
            (int)(windowWidth * 0.08f), 
            (int)(windowHeight * 0.15f),
            200, 30,
            1.0f,
            new float[]{0.0f, 1.0f, 0.0f, 1.0f},
            ""
        );
        currentAxeLabel.fontFamily = "comic_sans";
        uiData.elements.add(currentAxeLabel);
        
        /* Axe Slots */
        int startY = containerY + (int)(windowHeight * 0.05f);
        int slotHeight = (int)(windowHeight * 0.18f);
        int slotSpacing = (int)(windowHeight * 0.02f);
        int slotWidth = (int)(windowWidth * 0.70f);
        
        int maxUnlockedLevel = upgrader != null ? upgrader.getMaxUnlockedAxeLevel() : 0;
        int equippedLevel = upgrader != null ? upgrader.getEquippedAxeLevel() : 0;
        
        for(int i = 0; i < axeSlots.size(); i++) {
            AxeSlot slot = axeSlots.get(i);
            int slotX = containerX;
            int slotY = startY + (i * (slotHeight + slotSpacing));
            
            /* Slot Background */
            UIElement slotBackground = new UIElement(
                "div",
                "slot_bg_" + slot.level,
                "",
                slotX - (int)(windowWidth * 0.01f),
                slotY - (int)(windowHeight * 0.02f),
                slotWidth,
                slotHeight,
                1.0f,
                new float[]{0.15f, 0.15f, 0.15f, 0.8f},
                ""
            );
            slotBackground.hoverable = true;
            slotBackground.hasBackground = true;
            slotBackground.borderWidth = 2.0f;
            slotBackground.borderColor = new float[]{0.3f, 0.3f, 0.3f, 0.8f};
            slotBackground.hoverColor = new float[]{0.2f, 0.2f, 0.2f, 0.9f};
            uiData.elements.add(slotBackground);
            
            /* Axe Level Label */
            UIElement axeLabel = new UIElement(
                "label",
                "axe_" + slot.level,
                "Axe Level " + slot.level,
                slotX,
                slotY,
                slotWidth, 30,
                1.0f,
                slot.level == equippedLevel ?
                    new float[]{0.0f, 1.0f, 0.0f, 1.0f} : 
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                ""
            );
            axeLabel.fontFamily = "comic_sans";
            uiData.elements.add(axeLabel);
            
            /* Wood Cost */
            int upgradeCost = upgrader != null ? upgrader.getUpgradeCost(slot.level) : 0;
            boolean canAfford = playerWood >= upgradeCost;
            UIElement woodLabel = new UIElement(
                "label",
                "wood_cost_" + slot.level,
                "Wood Required: " + upgradeCost,
                slotX,
                slotY + (int)(slotHeight * 0.3f),
                slotWidth, 30,
                1.0f,
                canAfford ? 
                    new float[]{0.2f, 0.8f, 0.2f, 1.0f} : 
                    new float[]{0.8f, 0.2f, 0.2f, 1.0f},
                ""
            );
            woodLabel.fontFamily = "comic_sans";
            uiData.elements.add(woodLabel);
            
            /* Action Button or Status */
            if(slot.level == equippedLevel) {
                UIElement equippedLabel = new UIElement(
                    "label",
                    "equipped_" + slot.level,
                    "EQUIPPED",
                    slotX,
                    slotY + (int)(slotHeight * 0.6f),
                    (int)(windowWidth * 0.15f), 
                    (int)(windowHeight * 0.05f),
                    1.0f,
                    new float[]{0.0f, 0.5f, 1.0f, 1.0f},
                    ""
                );
                equippedLabel.fontFamily = "comic_sans";
                uiData.elements.add(equippedLabel);
            } else if(upgrader.getData().isAxeLevelUnlocked(slot.level)) {
                UIElement equipButton = new UIElement(
                    "button",
                    "equip_" + slot.level,
                    "EQUIP",
                    slotX,
                    slotY + (int)(slotHeight * 0.6f),
                    (int)(windowWidth * 0.10f), 
                    (int)(windowHeight * 0.05f),
                    1.0f,
                    new float[]{0.2f, 0.6f, 1.0f, 1.0f},
                    "equip_" + slot.level
                );
                equipButton.fontFamily = "comic_sans";
                equipButton.hoverable = true;
                equipButton.hoverColor = new float[]{0.4f, 0.7f, 1.0f, 1.0f};
                uiData.elements.add(equipButton);
            } else if(canAfford) {
                UIElement upgradeButton = new UIElement(
                    "button",
                    "upgrade_" + slot.level,
                    "UPGRADE",
                    slotX,
                    slotY + (int)(slotHeight * 0.6f),
                    (int)(windowWidth * 0.10f), 
                    (int)(windowHeight * 0.05f),
                    1.0f,
                    new float[]{0.2f, 0.8f, 0.2f, 1.0f},
                    "upgrade_" + slot.level
                );
                upgradeButton.fontFamily = "comic_sans";
                upgradeButton.hoverable = true;
                upgradeButton.hoverColor = new float[]{0.3f, 0.9f, 0.3f, 1.0f};
                uiData.elements.add(upgradeButton);
            } else {
                UIElement lockedLabel = new UIElement(
                    "label",
                    "locked_" + slot.level,
                    "LOCKED - Need more wood",
                    slotX,
                    slotY + (int)(slotHeight * 0.6f),
                    (int)(windowWidth * 0.20f), 
                    (int)(windowHeight * 0.05f),
                    1.0f,
                    new float[]{0.5f, 0.5f, 0.5f, 1.0f},
                    ""
                );
                lockedLabel.fontFamily = "comic_sans";
                uiData.elements.add(lockedLabel);
            }
        }
        
        int visibleAreaHeight = (int)(windowHeight * 0.65f);
        int totalContentHeight = startY + (axeSlots.size() * (slotHeight + slotSpacing));
        maxScroll = Math.max(0, totalContentHeight - visibleAreaHeight);
        if(scrollOffset > maxScroll) scrollOffset = maxScroll;

        updateElementPositions();
    }

    @Override
    public void handleAction(String action) {
        if(action.startsWith("equip_")) {
            int level = Integer.parseInt(action.substring(6));
            upgradeMenuActions.equipAxe(level);
            refreshAxeSlots();
        } else if(action.startsWith("upgrade_")) {
            int level = Integer.parseInt(action.substring(8));
            upgradeMenuActions.upgradeAxe(level);
            refreshAxeSlots();
        } else if(action.equals("close")) {
            System.out.print(action);
            uiController.hide();
        }
    }

    @Override
    public void update() {
        if(lastMouseX >= 0 && lastMouseY >= 0) {
           handleMouseMove(lastMouseX, lastMouseY);
        }
    }

    public void setCurrentAxeLevel(int level) {
        this.currentAxeLevel = level;
        refreshAxeSlots();
    }
    
    public int getCurrentAxeLevel() {
        return currentAxeLevel;
    }
    
    public Upgrader getUpgrader() {
        return upgrader;
    }

    @Override 
    public void render() {
        if(!visible || textRenderer == null) {
            return;
        }
        
        int windowWidth = window.getWidth();
        int windowHeight = window.getHeight();
        
        for(UIElement element : uiData.elements) {
            if(!element.id.startsWith("axe_") && 
                !element.id.startsWith("upgrade_") &&
                !element.id.startsWith("equip_") &&
                !element.id.startsWith("wood_cost_") &&
                !element.id.startsWith("locked_") &&
                !element.id.startsWith("equipped_") &&
                !element.id.startsWith("slot_bg_") &&
                !element.id.equals("scrollbarBg") &&
                !element.id.equals("scrollbarThumb")) {
                
                renderElement(element);
            }
        }
        
        int clipX = (int)(windowWidth * 0.03f);
        int clipY = (int)(windowHeight * 0.25f);
        int clipWidth = (int)(windowWidth * 0.85f);
        int clipHeight = (int)(windowHeight * 0.65f);
        
        glEnable(GL_SCISSOR_TEST);
        int scissorY = windowHeight - clipY - clipHeight;
        glScissor(clipX, scissorY, clipWidth, clipHeight);
        
        for(UIElement element : uiData.elements) {
            if(element.id.startsWith("axe_") || 
                element.id.startsWith("upgrade_") ||
                element.id.startsWith("equip_") ||
                element.id.startsWith("wood_cost_") ||
                element.id.startsWith("locked_") ||
                element.id.startsWith("equipped_") ||
                element.id.startsWith("slot_bg_")) {
                
                renderElement(element);
            }
        }
        
        glDisable(GL_SCISSOR_TEST);
        
        for(UIElement element : uiData.elements) {
            if(element.id.equals("scrollbarBg") || element.id.equals("scrollbarThumb")) {
                renderElement(element);
            }
        }
    }

    private void renderElement(UIElement element) {
        if(!element.visible) return;
        
        if(element.type.equals("div") || element.type.equals("button")) {
            DocParser.renderUIElement(element, window.getWidth(), window.getHeight(), shaderProgram);
        }
        
        if((element.type.equals("button") || element.type.equals("label")) && 
            element.text != null && !element.text.isEmpty() && textRenderer != null) {
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
            this.uiData = DocParser.parseUI(
                UI_PATH,
                width,
                height
            );
            
            if(visible) refreshAxeSlots();
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }

    @Override
    public void handleMouseMove(int mouseX, int mouseY) {
        if(!visible) return;
        
        for(UIElement element : uiData.elements) {
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