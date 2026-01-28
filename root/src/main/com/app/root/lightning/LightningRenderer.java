package main.com.app.root.lightning;
import main.com.app.root._shaders.ShaderProgram;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class LightningRenderer {
    private final LightningController lightningController;
    private final ShaderProgram shaderProgram;

    private static final int MAX_POINT_LIGHTS = 32;
    
    public LightningRenderer(LightningController lightningController, ShaderProgram shaderProgram) {
        this.lightningController = lightningController;
        this.shaderProgram = shaderProgram;
    }
    
    public void updateShaderUniforms(Vector3f cameraPosition) {
        if(!lightningController.isEnabled()) {
            setDefaultLighting();
            return;
        }
        
        /* Ambient Lights */
        List<Light> ambientLights = lightningController.getLights(LightningData.AMBIENT);
        if(!ambientLights.isEmpty()) {
            AmbientLight ambient = (AmbientLight) ambientLights.get(0);
            shaderProgram.setUniform("uAmbientLight.color", 
                ambient.getColor().x, 
                ambient.getColor().y, 
                ambient.getColor().z
            );
            shaderProgram.setUniform("uAmbientLight.intensity", ambient.getIntensity());
        } else {
            shaderProgram.setUniform("uAmbientLight.color", 1.0f, 1.0f, 1.0f);
            shaderProgram.setUniform("uAmbientLight.intensity", 0.3f);
        }

        /* Directional Lights */
        List<Light> directionalLights = lightningController.getLights(LightningData.DIRECTIONAL);
        if (!directionalLights.isEmpty()) {
            DirectionalLight directional = (DirectionalLight) directionalLights.get(0);
            shaderProgram.setUniform("uDirectionalLight.color", 
                directional.getColor().x, 
                directional.getColor().y, 
                directional.getColor().z
            );
            shaderProgram.setUniform("uDirectionalLight.intensity", directional.getIntensity());
            shaderProgram.setUniform("uDirectionalLight.direction", 
                directional.getDirection().x, 
                directional.getDirection().y, 
                directional.getDirection().z
            );
            shaderProgram.setUniform("uDirectionalLight.range", directional.getRange());
            shaderProgram.setUniform("uDirectionalLightOrigin", 
                DirectionalLight.DEFAULT_DIRECTION.x,
                DirectionalLight.DEFAULT_DIRECTION.y,
                DirectionalLight.DEFAULT_DIRECTION.z
            );
        } else {
            shaderProgram.setUniform("uDirectionalLight.color", 1.0f, 1.0f, 1.0f);
            shaderProgram.setUniform("uDirectionalLight.intensity", 0.0f);
            shaderProgram.setUniform("uDirectionalLight.direction", 0.0f, -1.0f, 0.0f);
            shaderProgram.setUniform("uDirectionalLight.range", 100.0f);
            shaderProgram.setUniform("uDirectionalLightOrigin", 0.0f, 0.0f, 0.0f);
        }
        
        /* Point Lights */
        List<Light> pointLights = lightningController.getLights(LightningData.POINT);
        List<PointLight> sortedLights = new ArrayList<>();
        for(Light light : pointLights) {
            sortedLights.add((PointLight) light);
        }

        sortedLights.sort((light1, light2) -> {
            float dist1 = light1.getPosition().distance(cameraPosition);
            float dist2 = light2.getPosition().distance(cameraPosition);
            return Float.compare(dist1, dist2);
        });

        int lightCount = Math.min(sortedLights.size(), MAX_POINT_LIGHTS);
        shaderProgram.setUniform("uPointLightCount", lightCount);

        for(int i = 0; i < lightCount; i++) {
            PointLight point = sortedLights.get(i);
            String uPrefix = "uPointLights[" + i + "].";

            shaderProgram.setUniform(uPrefix + "color", 
                point.getColor().x, 
                point.getColor().y, 
                point.getColor().z
            );
            shaderProgram.setUniform(uPrefix + "intensity", point.getIntensity());
            shaderProgram.setUniform(uPrefix + "position", 
                point.getPosition().x,
                point.getPosition().y,
                point.getPosition().z
            );
            shaderProgram.setUniform(uPrefix + "radius", point.getRadius());
            shaderProgram.setUniform(uPrefix + "attenuation", point.getAttenuation());
        }

        for(int i = lightCount; i < MAX_POINT_LIGHTS; i++) {
            String uPrefix = "uPointLights[" + i + "].";
            shaderProgram.setUniform(uPrefix + "intensity", 0.0f);
        }
    }
    
    private void setDefaultLighting() {
        shaderProgram.setUniform("uAmbientLight.color", 1.0f, 1.0f, 1.0f);
        shaderProgram.setUniform("uAmbientLight.intensity", 1.0f);
        shaderProgram.setUniform("uDirectionalLight.intensity", 0.0f);
        shaderProgram.setUniform("uPointLight.intensity", 0.0f);
    }
}