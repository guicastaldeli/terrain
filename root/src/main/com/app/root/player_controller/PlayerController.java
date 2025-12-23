package main.com.app.root.player_controller;
import main.com.app.root.Tick;
import main.com.app.root.Window;
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

    private float xPos = 0.0f;
    private float yPos = 10.0f;
    private float zPos = 5.0f;

    private float xSpeed = 0.0f;
    private float ySpeed = 0.0f;
    private float zSpeed = 0.0f;

    public PlayerController(
        Tick tick, 
        Window window,
        Mesh mesh
    ) {
        this.tick = tick;
        this.window = window;
        this.mesh = mesh;
        this.camera = new Camera();
        this.playerInputMap = new PlayerInputMap(this);
        
        this.set();

        this.camera.setAspectRatio(window.getAspectRatio());
        this.playerMesh = new PlayerMesh(
            tick, 
            this,
            mesh
        );
    }

    private void set() {
        this.position = new Vector3f(xPos, yPos, zPos);
        this.velocity = new Vector3f(xSpeed, ySpeed, zSpeed);
        this.movSpeed = 50.0f;
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

    /**
     * Update
     */
    public void update() {
        updateCameraPosition();
        if(playerMesh != null) playerMesh.update();
    }

    public void updatePosition(MovDir dir) {
        float vel = movSpeed * tick.getDeltaTime();
        
        Vector3f cameraFront = camera.getFront();
        Vector3f cameraRight = camera.getRight();
        Vector3f cameraUp = camera.getUp();
        
        Vector3f horizontalFront = new Vector3f(cameraFront.x, 0.0f, cameraFront.z).normalize();
        Vector3f horizontalRight = new Vector3f(cameraRight.x, 0.0f, cameraRight.z).normalize();
        
        switch(dir) {
            case FORWARD:
                position.add(horizontalFront.mul(vel));
                break;
            case BACKWARD:
                position.sub(horizontalFront.mul(vel));
                break;
            case LEFT:
                position.sub(horizontalRight.mul(vel));
                break;
            case RIGHT:
                position.add(horizontalRight.mul(vel));
                break;
            case UP:
                position.add(cameraUp.mul(vel));
                break;
            case DOWN:
                position.sub(cameraUp.mul(vel));
                break;
        }
        
        //System.out.println("Player moved to: " + position);
        updatePlayerMeshRotation(dir);
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

    public PlayerMesh getPlayerMesh() {
        return playerMesh;
    }

    public void render() {
        if(playerMesh != null) playerMesh.render();
    }
}
