package main.com.app.root;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;

public class Upgrader {
    private MainData data;
    private EnvController envController;
    private int cachedEquippedAxeLevel;

    public Upgrader(EnvController envController) {
        this.data = new MainData();
        if(envController != null) {
            this.envController = envController;
            Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
            this.cachedEquippedAxeLevel = (int) EnvCall.callReturn(axeInstance, "getLevel");
        }
    }

    public void setEnvController(EnvController envController) {
        this.envController = envController;
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        this.cachedEquippedAxeLevel = (int) EnvCall.callReturn(axeInstance, "getLevel");
    }

    /**
     * 
     * Upgrade
     * 
     */
    public boolean canUpgrade() {
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        int currLevel = (int) EnvCall.callReturn(axeInstance, "getLevel");
        int upgradeCost = (int) EnvCall.callReturn(axeInstance, "getUpgradeCost");

        return data.getWood() >= upgradeCost;
    }

    public boolean upgradeAxe(int targetLevel) {
        if(targetLevel <= getMaxUnlockedAxeLevel() || targetLevel > 10) return false;
        
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        
        int totalCost = 0;
        for(int level = getMaxUnlockedAxeLevel() + 1; level <= targetLevel; level++) {
            totalCost += getUpgradeCost(level);
        }
        
        if(data.getWood() < totalCost) return false;
        data.setWood(data.getWood() - totalCost);
        
        EnvCall.callWithParams(axeInstance, new Object[]{targetLevel}, "setLevel");
        
        cachedEquippedAxeLevel = targetLevel;
        data.setAxeLevel(targetLevel);
        data.setCurrentAxe("axe" + targetLevel);

        saveData();
        
        System.out.println("Upgraded to level " + targetLevel + "!");
        return true;
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

    /**
     * 
     * Wood
     * 
     */
    public void addWood(int amount) {
        data.setWood(data.getWood() + amount);
        saveData();
    }
    
    public int getWood() {
        return data.getWood();
    }

    public void setWood(int amount) {
        data.setWood(amount);
    }

    /**
     * 
     * Axe
     * 
     */
    public void equipAxe(int level) {
        if(level < 0 || level > data.getAxeLevel()) return;

        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        EnvCall.callWithParams(axeInstance, new Object[]{level}, "setLevel");

        cachedEquippedAxeLevel = level;
        data.setCurrentAxe("axe" + level);
        saveData();
    }

    public void setAxeLevel(int level) {
        cachedEquippedAxeLevel = level;
        data.setAxeLevel(level);
        Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
        EnvCall.callWithParams(axeInstance, new Object[]{level}, "setLevel");
    }

    public int getMaxUnlockedAxeLevel() {
        return data.getAxeLevel();
    }
    
    public int getEquippedAxeLevel() {
        return cachedEquippedAxeLevel;
    }

    public int getAxeLevel() {
        return data.getAxeLevel();
    }

    /**
     * 
     * Data
     * 
     */
    private void saveData() {
        MainDataLoader.saveData(data);
    }

    public MainData getData() {
        return data;
    }

    public void setData(MainData newData) {
        this.data = newData;
        if(newData.getCurrentAxe() != null && !newData.getCurrentAxe().isEmpty()) {
            String axeName = newData.getCurrentAxe();
            if(axeName.startsWith("axe")) {
                try {
                    this.cachedEquippedAxeLevel = Integer.parseInt(axeName.substring(3));
                } catch(NumberFormatException e) {
                    this.cachedEquippedAxeLevel = newData.getAxeLevel();
                }
            } else {
                this.cachedEquippedAxeLevel = newData.getAxeLevel();
            }
        } else {
            this.cachedEquippedAxeLevel = newData.getAxeLevel();
        }
    }

    /**
     * Reset
     */
    public void reset() {
        this.data = new MainData();
        this.cachedEquippedAxeLevel = 0;
        if(envController != null) {
            Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
            EnvCall.callWithParams(axeInstance, new Object[]{0}, "setLevel");
        }
    }
}