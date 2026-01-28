package main.com.app.root.player;
import main.com.app.root.Spawner;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root.Upgrader;
import main.com.app.root.Window;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.env.EnvCall;
import main.com.app.root.env.EnvController;
import main.com.app.root.env.EnvData;
import main.com.app.root.env.world.Water;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.DataController;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;

public class PlayerController {
    public enum MovDir {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

    private final Tick tick;
    private final Window window;
    private final Camera camera;
    private final PlayerInputMap playerInputMap;
    private final Mesh mesh;
    private final Spawner spawner;
    private final Upgrader upgrader;
    private final EnvController envController;
    private final DataController dataController;
    private final StateController stateController;
    
    private Vector3f position;
    private Vector3f velocity;
    private float movSpeed; 
    
    private PlayerMesh playerMesh;
    private RigidBody rigidBody;
    private CollisionManager collisionManager;

    private float sizeX = 1.0f;
    private float sizeY = 10.0f;
    private float sizeZ = 1.0f;

    private float xSpeed = 0.0f;
    private float ySpeed = 0.0f;
    private float zSpeed = 0.0f;

    private float jumpForce = 15.0f;
    private boolean onJump = false;

    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public boolean flyMode = false;
    private float flySpeed = 200.0f;
    private boolean movingUp = false;
    private boolean movingDown = false;

    public PlayerController(
        Tick tick, 
        Window window,
        Mesh mesh,
        CollisionManager collisionManager,
        Spawner spawner,
        Upgrader upgrader,
        EnvController envController,
        DataController dataController,
        StateController stateController,
        boolean autoSet
    ) {
        this.tick = tick;
        this.window = window;
        this.mesh = mesh;
        this.camera = new Camera();
        this.spawner = spawner;
        this.upgrader = upgrader;
        this.envController = envController;
        this.dataController = dataController;
        this.stateController = stateController;
        this.playerInputMap = new PlayerInputMap(
            tick,
            this,
            spawner,
            upgrader,
            envController
        );
        this.collisionManager = collisionManager;

        if(autoSet) this.set();

        this.camera.setAspectRatio(window.getAspectRatio());
        this.playerMesh = new PlayerMesh(
            tick, 
            this,
            mesh
        );
    }

    public void set() {
        Vector3f savedPos = dataController.getPlayerPos();
        System.out.println("DataController saved position: " + savedPos);

        boolean isNewSave = 
            stateController.isLoadInProgress() &&
            stateController.getCurrentSaveId() != null;

        if(position == null) {
            position = new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y, 
                camera.getPosition().z
            );
        }

        if(savedPos != null && !isNewSave) {
            this.position = new Vector3f(
                savedPos.x,
                savedPos.y,
                savedPos.z
            );
        } else {
            this.position = new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y, 
                camera.getPosition().z
            );
        }

        this.velocity = new Vector3f(xSpeed, ySpeed, zSpeed);
        this.movSpeed = 80.0f;

        this.rigidBody = new RigidBody(
            tick,
            new Vector3f(position),
            new Vector3f(
                sizeX, 
                sizeY, 
                sizeZ
            )
        );

