package main.com.app.root.env.tress;
import main.com.app.root.env.EnvInstance;

public class TreeController implements EnvInstance<TreeController> { //Tree Controller
    @Override
    public TreeController getInstance() {
        return this;
    }
}
