package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;

public class Upgrader {
    private MainData data;
    private EnvController envController;
    private int cachedAxeLevel;

    public Upgrader(EnvController envController) {
        this.data = MainDataLoader.load();
        this.envController = envController;
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        this.cachedAxeLevel = (int) EnvCall.callReturn(axeInstance, "getLevel");
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
        data.setWood(data.getWood() - upgradeCost);
        EnvCall.call(axeInstance, "upgrade");
        
        cachedAxeLevel++;
        data.setAxeLevel(cachedAxeLevel);
        data.setCurrentAxe("axe" + cachedAxeLevel);

        saveData();
        
        System.out.println("Upgraded! New level: " + cachedAxeLevel);
        return true;
    }

    public void equipAxe(int level) {
        if(level < 0 || level > data.getAxeLevel()) return;

        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        EnvCall.callWithParams(axeInstance, new Object[]{level}, "setLevel");

        cachedAxeLevel = level;
        data.setCurrentAxe("axe" + level);
        saveData();
    }

    public void addWood(int amount) {
        data.setWood(data.getWood() + amount);
        saveData();
    }
    
    public int getWood() {
        return data.getWood();
    }
    
    public int getUpgradeCost(int targetLevel) {
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        Object axeData = EnvCall.callReturn(axeInstance, "getAxeData");
        
        return (int) EnvCall.callReturnWithParams(
            axeData, 
            new Object[]{targetLevel}, 
            "getUpgradeCostForLevel"
        );
    }

    public void setWood(int amount) {
        data.setWood(amount);
    }

    public void setAxeLevel(int level) {
        cachedAxeLevel = level;
        data.setAxeLevel(level);
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        EnvCall.callWithParams(axeInstance, new Object[]{level}, "setLevel");
    }

    public int getAxeLevel() {
        return cachedAxeLevel;
    }

    public int getMaxUnlockedAxeLevel() {
        return data.getAxeLevel();
    }
    
    public int getEquippedAxeLevel() {
        return cachedAxeLevel;
    }

    private void saveData() {
        MainDataLoader.saveData(data);
    }
}