        addCollider();
        updateCameraPosition(); 
    }

    /**
     * Position
     */
    public Vector3f getPosition() {
        if(position == null) {
            this.position = new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y, 
                camera.getPosition().z
            );
        } 
        return new Vector3f(position);
    }

    public void setPosition(float x, float y, float z) {
        if(position == null) {
            position = new Vector3f(x, y, z);
        } else {
            position.set(x, y, z);
        }
        rigidBody.setPosition(new Vector3f(x, y, z));
        if(dataController != null) {
            dataController.setPlayerPos(new Vector3f(position));
        }
        updateCameraPosition();
    }

    /**
     * Get Camera
     */
    public Camera getCamera() {
        return camera;
    } 

    /**
     * Get Input Map
     */
    public PlayerInputMap getInputMap() {
        return playerInputMap;
    }

    private void applyMov() {
        Vector3f targetVel = new Vector3f();
        float speed = flyMode ? flySpeed : movSpeed;

        Vector3f cameraFront = camera.getFront();
        Vector3f cameraRight = camera.getRight();

        Vector3f horizontalFront = new Vector3f(cameraFront.x, 0.0f, cameraFront.z).normalize();
        Vector3f horizontalRight = new Vector3f(cameraRight.x, 0.0f, cameraRight.z).normalize();
        
        if(movingForward) targetVel.add(horizontalFront.mul(speed));
        if(movingBackward) targetVel.sub(horizontalFront.mul(speed));
        if(movingLeft) targetVel.sub(horizontalRight.mul(speed));
        if(movingRight) targetVel.add(horizontalRight.mul(speed));
        if(targetVel.lengthSquared() > 0) targetVel.normalize().mul(speed);

        Vector3f currentVel = rigidBody.getVelocity();

        if(flyMode) {
            if(movingUp) targetVel.y = speed;
            else if(movingDown) targetVel.y = -speed;
            else targetVel.y = 0;
            rigidBody.setVelocity(targetVel);
        } else {
            rigidBody.setVelocity(new Vector3f(
                targetVel.x,
                currentVel.y,
                targetVel.z
            ));
        }

        if(targetVel.length() > 0) {
            updateMeshRotation();
        }
    }

    public void jump() {
        onJump = true;
    }

    /**
     * Apply Movement Forces
     */
    private void applyMovForces() {
        Vector3f cameraFront = camera.getFront();
        Vector3f cameraRight = camera.getRight();
        Vector3f cameraUp = camera.getUp();

        Vector3f horizontalFront = new Vector3f(
            cameraFront.x,
            0.0f,
            cameraFront.z
        ).normalize();
        Vector3f horizontalRight = new Vector3f(
            cameraRight.x,
            0.0f,
            cameraRight.z
        ).normalize();
    }

    /**
     * Add Collider
     */
    private void addCollider() {
        if(collisionManager != null && rigidBody != null) {
            DynamicObject collider = new DynamicObject(rigidBody, "PLAYER");
            collisionManager.addDynamicCollider(collider);
        } else {
            System.err.println("Cannot create player collider!");
            if(collisionManager == null) System.err.println("collisionManager is null");
            if(rigidBody == null) System.err.println("rigidBody is null");
        }
    }

    /**
     * Get Player Mesh
     */
    public PlayerMesh getPlayerMesh() {
        return playerMesh;
    }

    /**
     * Get Rigid Body
     */
    public RigidBody getRigidBody() {
        return rigidBody;
    }

    /**
     * Fly Mode
     */
    public void toggleFlyMode() {
        flyMode = !flyMode;
        if(flyMode ){
            rigidBody.setGravityEnabled(false);

            Vector3f vel = rigidBody.getVelocity();
            if(vel.y < 0) vel.y = 0;

            rigidBody.setVelocity(vel);
            rigidBody.setOnGround(false);
            System.out.println("Fly mode: ON");
        } else {
            rigidBody.setGravityEnabled(true);
            movingUp = false;
            movingDown = false;
            
            Vector3f vel = rigidBody.getVelocity();
            if(vel.y > 0) vel.y = 0;
            rigidBody.setVelocity(vel);
            
            System.out.println("Fly mode: OFF");
        }
    }

    public boolean isInFlyMode() {
        return flyMode;
    }
 
    /**
     * Update
     */
    public void update() {
        float deltaTime = tick.getDeltaTime();

        applyMov();

        if(envController != null && envController.getEnv(EnvData.MAP) != null) {
            Vector3f pos = getPosition();
            Object[] params = { pos.x, pos.z };
            
            Object worldController = envController.getEnv(EnvData.MAP);
            if(worldController != null) {
                Object generator = EnvCall.callReturn(worldController, "getGenerator");
                if(generator != null) {
                    EnvCall.callWithParams(generator, params, "update");
                }
            }
        }
        
        if(onJump) {
            if(rigidBody.isOnGround()) {
                Vector3f currVel = rigidBody.getVelocity();
                currVel.y = jumpForce;
                rigidBody.setVelocity(currVel);
                onJump = false;
            }
        }

        rigidBody.update();
        Vector3f newPos = rigidBody.getPosition();
        if(newPos.y < Water.LEVEL) {
            newPos.y = Water.LEVEL;
            rigidBody.setPosition(newPos);
        }
        
        position.set(newPos);
        updateCameraPosition();
        if(playerMesh != null) playerMesh.update();
        updateAxePosition();

        /*
        System.out.println(
            "X:" + rigidBody.getPosition().x +
            "Y:" + rigidBody.getPosition().y +
            "Z:" + rigidBody.getPosition().z
        );
        */
    }

    private void updateMeshRotation() {
        if(playerMesh == null) return;
        
        if(movingForward && !movingBackward && !movingLeft && !movingRight) {
            updatePlayerMeshRotation(MovDir.FORWARD);
        } else if(movingBackward && !movingForward && !movingLeft && !movingRight) {
            updatePlayerMeshRotation(MovDir.BACKWARD);
        } else if(movingLeft && !movingRight && !movingForward && !movingBackward) {
            updatePlayerMeshRotation(MovDir.LEFT);
        } else if(movingRight && !movingLeft && !movingForward && !movingBackward) {
            updatePlayerMeshRotation(MovDir.RIGHT);
        }
        else if(movingForward) {
            updatePlayerMeshRotation(MovDir.FORWARD);
        }
    }

    public void updatePosition(MovDir dir, boolean isPressed) {
        switch(dir) {
            case FORWARD:
                movingForward = isPressed;
                break;
            case BACKWARD:
                movingBackward = isPressed;
                break;
            case LEFT:
                movingLeft = isPressed;
                break;
            case RIGHT:
                movingRight = isPressed;
                break;
            case UP:
                if(flyMode) {
                    movingUp = isPressed;
                } else {
                    if(isPressed && !onJump) {
                        onJump = true;
                    }
                }
                break;
            case DOWN:
                if(flyMode) movingDown = isPressed;
                break;
        }
    }

    private void updatePlayerMeshRotation(MovDir dir) {
        if(playerMesh == null) return;

        Camera camera = getCamera();
        Vector3f cameraFront = camera.getFront();
        float yaw = camera.getYaw();
        float targetRotation = 0.0f;

        switch(dir) {
            case FORWARD:
                targetRotation = 180.0f - yaw;
                break;
            case BACKWARD:
                targetRotation = 360.0f - yaw;
                break;
            case LEFT:
                targetRotation = 270.0f - yaw;
                break;
            case RIGHT:
                targetRotation = 90.0f - yaw;
                break;
            default:
                targetRotation = 180.0f - yaw;
                break;
        }

        targetRotation = targetRotation % 360.0f;
        if(targetRotation < 0) targetRotation += 360.0f;
        playerMesh.setMeshRotation(
            playerMesh.getMeshRotation().x,
            targetRotation,
            playerMesh.getMeshRotation().z
        );
    }

    private void updateCameraPosition() {
        camera.updatePlayerPos(position);
    }

    public void updateAspectRatio() {
        camera.setAspectRatio(window.getAspectRatio());
        if(window != null) {
            long windowHandle = window.getWindow();
            glfwSetFramebufferSizeCallback(windowHandle, (
                window,
                width,
                height
            ) -> {
                updateAspectRatio();
            });
        }
    }

    public void updateAxePosition() {
        if(envController != null && envController.getEnv(EnvData.AXE) != null) {
            Object axeInstance = envController.getEnv(EnvData.AXE).getInstance();
            if(axeInstance != null) {
                EnvCall.callWithParams(axeInstance, new Object[]{getPosition()}, "setPosition");
            }
        }
    }

    /**
     * Render
     */
    public void render() {
        if(playerMesh != null) playerMesh.render();
    }

    /**
     * Reset
     */
    public void reset() {
        Object axeController = envController != null ? envController.getEnv(EnvData.AXE) : null;
        if(axeController != null) EnvCall.call(axeController, "reset");
    }
}