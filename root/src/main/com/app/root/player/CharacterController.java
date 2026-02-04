package main.com.app.root.player;
import main.com.app.root.Tick;
import main.com.app.root._resources.TextureLoader;
import main.com.app.root.mesh.Mesh;

public class CharacterController {
    public CharacterController instance;
    private Tick tick;
    public Mesh mesh;

    public int texOpen;
    public int texClosed;

    public CharacterController(Tick tick, Mesh mesh) {
        this.tick = tick;
        this.mesh = mesh;
        instance = this;
    }

    /**
     * 
     * Animation
     * 
     */
    public String meshName;
    private String currentAnimation;
    private MovData currentMovData;

    public void setMeshForAnimation(String meshName) {
        this.meshName = meshName;
    }

    public void setAnimation(MovData movData) {
        if(mesh != null && meshName != null) {
            mesh.getAnimationController().play(meshName, movData.getAnimName());
            currentMovData = movData;
        }
    }

    public String getCurrentAnimation() {
        return currentAnimation;
    }

    public MovData getCurrentMovData() {
        return currentMovData;
    }

    /**
     * 
     * Movement
     * 
     */
    public boolean wasMoving = false;
    public boolean wasInWater = false;

    public static enum MovData {
        IDLE("idle"),
        IDLE_MOV("idle_mov"),
        WALK("walk"),
        SWIM("swim"),
        BREAK("break");

        private String animName;

        MovData(String animName) {
            this.animName = animName;
        }

        public String getAnimName() {
            return animName;
        }
    }


    /**
     * 
     * Blink
     * 
     */
    public boolean isBlinking;
    public float blinkTimer = 0.0f;
    public float nextBlinkTime = 0.0f;
    public static final float BLINK_DURATION = 0.15f;
    public static final float MIN_BLINK_INTERVAL = 0.5f;
    public static final float MAX_BLINK_INTERVAL = 6.0f;

    public void replaceTex(String val, int id, String texPath) {
        if(val.equals("susie")) {
            String df = "susie_df";
            String ce = "susie_ce";
            texOpen = id;

            String closedTexPath = texPath.replace(df, ce);
            texClosed = TextureLoader.load(closedTexPath);
            if(texClosed <= 0) return;

            scheduleNextBlink();
        }
    }

    private void scheduleNextBlink() {
        nextBlinkTime = 
            MIN_BLINK_INTERVAL +
            (float)(Math.random() * MAX_BLINK_INTERVAL - MIN_BLINK_INTERVAL);
    }

    private void updateBlink() {
        float deltaTime = tick.getDeltaTime();

        if(isBlinking) {
            blinkTimer += deltaTime;
            if(blinkTimer >= BLINK_DURATION) {
                isBlinking = false;
                blinkTimer = 0.0f;
                mesh.setTex("susie", texOpen);
                scheduleNextBlink();
            }
        } else {
            blinkTimer += deltaTime;
            if(blinkTimer >= nextBlinkTime) {
                isBlinking = true;
                blinkTimer = 0.0f;
                mesh.setTex("susie", texClosed);
            }
        }
    }

    /**
     * 
     * Update
     * 
     */
    public void update() {
        updateBlink();
    }
}
