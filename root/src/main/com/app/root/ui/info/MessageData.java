package main.com.app.root.ui.info;
import java.util.*;

public class MessageData extends HashMap<String, String> {
    /**
     * Show Tree Damage Message
     */
    public static void showTreeDamage(
        String treeIndex, 
        int damage, 
        float currHealth, 
        float maxHealth
    ) {
        String messageId = "wood-life";

        MessageData data = new MessageData();
        data.put("treeDataIndex", treeIndex);
        data.put("damage", String.valueOf(damage));
        data.put("currHealth", String.valueOf((int)currHealth));
        data.put("treeDataHealth", String.valueOf((int)maxHealth));

        Info.showMessage(messageId, data);
    }

    /**
     * Show Axe Level Low
     */
    public static void showAxeLevelLow(int axeLevel, int treeLevel) {
        String messageId = "axe-level-low";

        MessageData data = new MessageData();
        data.put("axeLevel", String.valueOf(axeLevel));
        data.put("treeLevel", String.valueOf(treeLevel));

        Info.showMessage(messageId, data);
    }
}