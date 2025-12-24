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
        int currLevel = EnvCall.call(axeInstance, "getLevel");
        int upgradeCost = EnvCall.call(axeInstance, "getUpgradeCost");

        return data.getWood() >= upgradeCost;
    }

    public boolean upgradeAxe() {
        if(!canUpgrade()) return false;

        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        int upgradeCost = EnvCall.call(axeInstance, "getUpgradeCost");
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
        return axeController.getLevel();
    }
    
    public int getNextUpgradeCost() {
        return axeController.getUpgradeCost();
    }

    private void saveGameData() {
        //todo this later... :P
    }
}
