package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;

public class Upgrader {
    private MainData data;
    private EnvController envController;

    public Upgrader(EnvController envController) {
        this.data = MainDataLoader.load();
        this.envController = envController;
    }

    /**
     * Upgrade
     */
    public boolean canUpgrade() {
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        int currLevel = (int) EnvCall.callReturn(axeInstance, "getLevel");
        int upgradeCost = (int) EnvCall.callReturn(axeInstance, "getUpgradeCost");

        return data.getWood() >= upgradeCost;
    }

    public boolean upgradeAxe() {
        if(!canUpgrade()) return false;

        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        int upgradeCost = (int) EnvCall.callReturn(axeInstance, "getUpgradeCost");
        EnvCall.call(axeInstance, "upgrade");

        saveData();
        return true;
    }

    public void addWood(int amount) {
        data.setWood(data.getWood() + amount);
    }
    
    public int getWood() {
        return data.getWood();
    }
    
    public int getAxeLevel() {
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        return (int) EnvCall.callReturn(axeInstance, "getLevel");
    }
    
    public int getNextUpgradeCost() {
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        return (int) EnvCall.callReturn(axeInstance, "getUpgradeCost");
    }

    private void saveData() {
        //todo this later... :P
    }
}
