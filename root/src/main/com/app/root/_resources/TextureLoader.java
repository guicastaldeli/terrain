package main.com.app.root._resources;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureLoader {
    public static int load(String filePath) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer img = STBImage.stbi_load(
                filePath, 
                width, 
                height, 
                channels, 
                4
            );
            if(img == null) {
                System.err.println("STBImage failed to load: " + STBImage.stbi_failure_reason());
                return -1;
            }

            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(
                GL_TEXTURE_2D, 
                0, 
                GL_RGBA, 
                width.get(0), 
                height.get(0), 
                0, 
                GL_RGBA, 
                GL_UNSIGNED_BYTE, 
                img
            );
            
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(img);
            glBindTexture(GL_TEXTURE_2D, 0);
            
            //System.out.println("Texture created with ID: " + texId);
            return texId;
        } catch(Exception err) {
            err.printStackTrace();
            return -1;
        }
    }    
}