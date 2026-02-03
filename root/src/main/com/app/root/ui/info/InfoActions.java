package main.com.app.root.ui.info;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InfoActions {
    private Info info;
    private Map<String, Consumer<Map<String, Object>>> actionHandlers;

    public InfoActions(Info info) {
        this.info = info;
        this.actionHandlers = new HashMap<>();
        this.registerActions();
    }

    /**
     * 
     * Register Actions
     * 
     */
    public void registerAction(String actionName, Consumer<Map<String, Object>> handler) {
        actionHandlers.put(actionName, handler);
    }
    
    private void registerActions() {
        /* Tree Damage */
        registerAction("tree_damage", p -> {
            String treeIndex = (String) p.get("treeIndex");
            int damage = (int) p.get("damage");
            float currHealth = (float) p.get("currHealth");
            float maxHealth = (float) p.get("maxHealth");
            MessageData.showTreeDamage(
                treeIndex, 
                damage, 
                currHealth, 
                maxHealth
            );
        });
        /* Axe Level (Low) */
        registerAction("axe_level_low", p -> {
            int axeLevel = (int) p.get("axeLevel");
            int treeLevel = (int) p.get("treeLevel");
            MessageData.showAxeLevelLow(axeLevel, treeLevel);
        });
    }

    /**
     * 
     * Execute Action
     * 
     */
    public void executeAction(String actionName, Map<String, Object> params) {
        Consumer<Map<String, Object>> handler = actionHandlers.get(actionName);
        if(handler != null) {
            handler.accept(params);
        } else {
            System.err.println("No handler registered for action: " + actionName);
        }
    }
}
