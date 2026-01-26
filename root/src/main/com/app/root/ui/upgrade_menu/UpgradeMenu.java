package main.com.app.root.ui.upgrade_menu;
import main.com.app.root.ui.UI;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIElement;
import main.com.app.root.Window;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.env.axe.AxeController;
import main.com.app.root.env.axe.AxeSlot;
import main.com.app.root.DocParser;
import main.com.app.root.Upgrader;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class UpgradeMenu extends UI {
    private static final String UI_PATH = DIR + "upgrade_menu/upgrade_menu.xml";

    private final Window window;
    private final ShaderProgram shaderProgram;
    private final UIController uiController;
    private final Upgrader upgrader;

    private UpgradeMenuActions upgradeMenuActions;
    
    private int currentAxeLevel = 0;
    private List<AxeSlot> axeSlots;
    private List<String> axeMeshIds;

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
        this.axeMeshIds = new ArrayList<>();
        this.currentAxeLevel = upgrader.getAxeLevel();

        createAxeMeshes();
        refreshAxeSlots();
        
        this.upgradeMenuActions = new UpgradeMenuActions(this);
    }

    @Override
    public void onShow() {
        super.onShow();
        if(upgrader != null) {
            this.visible = true;
            currentAxeLevel = upgrader.getAxeLevel();
            refreshAxeSlots();
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        this.visible = false;
        cleanup();
    }

    /**
     * Create Axe Meshes
     */
    private void createAxeMeshes() {
        for(int level = 0; level <= 10; level++) {
            String meshId = "upgrade_menu_axe_" + level;
            String axeName = "axe" + level;
            try {
                mesh.addModel(meshId, axeName);
                int texId = TextureLoader.load(AxeController.TEX_PATH + axeName + ".png");
                if(texId > 0) mesh.setTex(meshId, texId);

                axeMeshIds.add(meshId);
            } catch(Exception err) {
                System.err.println("Failed to load axe model for level " + level + ": " + err.getMessage());
            }
        }
    }

    /**
     * Render Axe Mesh
     */
    private void renderAxeMesh() {
        int startX = 100;
        int startY = 250;
        int slotWidth = 200;
        int slotHeight = 80;
        int slotSpacing = 20;

        for(int level = AxeController.AXE_MIN_LEVEL; level < AxeController.AXE_MAX_LEVEL; level++) {
            String meshId = "upgrade_menu_axe_" + level;
            if(!mesh.hasMesh(meshId)) continue;

            int slotX = startX + (level % 3) * (slotWidth + slotSpacing);
            int slotY = startY + (level / 3) * (slotHeight + slotSpacing);

            float worldX = (slotX + slotWidth / 2 - window.getWidth() / 2) * 0.01f;
            float worldY = -(slotY + 40 - window.getHeight() / 2) * 0.01f;
            float worldZ = -5.0f;

            mesh.setPosition(meshId, new Vector3f(worldX, worldY, worldZ));
            mesh.render(meshId, 0);
        }
    }

    /**
     * Refresh Axe Slots
     */
    public void refreshAxeSlots() {
        if(upgrader == null) return;
        axeSlots.clear();
        
        int playerWood = upgrader.getWood();
        currentAxeLevel = upgrader.getAxeLevel();

        for(int level = 0; level <= 10; level++) {
            AxeSlot slot = new AxeSlot(level, currentAxeLevel);
            axeSlots.add(slot);
        }

        updateEl();
    }

    /**
     * Update uiData.elements
     */
    private void updateEl() {
        int startX = 100;
        int startY = 250;
        int slotWidth = 200;
        int slotHeight = 80;
        int slotSpacing = 20;

        List<UIElement> toRemove = new ArrayList<>();
        for(UIElement el : uiData.elements) {
            if(el.id.startsWith("axe_") || 
                el.id.startsWith("upgrade_") ||
                el.id.startsWith("equip_") ||
                el.id.startsWith("wood_") ||
                el.id.startsWith("locked_") ||
                el.id.startsWith("equipped_") ||
                el.id.equals("wood_count") ||
                el.id.equals("current_axe")
            ) {
                toRemove.add(el);
            }
        }
        uiData.elements.removeAll(toRemove);

        /* Wood */
        int playerWood = upgrader != null ? upgrader.getWood() : 0;
        UIElement woodCountLabel = new UIElement(
            "label",
            "wood_count",
            "Wood: " + playerWood,
            80, 100,
            200, 30,
            1.0f,
            new float[]{0.9f, 0.8f, 0.6f, 1.0f},
            ""
        );
        uiData.elements.add(woodCountLabel);
        
        /* Axe Current Level */
        UIElement currentAxeLabel = new UIElement(
            "label",
            "current_axe",
            "Current Axe: Level " + currentAxeLevel,
            80, 150,
            200, 30,
            1.0f,
            new float[]{0.0f, 1.0f, 0.0f, 1.0f},
            ""
        );
        uiData.elements.add(currentAxeLabel);
        
        /* Slots */
        int maxUnlockedLevel = upgrader != null ? upgrader.getMaxUnlockedAxeLevel() : 0;
        
        for(int i = 0; i < axeSlots.size(); i++) {
            AxeSlot slot = axeSlots.get(i);
            int slotX = startX + (i % 3) * (slotWidth + slotSpacing);
            int slotY = startY + (i / 3) * (slotHeight + slotSpacing);
            
            UIElement axeLabel = new UIElement(
                "label",
                "axe_" + slot.level,
                "Axe Level " + slot.level,
                slotX, slotY,
                slotWidth, 30,
                0.8f,
                slot.level == currentAxeLevel ? 
                    new float[]{0.0f, 1.0f, 0.0f, 1.0f} : 
                    new float[]{1.0f, 1.0f, 1.0f, 1.0f},
                ""
            );
            uiData.elements.add(axeLabel);
            
            int upgradeCost = upgrader != null ? upgrader.getUpgradeCost(slot.level) : 0;
            boolean canAfford = playerWood >= upgradeCost;
            UIElement woodLabel = new UIElement(
                "label",
                "wood_" + slot.level,
                "Wood: " + playerWood + "/" + upgradeCost,
                slotX, slotY + 30,
                slotWidth, 30,
                0.6f,
                canAfford ? 
                    new float[]{0.2f, 0.8f, 0.2f, 1.0f} : 
                    new float[]{0.8f, 0.2f, 0.2f, 1.0f},
                ""
            );
            uiData.elements.add(woodLabel);
            
            if(slot.level == currentAxeLevel) {
                UIElement equippedLabel = new UIElement(
                    "label",
                    "equipped_" + slot.level,
                    "EQUIPPED",
                    slotX, slotY + 50,
                    slotWidth, 30,
                    0.7f,
                    new float[]{0.0f, 0.5f, 1.0f, 1.0f},
                    ""
                );
                uiData.elements.add(equippedLabel);
            } else if(slot.level <= maxUnlockedLevel) {
                UIElement equipButton = new UIElement(
                    "button",
                    "equip_" + slot.level,
                    "EQUIP",
                    slotX, slotY + 50,
                    80, 30,
                    0.7f,
                    new float[]{0.2f, 0.6f, 1.0f, 1.0f},
                    "equip_" + slot.level
                );
                uiData.elements.add(equipButton);
            } else if(canAfford) {
                UIElement upgradeButton = new UIElement(
                    "button",
                    "upgrade_" + slot.level,
                    "UPGRADE",
                    slotX, slotY + 50,
                    80, 30,
                    0.7f,
                    new float[]{0.2f, 0.8f, 0.2f, 1.0f},
                    "upgrade_" + slot.level
                );
                uiData.elements.add(upgradeButton);
            } else {
                UIElement lockedLabel = new UIElement(
                    "label",
                    "locked_" + slot.level,
                    "LOCKED",
                    slotX, slotY + 50,
                    slotWidth, 30,
                    0.7f,
                    new float[]{0.5f, 0.5f, 0.5f, 1.0f},
                    ""
                );
                uiData.elements.add(lockedLabel);
            }
        }
    }

    @Override
    public void handleAction(String action) {
        if(action.startsWith("equip_")) {
            int level = Integer.parseInt(action.substring(6));
            upgradeMenuActions.equipAxe(level);
        } else if(action.startsWith("upgrade_")) {
            int level = Integer.parseInt(action.substring(8));
            upgradeMenuActions.upgradeAxe(level);
        } else if(action.equals("close")) {
            System.out.print(action);
            uiController.hide();
        }
    }

    @Override
    public void update() {
        if(upgrader != null) {
            refreshAxeSlots();
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
        super.render();
        DocParser.renderUI(
            uiData, 
            window.getWidth(), 
            window.getHeight(), 
            shaderProgram, 
            textRenderer
        );
        renderAxeMesh();
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
        } catch(Exception err) {
            System.err.println("Failed to re-parse screen on resize: " + err.getMessage());
        }
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        for(String meshId : axeMeshIds) {
            if(mesh.hasMesh(meshId)) {
                mesh.removeMesh(meshId);
            }
        }
        axeMeshIds.clear();
    }
}
