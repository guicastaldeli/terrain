package main.com.app.root.env.tree;
import main.com.app.root.Spawner;
import main.com.app.root.SpawnerData;
import main.com.app.root.SpawnerHandler;
import main.com.app.root.Tick;
import main.com.app.root.Upgrader;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.axe.AxeController;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.player.CharacterController;
import main.com.app.root.player.PlayerController;
import main.com.app.root.ui.UIController;
import main.com.app.root.ui.UIController.UIType;
import main.com.app.root.ui.info.Info;
import main.com.app.root.utils.TreeColors;
import java.util.List;
import org.joml.Vector3f;

public class TreeInteractor {
    private final Tick tick;
    private final PlayerController playerController;
    private final Spawner spawner;
    private final Upgrader upgrader;
    private final EnvController envController;
    private UIController uiController;

    private float swingTimer = 0f;
    private boolean isSwinging = false;
    private float swingCooldown = 0.5f;
    private float interactionRange = 200.0f;

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

    public void setUIController(UIController uiController) {
        this.uiController = uiController;
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
        List<SpawnerHandler> treeHandlers = spawner.spawnerData.get(SpawnerData.TREE);
        if(!treeHandlers.isEmpty()) {
            TreeSpawner treeSpawner = (TreeSpawner) treeHandlers.get(0);
            TreeController nearestTree = treeSpawner.getNearestTree(playerPos, interactionRange);
            if(nearestTree != null) {
                int treeLevel = nearestTree.getGenerator().getLevel();
                if(axe.canBreakTree(treeLevel)) {
                    CharacterController characterController = playerController.getCharacterController();
                    if(characterController != null) {
                        characterController.setAnimation(CharacterController.MovData.BREAK);
                    }
                }
                breakTree(axe, nearestTree);
            } else {
                System.out.println("No trees in range!");
            }
        }   
    }

    /**
     * Create Effect
     */
    public static void createTreeBreakEffect(
        Mesh mesh,
        Vector3f position, 
        int treeLevel
    ) {
        Vector3f colorLevel = TreeColors.getColorForLevel(treeLevel);
        Vector3f color = new Vector3f(colorLevel.x, colorLevel.y, colorLevel.z);
        
        int amount = 150 + (treeLevel * 2);
        float size = 1.0f + (treeLevel * 0.01f);
        float speed = 1.0f + (treeLevel * 0.2f);
        float lifetime = 2.5f + (treeLevel * 0.3f);
        
        mesh.getParticleManager()
            .create(
                position, 
                color, 
                amount, 
                size, 
                speed, 
                lifetime
            );

        Vector3f velNum = new Vector3f(10.0f, 10.0f, 10.0f);
        mesh.getParticleManager().getParticleSystem().setVelNum(velNum);
        mesh.getParticleManager().getParticleSystem().emit(position, true);
    }

    /**
     * Break Tree
     */
    private void breakTree(AxeController axe, TreeController tree) {
        Object treeGenerator = EnvCall.callReturn(tree, "getGenerator");
        if(treeGenerator == null) return;
        Object[] paramsUI = {uiController};
        EnvCall.callReturnWithParams(treeGenerator, paramsUI, "setUIController");

        Info info = (Info) uiController.get(UIType.INFO);

        int treeLevel = (int) EnvCall.callReturn(treeGenerator, "getLevel");
        if(!axe.canBreakTree(treeLevel)) {
            if(info != null) {
                info.setLevel(treeLevel);
                info.getInfoActions().showAxeLevelLow(axe.getLevel(), treeLevel);
            }
            return;
        }

        int damage = axe.calcDamage();
        Object[] params = new Object[]{ damage, axe.getLevel() };
        int woodDropped = (int) EnvCall.callReturnWithParams(treeGenerator, params, "takeDamage");
        if(woodDropped > 0) {
            int actualWood = axe.calcWoodDrop(woodDropped);
            upgrader.addWood(actualWood);
            info.setLevel(treeLevel);
            info.getInfoActions().showWoodCollected(actualWood);
        }
    }

    private void startSwinging(float speed) {
        isSwinging = true;
        swingCooldown = 1.0f / speed;
        swingTimer = swingCooldown;
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
