package main.com.app.root.env;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NoiseGeneratorWrapper {
    private static final String DLL_PATH = "root/src/main/com/app/root/env/_noise/.build/";
    
    static {
        loadNativeLibraries();
    }
    
    private static void loadNativeLibraries() {
        try {
            Path directory = Paths.get(DLL_PATH);
            if (!Files.exists(directory)) {
                throw new RuntimeException("dll directory does not exist: " + directory.toAbsolutePath());
            }
            //System.out.println("Files in dll directory:");
            try {
                Files.list(directory)
                    .filter(path -> path.toString().toLowerCase().endsWith(".dll"))
                    .forEach(path -> System.out.println("  - " + path.getFileName()));
            } catch (Exception err) {
                System.out.println("error directory" + err.getMessage());
            }

            String[] libraries = {
                "libcrypto-3-x64.dll",
                "libssl-3-x64.dll", 
                "noise_generator.dll"
            };
            
            for(String lib : libraries) {
                Path libPath = directory.resolve(lib);
                if (!Files.exists(libPath)) {
                    System.err.println("Missing required DLL: " + libPath.toAbsolutePath());
                    throw new RuntimeException("Required DLL not found: " + lib);
                }
                //System.out.println("Found: " + libPath.toAbsolutePath());
            }
            for(String lib : libraries) {
                Path libPath = directory.resolve(lib);
                try {
                    System.load(libPath.toAbsolutePath().toString());
                    //System.out.println("Successfully loaded: " + lib);
                } catch (UnsatisfiedLinkError e) {
                    System.err.println("Failed to load: " + lib);
                    System.err.println("Error: " + e.getMessage());
                    throw e;
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
            throw new RuntimeException("Failed to load native libraries: " + err.getMessage());
        }
    }

    public native boolean generateMap(String outputhPath, long seed);
    public native boolean loadMapData(String filePath);

    public native float[] getHeightMapData();
    public native int[] getIndicesData();
    public native float[] getNormalsData();
    public native float[] getColorsData();

    public native int getMapWidth();
    public native int getMapHeight();
    public native int getVertexCount();
    public native int getIndexCount();

    public native float[] getPointData();
    public native int getPointCount();
}