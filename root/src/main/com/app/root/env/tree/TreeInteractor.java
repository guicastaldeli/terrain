package main.com.app.root.env.tree;
import main.com.app.root.Spawner;
import main.com.app.root.Tick;
import main.com.app.root.Upgrader;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.axe.AxeController;
import main.com.app.root.player_controller.PlayerController;
import org.joml.Vector3f;

public class TreeInteractor {
    private final Tick tick;
    private final PlayerController playerController;
    private final Spawner spawner;
    private final Upgrader upgrader;
    private final EnvController envController;

    private float swingTimer = 0f;
    private boolean isSwinging = false;
    private float swingCooldown = 0.5f;
    private float interactionRange = 5.0f;

    public TreeInteractor(
        Tick tick,
        PlayerController playerController,
        Spawner spawner,
        Upgrader upgrader,
        EnvController envController
    ) {
        this.tick = tick;
        this.playerController = playerController;
        this.spawner = spawner;
        this.upgrader = upgrader;
        this.envController = envController;
    }

    /**
     * Attempt Break
     */
    public void attemptBreak() {
        if(isSwinging || swingTimer > 0) return;

        Object axeEnv = envController.getEnv(EnvData.AXE);
        if(axeEnv == null) return;

        Object axeInstance = EnvCall.callReturn(axeEnv, "getInstance");
        if(axeInstance == null) return;

        AxeController axe = (AxeController) axeInstance;
        float swingSpeed = axe.getSwingSpeed();

        startSwinging(swingSpeed);

        Vector3f playerPos = playerController.getPosition();
        TreeController nearestTree = spawner.getNearestTree(playerPos, interactionRange);
        if(nearestTree != null) {
            breakTree(axe, nearestTree);
        } else {
            System.out.println("No trees in range!");
        }
    }

    /**
     * Break Tree
     */
    private void breakTree(AxeController axe, TreeController tree) {
        Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
        if(treeGenerator == null) return;

        int treeLevel = (int) EnvCall.callReturn(treeGenerator, "getLevel");
        if(!axe.canBreakTree(treeLevel)) {
            System.out.println("Axe level " + axe.getLevel() + " too low for tree level " + treeLevel);
            return;
        }

        int damage = axe.calcDamage();
        Object[] params = new Object[]{ damage, axe.getLevel() };
        int woodDropped = (int) EnvCall.callReturnWithParams(treeGenerator, params, "takeDamage");
        if(woodDropped > 0) {
            int actualWood = axe.calcWoodDrop(woodDropped);
            upgrader.addWood(actualWood);
            System.out.println("+ " + actualWood + " wood! (Total: " + upgrader.getWood() + ")");
            //Visuals later
        }
    }

    private void startSwinging(float speed) {
        isSwinging = true;
        swingCooldown = 1.0f / speed;
        swingTimer = swingCooldown;

        //Do the anim...
        System.out.println("Swinging axe...");
    }

    public boolean isSwinging() {
        return isSwinging;
    }

    public float getSwingProgress() {
        if(swingCooldown <= 0) return 1.0f;
        return 1.0f - (swingTimer / swingCooldown);
    }

    public void setInteractionRange(float range) {
        this.interactionRange = range;
    }

    /**
     * Update
     */
    public void update() {
        float deltaTime = tick.getDeltaTime();
        if(swingTimer > 0) {
            swingTimer -= deltaTime;
            if(swingTimer <= 0) isSwinging = false;
        }
    }
}
