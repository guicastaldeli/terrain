package main.com.app.root.screen_controller;

public interface ScreenHandler {
    default void render() {};
    default void handleAction(String action) {};
    default void handleKeyPress(int key, int action) {};
    default void onWindowResize(int width, int height) {};
}
