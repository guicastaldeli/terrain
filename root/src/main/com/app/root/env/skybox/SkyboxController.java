package main.com.app.root.env.skybox;
import main.com.app.root.env.EnvInstance;

public class SkyboxController implements EnvInstance<SkyboxController> {
    @Override
    public SkyboxController getInstance() {
        return this;
    }
}
