package main.com.app.root.lightning;
import main.com.app.root._shaders.ShaderProgram;
import java.util.List;

public class LightningRenderer {
    private final LightningController lightningController;
    private final ShaderProgram shaderProgram;
    
    public LightningRenderer(LightningController lightningController, ShaderProgram shaderProgram) {
        this.lightningController = lightningController;
        this.shaderProgram = shaderProgram;
    }
    
    public void updateShaderUniforms() {
        if (!lightningController.isEnabled()) {
            setDefaultLighting();
            return;
        }
        
        List<Light> ambientLights = lightningController.getLights(LightningData.AMBIENT);
        if (!ambientLights.isEmpty()) {
            AmbientLight ambient = (AmbientLight) ambientLights.get(0);
            shaderProgram.setUniform("uAmbientLight.color", ambient.getColor().x, ambient.getColor().y, ambient.getColor().z);
            shaderProgram.setUniform("uAmbientLight.intensity", ambient.getIntensity());
        } else {
            shaderProgram.setUniform("uAmbientLight.color", 1.0f, 1.0f, 1.0f);
            shaderProgram.setUniform("uAmbientLight.intensity", 0.3f);
        }

        List<Light> directionalLights = lightningController.getLights(LightningData.DIRECTIONAL);
        if (!directionalLights.isEmpty()) {
            DirectionalLight directional = (DirectionalLight) directionalLights.get(0);
            shaderProgram.setUniform("uDirectionalLight.color", directional.getColor().x, directional.getColor().y, directional.getColor().z);
            shaderProgram.setUniform("uDirectionalLight.intensity", directional.getIntensity());
            shaderProgram.setUniform("uDirectionalLight.direction", directional.getDirection().x, directional.getDirection().y, directional.getDirection().z);
        } else {
            shaderProgram.setUniform("uDirectionalLight.color", 1.0f, 1.0f, 1.0f);
            shaderProgram.setUniform("uDirectionalLight.intensity", 5.0f);
            shaderProgram.setUniform("uDirectionalLight.direction", 0.0f, 0.0f, 0.0f);
        }
        
        List<Light> pointLights = lightningController.getLights(LightningData.POINT);
        if (!pointLights.isEmpty()) {
            PointLight point = (PointLight) pointLights.get(0);
            shaderProgram.setUniform("uPointLight.color", point.getColor().x, point.getColor().y, point.getColor().z);
            shaderProgram.setUniform("uPointLight.intensity", point.getIntensity());
            shaderProgram.setUniform("uPointLight.position", point.getPosition().x, point.getPosition().y, point.getPosition().z);
            shaderProgram.setUniform("uPointLight.radius", point.getRadius());
            shaderProgram.setUniform("uPointLight.attenuation", point.getAttenuation());
        } else {
            shaderProgram.setUniform("uPointLight.intensity", 0.0f);
        }
    }
    
    private void setDefaultLighting() {
        shaderProgram.setUniform("uAmbientLight.color", 1.0f, 1.0f, 1.0f);
        shaderProgram.setUniform("uAmbientLight.intensity", 1.0f);
        shaderProgram.setUniform("uDirectionalLight.intensity", 0.0f);
        shaderProgram.setUniform("uPointLight.intensity", 0.0f);
    }
}