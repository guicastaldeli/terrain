package main.com.app.root.ui.upgrade_menu;

public class UpgradeMenuActions {
    private final UpgradeMenu upgradeMenu;

    public UpgradeMenuActions(UpgradeMenu upgradeMenu) {
        this.upgradeMenu = upgradeMenu;
    }

    /**
     * Equip Axe
     */
    public void equipAxe(int level) {
        if(level < 0 || level > 10) return;

        int currentLevel = upgradeMenu.getCurrentAxeLevel();
        if(level <= currentLevel) {
            upgradeMenu.setCurrentAxeLevel(currentLevel);
            upgradeMenu.refreshAxeSlots();
            System.out.println("Equipped axe level " + level);
        }
    }

    /**
     * Upgrade Axe
     */
    public void upgradeAxe(int targetLevel) {
        if(upgradeMenu.getUpgrader() == null) return;

        int currentLevel = upgradeMenu.getCurrentAxeLevel();
        if(targetLevel != currentLevel + 1) {
            System.out.println("Can only upgrade to next level!");
            return;
        }

        int upgradeCost = upgradeMenu.getUpgrader().getUpgradeCost(targetLevel);
        int playerWood = upgradeMenu.getUpgrader().getWood();
        if(playerWood >= upgradeCost) {
            boolean success = upgradeMenu.getUpgrader().upgradeAxe();
            if(success) {
                upgradeMenu.setCurrentAxeLevel(targetLevel);
                upgradeMenu.refreshAxeSlots();

                System.out.println("Upgraded to axe level " + targetLevel);
            }
        } else {
            System.out.println("Not enough wood! Need " + upgradeCost + ", have " + playerWood);
        }
    }
}
