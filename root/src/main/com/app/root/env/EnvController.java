package main.com.app.root.env;
import main.com.app.root.DependencyContainer;
import java.util.HashMap;
import java.util.Map;

public class EnvController {
    public final DependencyContainer dependencyContainer;
    private final Map<EnvData, EnvInstance<?>> envInstance;

    public EnvController(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        this.envInstance = new HashMap<>();
    }

    /**
     * Get Env
     */
    public EnvInstance<?> getEnv(EnvData type) {
        return envInstance.computeIfAbsent(type,
            t -> t.createInstance(dependencyContainer)
        );
    }

    /**
     * Load Env
     */
    public Map<EnvData, EnvInstance<?>> loadEnv() {
        return new HashMap<>(envInstance);
    }
}
