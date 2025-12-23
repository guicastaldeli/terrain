package main.com.app.root.player_controller;
import main.com.app.root.Tick;
import main.com.app.root.Window;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.collision.types.DynamicObject;
import main.com.app.root.mesh.Mesh;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import org.joml.Vector3f;

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
    private PlayerMesh playerMesh;

    private Vector3f position;
    private Vector3f velocity;
    private float movSpeed; 

    private RigidBody rigidBody;
    private CollisionManager collisionManager;

    private float sizeX = 1.0f;
    private float sizeY = 2.0f;
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

    public PlayerController(
        Tick tick, 
        Window window,
        Mesh mesh,
        CollisionManager collisionManager
    ) {
        this.tick = tick;
        this.window = window;
        this.mesh = mesh;
        this.camera = new Camera();
        this.playerInputMap = new PlayerInputMap(this);
        this.collisionManager = collisionManager;

        this.set();

        this.camera.setAspectRatio(window.getAspectRatio());
        this.playerMesh = new PlayerMesh(
            tick, 
            this,
            mesh
        );
        addCollider();
    }

    private void set() {
        this.position = new Vector3f(
            camera.getPosition().x,
            camera.getPosition().y, 
            camera.getPosition().z
        );
        this.velocity = new Vector3f(xSpeed, ySpeed, zSpeed);
        this.movSpeed = 40.0f;

        this.rigidBody = new RigidBody(
            tick,
            new Vector3f(
                camera.getPosition().x,
                camera.getPosition().y, 
                camera.getPosition().z
            ),
            new Vector3f(sizeX, sizeY, sizeZ)
        );
        rigidBody.setGravityScale(2.0f);
        updateCameraPosition(); 
    }

    /**
     * Position
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
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
        Vector3f moveForce = new Vector3f();
        float force = movSpeed * rigidBody.getMass();
        
        Vector3f cameraFront = camera.getFront();
        Vector3f cameraRight = camera.getRight();
        
        Vector3f horizontalFront = new Vector3f(cameraFront.x, 0.0f, cameraFront.z).normalize();
        Vector3f horizontalRight = new Vector3f(cameraRight.x, 0.0f, cameraRight.z).normalize();
        
        if(movingForward) moveForce.add(horizontalFront.mul(force));
        if(movingBackward) moveForce.sub(horizontalFront.mul(force));
        if(movingLeft) moveForce.sub(horizontalRight.mul(force));
        if(movingRight) moveForce.add(horizontalRight.mul(force));
        if(moveForce.length() > 0) {
            moveForce.normalize().mul(force);
            rigidBody.applyForce(moveForce);
            updateMeshRotation();
        }
    }

    /**
     * Update
     */
    public void update() {
        float deltaTime = tick.getDeltaTime();

        applyMov();
        
        if(onJump && rigidBody.isOnGround()) {
            rigidBody.applyForce(
                new Vector3f(
                    0,
                    jumpForce * rigidBody.getMass(),
                    0
                )
            );
            onJump = false;
        }

        rigidBody.update();
        position.set(rigidBody.getPosition());

        updateCameraPosition();
        if(playerMesh != null) playerMesh.update();
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
                if(isPressed) jump();
                break;
            case DOWN:
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

    public void jump() {
        onJump = true;
    }

    public RigidBody getRigidBody() {
        return rigidBody;
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

    public PlayerMesh getPlayerMesh() {
        return playerMesh;
    }

    public void render() {
        if(playerMesh != null) playerMesh.render();
    }
}
