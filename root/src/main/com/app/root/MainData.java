package main.com.app.root;
import java.io.Serializable;

public class MainData implements Serializable {
    private static final long serialVersionUID = 1L;

    public int wood;
    public int axeLevel;
    private String currAxe;
    private int[] upgradeCosts;
    private float[] damageIncrease;
    private float[] speedIncrease;
    private float[] woodMultiplier;

    public MainData() {
        this.wood = 0;
        this.axeLevel = 0;
        this.currAxe = "axe0";
        this.upgradeCosts = new int[10];
        this.damageIncrease = new float[10];
        this.speedIncrease = new float[10];
        this.woodMultiplier = new float[10];
        
        for(int i = 0; i < 10; i++) {
            upgradeCosts[i] = 100 * (i + 1);
            damageIncrease[i] = 10 + (i * 5);
            speedIncrease[i] = 1.0f + (i * 0.2f);
            woodMultiplier[i] = 1.0f + (i * 0.2f);
        }
    }

    /**
     * Wood
     */
    public int getWood() { 
        return wood; 
    }
    public void setWood(int wood) { 
        this.wood = Math.max(0, wood); 
    }
    public void addWood(int amount) {
        this.wood += Math.max(0, amount);
    }

    /**
     * Axe
     */
    public int getAxeLevel() { 
        return axeLevel; 
    }
    public String getCurrentAxe() { 
        return currAxe; 
    }
    public void setAxeLevel(int level) { 
        this.axeLevel = Math.max(0, Math.min(9, level)); 
        this.currAxe = "axe" + this.axeLevel;
    }
    public void upgradeAxe() {
        if(axeLevel < 9) {
            axeLevel++;
            currAxe = "axe" + axeLevel;
        }
    }
    public void setCurrentAxe(String axe) { 
        this.currAxe = axe; 
    }
    public float[] getWoodMultiplier() { 
        return woodMultiplier.clone(); 
    }
    public void removeWood(int amount) {
        this.wood = Math.max(0, this.wood - amount);
    }
    public void setWoodMultiplier(float[] multiplier) { 
        if(multiplier != null && multiplier.length == 10) {
            this.woodMultiplier = multiplier.clone();
        }
    }

    /**
     * Upgrade Costs
     */
    public int[] getUpgradeCosts() { 
        return upgradeCosts.clone(); 
    }
    public void setUpgradeCosts(int[] costs) { 
        if(costs != null && costs.length == 10) {
            this.upgradeCosts = costs.clone();
        }
    }

    /**
     * Damage Increase
     */
    public float[] getDamageIncrease() { 
        return damageIncrease.clone(); 
    }
    public void setDamageIncrease(float[] damage) { 
        if(damage != null && damage.length == 10) {
            this.damageIncrease = damage.clone();
        }
    }
    
    /**
     * Speed Increase
     */
    public float[] getSpeedIncrease() { 
        return speedIncrease.clone(); 
    }
    
    
    
    public void setSpeedIncrease(float[] speed) { 
        if(speed != null && speed.length == 10) {
            this.speedIncrease = speed.clone();
        }
    }

    /**
     * Current Upgrade Cost
     */
    public int getCurrentUpgradeCost() {
        if(axeLevel < upgradeCosts.length) {
            return upgradeCosts[axeLevel];
        }
        return 0;
    }

    /**
     * Current Damage
     */
    public float getCurrentDamage() {
        if(axeLevel < damageIncrease.length) {
            return damageIncrease[axeLevel];
        }
        return damageIncrease[damageIncrease.length - 1];
    }

    /**
     * Current Speed
     */
    public float getCurrentSpeed() {
        if(axeLevel < speedIncrease.length) {
            return speedIncrease[axeLevel];
        }
        return speedIncrease[speedIncrease.length - 1];
    }

    /**
     * Current Wood Multiplier
     */
    public float getCurrentWoodMultiplier() {
        if(axeLevel < woodMultiplier.length) {
            return woodMultiplier[axeLevel];
        }
        return woodMultiplier[woodMultiplier.length - 1];
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("return {\n");
        sb.append("    player = {\n");
        sb.append("        wood = ").append(wood).append(",\n");
        sb.append("        axe_level = ").append(axeLevel).append(",\n");
        sb.append("        current_axe = \"").append(currAxe).append("\",\n");
        sb.append("        inventory = {\n");
        sb.append("            wood = ").append(wood).append("\n");
        sb.append("        }\n");
        sb.append("    },\n");
        sb.append("    upgrades = {\n");
        
        sb.append("        costs = {");
        for(int i = 0; i < upgradeCosts.length; i++) {
            sb.append(upgradeCosts[i]);
            if(i < upgradeCosts.length - 1) sb.append(", ");
        }
        sb.append("},\n");
        
        sb.append("        damage_increase = {");
        for(int i = 0; i < damageIncrease.length; i++) {
            sb.append(damageIncrease[i]);
            if(i < damageIncrease.length - 1) sb.append(", ");
        }
        sb.append("},\n");
        
        sb.append("        speed_increase = {");
        for(int i = 0; i < speedIncrease.length; i++) {
            sb.append(speedIncrease[i]);
            if(i < speedIncrease.length - 1) sb.append(", ");
        }
        sb.append("},\n");
        
        sb.append("        wood_multiplier = {");
        for(int i = 0; i < woodMultiplier.length; i++) {
            sb.append(woodMultiplier[i]);
            if(i < woodMultiplier.length - 1) sb.append(", ");
        }
        sb.append("}\n");
        
        sb.append("    }\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    public void print() {
        System.out.println("\n=== GAME DATA ===");
        System.out.println("Wood: " + wood);
        System.out.println("Axe Level: " + axeLevel);
        System.out.println("Current Axe: " + currAxe);
        System.out.println("Current Damage: " + getCurrentDamage());
        System.out.println("Current Speed: " + getCurrentSpeed());
        System.out.println("Wood Multiplier: " + getCurrentWoodMultiplier());
        System.out.println("Next Upgrade Cost: " + getCurrentUpgradeCost());
        System.out.println("=================\n");
    }
}
