package main.com.app.root.env;
import main.com.app.root.DependencyContainer;
import main.com.app.root.env.axe.AxeController;
import main.com.app.root.env.clouds.CloudController;
import main.com.app.root.env.map.MapController;
import main.com.app.root.env.skybox.SkyboxController;
import main.com.app.root.env.tree.TreeController;

public enum EnvData {
    SKYBOX(SkyboxController.class),
    CLOUD(CloudController.class),
    MAP(MapController.class),
    TREE(TreeController.class),
    AXE(AxeController.class);

    private final Class<? extends EnvInstance<?>> instance;

    EnvData(Class<? extends EnvInstance<?>> instance) {
        this.instance = instance;
    }

    public EnvInstance<?> createInstance(DependencyContainer dependencyContainer) {
        try {
            return dependencyContainer.createInstance(this.instance);
        } catch(Exception err) {
            throw new RuntimeException("Failed to create " + this.name(), err);
        }
    }

    public<T> T getInstance(DependencyContainer dependencyContainer) {
        return (T) createInstance(dependencyContainer).getInstance();
    }
}
