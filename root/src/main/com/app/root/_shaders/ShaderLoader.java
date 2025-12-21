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
                if(file.contains("/")) {
                    path = file.substring(1);
                } else if(file.contains("/")){
                    path = resolveRelativePath(parentDir, file);
                } else {
                    path =parentDir + file;
                }

                String includeContent;
                try {
                    includeContent = loadFile(path);
                } catch(IOException err) {
                    path = file;
                    includeContent = loadFile(path);
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

    private static String resolveRelativePath(String dir, String path) {
        return dir + path;
    }

    private static String stripVersionDirective(String content) {
        Scanner scanner = new Scanner(content);
        StringBuilder res = new StringBuilder();
        boolean firstLine = true;

        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String trimmed = line.trim();
            if(firstLine && trimmed.startsWith("#version")) {
                res.append(line).append("\n");
                firstLine = false;
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
