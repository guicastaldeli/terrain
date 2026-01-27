package main.com.app.root.player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f front;
    private Vector3f up;
    private Vector3f right;
    private Vector3f worldUp;

    private float yaw;
    private float pitch;

    private float fov;
    private float aspectRatio;
    private float zNear;
    private float zFar;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    private Matrix4f modelMatrix;

    private boolean viewMatrixNeedsUpdate;
    private boolean projectionMatrixNeedsUpdate;
    private boolean modelMatrixNeedsUpdate;

    public float distanceFromTarget = 1.5f;
    private float minDistance = 1.0f;
    private float maxDistance = 5000.0f;
    private Vector3f targetOffset = new Vector3f(0.0f, 1.0f, 0.0f);

    private boolean showCursor = false;
    private AimController aimController;

    public float posX = 0.0f;
    public float posY = 150.0f;
    public float posZ = 0.0f;

    public static final int RENDER_DISTANCE = 16;
    public static final float FOG = 650.0f;

    public Camera() {
        this.position = new Vector3f(posX, posY, posZ);
        this.worldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        this.yaw = 0.0f;
        this.pitch = 0.0f;

        this.front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.up = new Vector3f();
        this.right = new Vector3f();

        this.fov = 120.0f;
        this.aspectRatio = 16.0f / 9.0f;
        this.zNear = 0.1f;
        this.zFar = maxDistance;

        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
        this.modelMatrix = new Matrix4f();

        this.viewMatrixNeedsUpdate = true;
        this.projectionMatrixNeedsUpdate = true;
        this.modelMatrixNeedsUpdate = true;

        updateVectors();

        this.aimController = new AimController();
    }

    /**
     * Update Vectors
     */
    private void updateVectors() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        front.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        front.y = (float) Math.sin(pitchRad);
        front.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        front.normalize();

        front.cross(worldUp, right);
        right.normalize();

        right.cross(front, up);
        up.normalize();

        viewMatrixNeedsUpdate = true;
    }

    /**
     * Process Rotation
     */
    public void processRotation(
        float xOffset,
        float yOffset,
        boolean cPitch
    ) {
        float sensv = 0.3f;
        xOffset *= sensv;
        yOffset *= sensv;

        yaw += xOffset;
        pitch += yOffset;
        if(cPitch) {
            if(pitch > 89.0f) pitch = 89.0f;
            if(pitch < -89.0f) pitch = -89.0f;
        }

        updateVectors();
    }

    /**
     * Look At
     */
    public void lookAt(Vector3f target) {
        Vector3f dir = new Vector3f(target).sub(position).normalize();

        pitch = (float) Math.toDegrees(Math.asin(dir.y));
        yaw = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;

        updateVectors();
    }

    /**
     * Handle Mouse
     */
    public void handleMouse(float xOffset, float yOffset) {
        //System.out.println("Mouse: " + xOffset + ", " + yOffset);
        if(aimController.mode) return;
        processRotation(xOffset, yOffset, true);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        viewMatrixNeedsUpdate = true;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        if(pitch > 89.0f) this.pitch = 89.0f;
        if(pitch < -89.0f) this.pitch = -89.0f;
    
        updateVectors();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        projectionMatrixNeedsUpdate = true;
    }

    public Vector3f getFront() {
        return new Vector3f(front);
    }

    public Vector3f getRight() {
        return new Vector3f(right);
    }

    public Vector3f getUp() {
        return new Vector3f(up);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * 
     * View Matrix
     * 
     */
    public Matrix4f getViewMatrix() {
        if(viewMatrixNeedsUpdate) updateViewMatrix();
        return new Matrix4f(viewMatrix);
    }

    public void updatePlayerPos(Vector3f targetPosition) {
        Vector3f dPosition = calculatePlayerPos(targetPosition);
        position.set(dPosition);
        viewMatrixNeedsUpdate = true;
    }

    private Vector3f calculateCameraPos() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        Vector3f offset = new Vector3f();
        offset.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad)) * distanceFromTarget;
        offset.y = (float) Math.sin(pitchRad) * distanceFromTarget;
        offset.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad)) * distanceFromTarget;
        
        Vector3f cameraPos = new Vector3f(position).sub(offset);
        return cameraPos;
    }

    private Vector3f calculatePlayerPos(Vector3f targetPosition) {
        Vector3f targetPoint = new Vector3f(targetPosition).add(targetOffset);

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        Vector3f offset = new Vector3f();
        offset.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad)) * distanceFromTarget;
        offset.y = (float) Math.sin(pitchRad) * distanceFromTarget;
        offset.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad)) * distanceFromTarget;

        return new Vector3f(targetPoint).sub(offset);
    }

    public void updateViewMatrix() {
        Vector3f cameraPos = calculateCameraPos();
        Vector3f lookTarget = new Vector3f(position).add(targetOffset);
        viewMatrix
            .identity()
            .lookAt(
                cameraPos,
                lookTarget,
                up
            );
        viewMatrixNeedsUpdate = false;
    }

    /**
     * 
     * Projection Matrix
     * 
     */
    public Matrix4f getProjectionMatrix() {
        if(projectionMatrixNeedsUpdate) updateProjectionMatrix();
        return new Matrix4f(projectionMatrix);
    }

    private void updateProjectionMatrix() {
        float fovRad = (float) Math.toRadians(fov);
        projectionMatrix
            .identity()
            .perspective(
                fovRad, 
                aspectRatio, 
                zNear, 
                zFar
            );
        projectionMatrixNeedsUpdate = false;
    }

    /**
     * 
     * Model Matrix
     * 
     */
    public Matrix4f getModelMatrix() {
        if(modelMatrixNeedsUpdate) updateModelMatrix();
        return new Matrix4f(modelMatrix);
    }

    private void updateModelMatrix() {
        modelMatrix.identity();
        modelMatrixNeedsUpdate = true;
    }

    public void setModelMatrixTransform(
        Vector3f translation,
        Vector3f rotation,
        Vector3f scale
    ) {
        modelMatrix
            .identity()
            .translate(translation)
            .rotationXYZ(
                (float)Math.toRadians(rotation.x),
                (float)Math.toRadians(rotation.y),
                (float)Math.toRadians(rotation.z)
            )
            .scale(scale);
        modelMatrixNeedsUpdate = false;
    }

    /**
     * Distance
     */
    public void setDistance(float distance) {
        this.distanceFromTarget = Math.max(minDistance, Math.min(distance, maxDistance));
        viewMatrixNeedsUpdate = true;
    }

    public float getDistance() {
        return distanceFromTarget;
    }

    public void adjustDistance(float delta) {
        setDistance(distanceFromTarget + delta);
    }

    /**
     * Set Target Offset
     */
    public void setTargetOffset(float x, float y, float z) {
        targetOffset.set(x, y, z);
        viewMatrixNeedsUpdate = true;
    }

    public Vector3f getTargetOffset() {
        return new Vector3f(targetOffset);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    /**
     * Aim Controller
     */
    public AimController getAimController() {
        return aimController;
    }

    /**
     * Show Cursor
     */
    public void setShowCursor(boolean show) {
        this.showCursor = show;
    }

    public boolean shouldShowCursor() {
        return showCursor;
    }

    /**
     * 
     * Rotation Axis
     * 
     */
    public void rotateAroundAxis(Vector3f axis, float angleDegrees) {
        if(axis.length() == 0) return;
        axis.normalize();

        float angleRad = (float) Math.toRadians(angleDegrees);
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.rotation(angleRad, axis);

        rotationMatrix.transformDirection(front);
        front.normalize();
        front.cross(worldUp, right);
        right.normalize();
        right.cross(front, up);
        up.normalize();

        updateYawPitchFromFront();

        viewMatrixNeedsUpdate = true;
    }

    private void updateYawPitchFromFront() {
        pitch = (float) Math.toDegrees(Math.asin(front.y));
        yaw = (float) Math.toDegrees(Math.atan2(front.z, front.x));

        if(yaw < 0) yaw += 360.0f;
        if(yaw >= 360.0f) yaw -= 360.0f;
    }

    public void rotateAroundX(float angleDegrees) {
        rotateAroundAxis(new Vector3f(1.0f, 0.0f, 0.0f), angleDegrees);
    }
    
    public void rotateAroundY(float angleDegrees) {
        rotateAroundAxis(new Vector3f(0.0f, 1.0f, 0.0f), angleDegrees);
    }
    
    public void rotateAroundZ(float angleDegrees) {
        rotateAroundAxis(new Vector3f(0.0f, 0.0f, 1.0f), angleDegrees);
    }

    /**
     * 
     * Orbit Rotation
     * 
     */
    public void orbitAroundPoint(
        Vector3f point,
        Vector3f axis,
        float angleDegrees
    ) {
        if(axis.length() == 0) return;
        axis.normalize();
        
        float angleRad = (float) Math.toRadians(angleDegrees);
        Matrix4f rotationMatrix = new Matrix4f();
        rotationMatrix.rotate(angleRad, axis);

        Vector3f targetToCamera = new Vector3f(position).sub(point);
        rotationMatrix.transformPosition(targetToCamera);
        position.set(point).add(targetToCamera);

        updateVectorsForOrbit(point);

        viewMatrixNeedsUpdate = true;
    }

    private void updateVectorsForOrbit(Vector3f target) {
        front.set(target).sub(position).normalize();
        front.cross(worldUp, right);
        right.normalize();
        right.cross(front, up);
        up.normalize();

        updateYawPitchFromFront();

        viewMatrixNeedsUpdate = true;
    }

    @Override
    public String toString() {
        return "";
        /*
        return String.format("Camera[Pos: (%.2f, %.2f, %.2f), Yaw: %.2f, Pitch: %.2f, FOV: %.2f]",
                position.x, position.y, position.z, yaw, pitch, fov);
                */
    }
}
