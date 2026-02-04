package main.com.app.root.player;
import org.joml.Vector3f;

public class PlayerDirection {
    /**
     * 
     * Direction
     * 
     */
    public enum Direction {
        NORTH(90.0f),
        NORTHEAST(45.0f),
        EAST(0.0f),
        SOUTHEAST(-45.0f),
        SOUTH(-90.0f),
        SOUTHWEST(-135.0f),
        WEST(180.0f),
        NORTHWEST(135.0f);

        private final float angle;

        Direction(float angle) {
            this.angle = angle;
        }

        public float getAngle() {
            return angle;
        }
    }

    private Direction currentDirection;
    private float currentAngle;
    private float targetAngle;
    private float displayAngle;
    private float lerpSpeed = 1000.0f;

    public PlayerDirection() {
        this.currentDirection = Direction.NORTH;
        this.currentAngle = Direction.NORTH.getAngle();
        this.targetAngle = this.currentAngle;
        this.displayAngle = this.currentAngle;
    }

    /**
     * Lerp Speed
     */
    public void setLerpSpeed(float speed) {
        this.lerpSpeed = speed;
    }

    public float getLerpSpeed() {
        return lerpSpeed;
    }

    /**
     * 
     * Update Direction
     * 
     */
    public boolean updateDirection(
        boolean movForward,
        boolean movBackward,
        boolean movLeft,
        boolean movRight
    ) {
        Direction newDirection = calcDirection(
            movForward,
            movBackward,
            movLeft,
            movRight
        );
        if(newDirection == null) {
            return false;
        }

        if(newDirection != currentDirection) {
            currentDirection = newDirection;
            currentAngle = newDirection.getAngle();
            targetAngle = newDirection.getAngle();
            return true;
        }

        return false;
    }

    /**
     * Update Lerp
     */
    public void updateLerp(float deltaTime) {
        if(Math.abs(displayAngle - targetAngle) > 0.1f) {
            float diff = targetAngle - displayAngle;

            while(diff > 180.0f) diff -= 360.0f;
            while(diff < -180.0f) diff += 360.0f;

            float step = lerpSpeed * deltaTime;
            if(Math.abs(diff) < step) {
                displayAngle = targetAngle;
            } else {
                displayAngle += Math.signum(diff) * step;
            }

            while(displayAngle > 180.0f) displayAngle -= 360.0f;
            while(displayAngle < -180.0f) displayAngle += 360.0f;
        } else {
            displayAngle = targetAngle;
        }
    }

    /**
     * Get Current Direction
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    
    /**
     * Get Current Angle
     */
    public float getCurrentAngle() {
        return currentAngle;
    }

    /**
     * Is Moving
     */
    public boolean isMoving() {
        return currentDirection != null;
    }

    /**
     * 
     * Calculate Direction
     * 
     */
    public Direction calcDirection(
        boolean forward,
        boolean backward,
        boolean left,
        boolean right
    ) {
        if(!forward && !backward && !left && !right) {
            return null;
        }
        
        /* Single Direction */
        if(forward && !backward && !left && !right) {
            return Direction.NORTH;
        }
        if(backward && !forward && !left && !right) {
            return Direction.SOUTH;
        }
        if(left && !right && !forward && !backward) {
            return Direction.WEST;
        }
        if(right && !left && !forward && !backward) {
            return Direction.EAST;
        }
        
        /* Diagonal */
        if(forward && right && !backward && !left) {
            return Direction.NORTHEAST;
        }
        if(forward && left && !backward && !right) {
            return Direction.NORTHWEST;
        }
        if(backward && right && !forward && !left) {
            return Direction.SOUTHEAST;
        }
        if(backward && left && !forward && !right) {
            return Direction.SOUTHWEST;
        }
        
        if(forward && backward) {
            if(left) return Direction.WEST;
            if(right) return Direction.EAST;
            return null;
        }
        
        if(left && right) {
            if(forward) return Direction.NORTH;
            if(backward) return Direction.SOUTH;
            return null;
        }
        
        return null;
    }

    /**
     * 
     * Get Rotation For Camera
     * 
     */
    public float getRotationForCamera(float cameraYaw) {
        float rotation = cameraYaw + displayAngle;

        rotation = rotation % 360.0f;
        if(rotation < 0) rotation += 360.0f;
        return rotation;
    }

    /**
     * 
     * Get Direction Vector
     * 
     */
    public Vector3f getDirectionVector() {
        float radians = (float) Math.toRadians(displayAngle);
        return new Vector3f(
            (float) Math.sin(radians),
            0.0f,
            (float) Math.cos(radians)
        ).normalize();
    }

    /**
     * 
     * Reset
     * 
     */
    public void reset() {
        currentDirection = Direction.NORTH;
        currentAngle = Direction.NORTH.getAngle();
        targetAngle = Direction.NORTH.getAngle();
    }

    @Override
    public String toString() {
        return currentDirection != null 
            ? String.format("%s (%.1f°)", currentDirection.name(), currentAngle)
            : "NONE";
    }
}
