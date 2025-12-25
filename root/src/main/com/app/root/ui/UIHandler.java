package main.com.app.root.ui;

public interface UIHandler {
    default void render() {};
    default void handleAction(String action) {};
    default void handleKeyPress(int key, int action) {};
    default void onWindowResize(int width, int height) {};
    default void onShow() {};
    default void onHide() {};
    default void update() {};
}
