package main.com.app.root.player;

import org.joml.Vector3f;

public class PlayerDirection {
    /**
     * 
     * Direction
     * 
     */
    public enum Direction {
        NORTH(0.0f), //0
        NORTHEAST(225.0f),
        EAST(200.0f), //0
        SOUTHEAST(135.0f),
        SOUTH(150.0f),
        SOUTHWEST(225.0f),
        WEST(150.0f), //0
        NORTHWEST(45.0f); //0

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

    public PlayerDirection() {
        this.currentDirection = Direction.NORTH;
        this.currentAngle = Direction.NORTH.getAngle();
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
            return true;
        }

        return false;
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
        float rotation = 180.0f - cameraYaw + currentAngle;

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
        float radians = (float) Math.toRadians(currentAngle);
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
    }

    @Override
    public String toString() {
        return currentDirection != null 
            ? String.format("%s (%.1f°)", currentDirection.name(), currentAngle)
            : "NONE";
    }
}
