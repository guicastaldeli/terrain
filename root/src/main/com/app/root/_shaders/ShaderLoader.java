package main.com.app.root._shaders;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ShaderLoader {
    private static final Map<String, String> loadedShaders = new HashMap<>();
    private static final String INCLUDE_PREFIX = "#include ";
    private static final String DIR = "main/com/app/root/_shaders/";

    /**
     * Load File
     */
    public static String load(String fileName) throws IOException {
        if(loadedShaders.containsKey(fileName)) {
            return loadedShaders.get(fileName);
        }

        String content = loadFile(fileName);
        content = processIncludes(content, fileName);
        loadedShaders.put(fileName, content);

        System.out.println(content);
        return content;
    }

    private static String loadFile(String fileName) throws IOException {
        InputStream stream = ShaderLoader.class
            .getClassLoader()
            .getResourceAsStream(DIR + fileName);
        if(stream == null) {
            throw new IOException("Shader file not found!: " + fileName);
        }
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Process Includes
     */
    private static String processIncludes(String content, String parentFile) throws IOException {
        StringBuilder res = new StringBuilder();
        Scanner scanner = new Scanner(content);
        String parentDir = getParentDir(parentFile);

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if(line.startsWith(INCLUDE_PREFIX)) {
                String file = line.substring(INCLUDE_PREFIX.length()).trim();
                file = file.replace("\"", "").replace("'", "");

                String path;
                // Handle absolute paths (starting with /)
                if(file.startsWith("/")) {
                    path = file.substring(1);
                } 
                // Handle relative paths (containing ../ or ./)
                else if(file.contains("../") || file.contains("./")) {
                    path = resolveRelativePath(parentDir, file);
                } 
                // Handle simple filenames in same directory
                else {
                    path = parentDir + file;
                }

                String includeContent;
                try {
                    includeContent = loadFile(path);
                } catch(IOException err) {
                    // Fallback: try the path as-is
                    try {
                        path = file;
                        includeContent = loadFile(path);
                    } catch(IOException err2) {
                        throw new IOException("Could not find shader include: " + file + 
                            " (tried: " + path + " and " + file + ")", err);
                    }
                }

                includeContent = processIncludes(includeContent, path);
                includeContent = stripVersionDirective(includeContent);
                res.append(includeContent).append("\n");
            } else {
                res.append(line).append("\n");
            }
        }

        scanner.close();
        return res.toString();
    }

    private static String getParentDir(String path) {
        int lastSlash = path.lastIndexOf('/');
        if(lastSlash > 0) {
            return path.substring(0, lastSlash + 1);
        }
        return "";
    }

    private static String resolveRelativePath(String baseDir, String relativePath) {
        // Split the base directory into parts
        String[] baseParts = baseDir.split("/");
        String[] relativeParts = relativePath.split("/");
        
        // Build result path, handling ../ and ./
        StringBuilder result = new StringBuilder();
        int baseDepth = baseParts.length;
        
        // Count how many levels to go up
        int upCount = 0;
        int relativeStart = 0;
        for(int i = 0; i < relativeParts.length; i++) {
            if(relativeParts[i].equals("..")) {
                upCount++;
                relativeStart = i + 1;
            } else if(relativeParts[i].equals(".")) {
                relativeStart = i + 1;
            } else {
                break;
            }
        }
        
        // Add base path minus the upCount directories
        for(int i = 0; i < baseDepth - upCount; i++) {
            if(!baseParts[i].isEmpty()) {
                result.append(baseParts[i]).append("/");
            }
        }
        
        // Add remaining relative path parts
        for(int i = relativeStart; i < relativeParts.length; i++) {
            result.append(relativeParts[i]);
            if(i < relativeParts.length - 1) {
                result.append("/");
            }
        }
        
        return result.toString();
    }

    private static String stripVersionDirective(String content) {
        Scanner scanner = new Scanner(content);
        StringBuilder res = new StringBuilder();
        boolean versionFound = false;

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();
            // Skip all version directives except the first one we encounter
            if(trimmed.startsWith("#version")) {
                if(!versionFound) {
                    res.append(line).append("\n");
                    versionFound = true;
                }
                // Skip subsequent version directives
            } else {
                res.append(line).append("\n");
            }
        }

        scanner.close();
        return res.toString();
    }

    public static void clearCache() {
        loadedShaders.clear();
    }

    public static String getDir() {
        return DIR;
    }
}