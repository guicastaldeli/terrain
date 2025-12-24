package main.com.app.root.env.axes;

import main.com.app.root.env.EnvInstance;

public class AxeController implements EnvInstance<AxeController> { //Axe Controller
    @Override
    public AxeController getInstance() {
        return this;
    }
}
