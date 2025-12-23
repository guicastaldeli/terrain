package main.com.app.root.env;
import main.com.app.root.DataController;
import main.com.app.root.DependencyValue;
import main.com.app.root.StateController;
import main.com.app.root.Tick;
import main.com.app.root._shaders.ShaderProgram;
import main.com.app.root.collision.CollisionManager;
import main.com.app.root.mesh.Mesh;
import main.com.app.root.mesh.MeshRenderer;

public class EnvDependencyList {
    @DependencyValue(required = true)
    public static final Class<?> TICK = Tick.class;

    @DependencyValue(required = true)
    public static final Class<?> MESH = Mesh.class;

    @DependencyValue(required = true)
    public static final Class<?> MESH_RENDERER = MeshRenderer.class;

    @DependencyValue(required = true)
    public static final Class<?> SHADER_PROGRAM = ShaderProgram.class;

    @DependencyValue(required = true)
    public static final Class<?> DATA_CONTROLLER = DataController.class;

    @DependencyValue(required = true)
    public static final Class<?> STATE_CONTROLLER = StateController.class;

    @DependencyValue(required = true)
    public static final Class<?> COLLISION_MANAGER = CollisionManager.class;
}
