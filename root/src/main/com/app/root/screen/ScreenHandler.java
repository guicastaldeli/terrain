package main.com.app.root.screen;

public interface ScreenHandler {
    default void render() {};
    default void update() {};
    default void handleAction(String action) {};
    default void handleKeyPress(int key, int action) {};
    default void handleMouse() {};
    default void onWindowResize(int width, int height) {};
}
