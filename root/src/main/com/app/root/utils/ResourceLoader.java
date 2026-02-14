package main.com.app.root.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.lwjgl.BufferUtils;

/**
 * ResourceLoader - Auto-detects if running from JAR or dev environment
 * and loads resources accordingly. No code changes needed!
 */
public class ResourceLoader {
    
    private static final boolean IS_JAR;
    
    static {
        // Detect if running from JAR
        String className = ResourceLoader.class.getName().replace('.', '/');
        String classJar = ResourceLoader.class.getResource("/" + className + ".class").toString();
        IS_JAR = classJar.startsWith("jar:");
    }
    
    /**
     * Load a font file as ByteBuffer (for STB TrueType)
     * Works in both dev (filesystem) and JAR (resources)
     */
    public static ByteBuffer loadFontToBuffer(String path) throws IOException {
        if (IS_JAR) {
            // Running from JAR - load from resources
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
            if (stream == null) {
                throw new IOException("Font not found in JAR: " + path);
            }
            
            try {
                byte[] fontBytes = stream.readAllBytes();
                ByteBuffer buffer = BufferUtils.createByteBuffer(fontBytes.length + 1);
                buffer.put(fontBytes);
                buffer.flip();
                stream.close();
                return buffer;
            } catch (IOException e) {
                stream.close();
                throw e;
            }
        } else {
            // Running in dev - use filesystem
            Path fontPath = Paths.get(path);
            if (!Files.exists(fontPath)) {
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
     * Get InputStream for any resource
     * Works in both dev and JAR
     */
    public static InputStream getResourceStream(String path) throws IOException {
        if (IS_JAR) {
            // Running from JAR - load from resources
            InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream(path);
            if (stream == null) {
                throw new IOException("Resource not found in JAR: " + path);
            }
            return stream;
        } else {
            // Running in dev - use filesystem
            Path filePath = Paths.get(path);
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + path);
            }
            return Files.newInputStream(filePath);
        }
    }
    
    /**
     * Get File path for XML parsing
     * Works in both dev and JAR
     */
    public static InputStream getXMLStream(String path) throws IOException {
        return getResourceStream(path);
    }
    
    /**
     * Get absolute path to external directory (like natives/ or saves/)
     * These are OUTSIDE the JAR
     */
    public static String getExternalPath(String relativePath) {
        if (IS_JAR) {
            // When in JAR, external files are relative to JAR location
            try {
                String jarPath = ResourceLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
                File jarFile = new File(jarPath);
                File externalFile = new File(jarFile.getParent(), relativePath);
                return externalFile.getAbsolutePath();
            } catch (Exception e) {
                // Fallback to current directory
                return new File(relativePath).getAbsolutePath();
            }
        } else {
            // In dev, use the path as-is
            return new File(relativePath).getAbsolutePath();
        }
    }
    
    /**
     * Check if running from JAR
     */
    public static boolean isRunningFromJar() {
        return IS_JAR;
    }
}