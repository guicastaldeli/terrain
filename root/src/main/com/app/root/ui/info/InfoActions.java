package main.com.app.root.ui.info;

public class InfoActions {
    private Info info;

    public InfoActions(Info info) {
        this.info = info;
    }

    /**
     * Show Tree Damage
     */
    public void showTreeDamage(
        int treeLevel, 
        int damage, 
        float currHealth, 
        float maxHealth
    ) {
        MessageData data = new MessageData(info);
        data.put("treeLevel", String.valueOf(treeLevel));
        data.put("damage", String.valueOf(damage));
        data.put("currHealth", String.valueOf((int)currHealth));
        data.put("treeDataHealth", String.valueOf((int)maxHealth));
        
        info.showMessage("wood-life", data);
    }
    
    /**
     * Show Axe Level (Low)
     */
    public void showAxeLevelLow(int axeLevel, int treeLevel) {
        MessageData data = new MessageData(info);
        data.put("axeLevel", String.valueOf(axeLevel));
        data.put("treeLevel", String.valueOf(treeLevel));
        
        info.showMessage("axe-level-low", data);
    }
}