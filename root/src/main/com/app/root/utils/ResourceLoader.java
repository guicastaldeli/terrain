package main.com.app.root.utils;
import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.*;

/**
 *  ---- ResourceLoader - Auto-detects if running from JAR or dev environment
 *  ---- and loads resources accordingly!. 
 */
public class ResourceLoader {
    private static final File TEMP_DIR;
    private static final boolean isJar;

    static {
        String className = ResourceLoader.class.getName().replace('.', '/');
        String classJar = ResourceLoader.class.getResource("/" + className + ".class").toString();
        isJar = classJar.startsWith("jar:");
        
        if(isJar) {
            try {
                TEMP_DIR = Files.createTempDirectory("terrain_game_").toFile();
                TEMP_DIR.deleteOnExit();
                System.out.println("Created temp directory for resources: " + TEMP_DIR.getAbsolutePath());
            } catch(IOException e) {
                throw new RuntimeException("Failed to create temp directory", e);
            }
        } else {
            TEMP_DIR = null;
        }
    }

    /**
     * Load Font to Buffer
     */
    public static ByteBuffer loadFontToBuffer(String path) throws IOException {
        if(isJar) {
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
            if(stream == null) {
                throw new IOException("Font not found in JAR: " + path);
            }

            try {
                byte[] fontBytes = stream.readAllBytes();
                ByteBuffer buffer = BufferUtils.createByteBuffer(fontBytes.length + 1);
                buffer.put(fontBytes);
                buffer.flip();
                stream.close();
                return buffer;
            } catch(IOException e) {
                stream.close();
                throw e;
            }
        } else {
            Path fontPath = Paths.get(path);
            if(!Files.exists(fontPath)) {
                throw new IOException("Font file not found: " + path);
            }

            byte[] fontBytes = Files.readAllBytes(fontPath);
            ByteBuffer buffer = BufferUtils.createByteBuffer(fontBytes.length + 1);
            buffer.put(fontBytes);
            buffer.flip();
            return buffer;
        }
    }

    /**
     * Get Native Path
     */
    public static String getNativeResourcePath(String resourcePath) throws IOException {
        if(isJar) {
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if(stream == null) {
                throw new IOException("Resource not found in JAR: " + resourcePath);
            }

            String[] pathParts = resourcePath.split("/");
            String fileName = pathParts[pathParts.length - 1];
            
            File targetDir = TEMP_DIR;
            for(int i = 0; i < pathParts.length - 1; i++) {
                targetDir = new File(targetDir, pathParts[i]);
            }
            targetDir.mkdirs();
            
            File tempFile = new File(targetDir, fileName);
            
            if(!tempFile.exists()) {
                Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.deleteOnExit();
            }
            stream.close();
            
            return tempFile.getAbsolutePath();
        } else {
            File file = new File(resourcePath);
            if(!file.exists()) {
                throw new IOException("File not found: " + resourcePath);
            }
            return file.getAbsolutePath();
        }
    }

    /**
     * Get Resource Stream
     */
    public static InputStream getResourceStream(String path) throws IOException {
        if(isJar) {
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
            if(stream == null) {
                throw new IOException("Resource not found in JAR: " + path);
            }
            return stream;
        } else {
            Path filePath = Paths.get(path);
            if(!Files.exists(filePath)) {
                throw new IOException("File not found: " + path);
            }
            return Files.newInputStream(filePath);
        }
    }

    /**
     * Get XML Stream
     */
    public static InputStream getXMLStream(String path) throws IOException {
        return getResourceStream(path);
    }

    /**
     * Get External Path
     */
    public static String getExternalPath(String relativePath) {
        if(isJar) {
            try {
                String jarPath = ResourceLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
                File jarFile = new File(jarPath);
                File externalFile = new File(jarFile.getParent(), relativePath);
                return externalFile.getAbsolutePath();
            } catch(Exception e) {
                return new File(relativePath).getAbsolutePath();
            }
        } else {
            return new File(relativePath).getAbsolutePath();
        }
    }

    /**
     * Read All Bytes from Resource
     */
    public static byte[] readAllBytesFromResource(String resourcePath) throws IOException {
        if(isJar) {
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            if(stream == null) {
                throw new IOException("Resource not found in JAR: " + resourcePath);
            }
            try {
                return stream.readAllBytes();
            } finally {
                stream.close();
            }
        } else {
            Path filePath = Paths.get(resourcePath);
            if(!Files.exists(filePath)) {
                throw new IOException("File not found: " + resourcePath);
            }
            return Files.readAllBytes(filePath);
        }
    }

    public static boolean isRunningFromJar() {
        return isJar;
    }
}