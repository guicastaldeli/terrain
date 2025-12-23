package main.com.app.root.env.clouds;

import main.com.app.root.env.EnvInstance;

public class CloudController implements EnvInstance<CloudController> {
    @Override
    public CloudController getInstance() {
        return this;
    }
}
