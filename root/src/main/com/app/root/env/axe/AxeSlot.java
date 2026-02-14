package main.com.app.root.env.axe;

public class AxeSlot {
    public int level;
    public boolean unlocked;
    public boolean equipped;
        
    public AxeSlot(int level, int currentAxeLevel) {
        this.level = level;
        this.unlocked = level <= currentAxeLevel;
        this.equipped = level == currentAxeLevel;
    }
}