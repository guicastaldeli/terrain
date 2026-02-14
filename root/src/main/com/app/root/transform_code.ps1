param(
    [string]$ModifiedSrcDir
)

$ErrorActionPreference = 'Stop'

Write-Host "Starting code transformations..."
Write-Host "Target directory: $ModifiedSrcDir"
Write-Host ""

$javaFiles = Get-ChildItem -Path $ModifiedSrcDir -Filter *.java -Recurse
$luaFiles = Get-ChildItem -Path $ModifiedSrcDir -Filter *.lua -Recurse
$allFiles = $javaFiles + $luaFiles

Write-Host "Processing $($javaFiles.Count) Java files and $($luaFiles.Count) Lua files..."

$pathCount = 0
$fontCount = 0
$xmlCount = 0
$dllCount = 0
$meshLoaderCount = 0
$modelMapCount = 0
$objLoaderCount = 0
$gltfLoaderCount = 0
$animationLoaderCount = 0

foreach ($file in $allFiles) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    $fileModified = $false
    $isLuaFile = $file.Extension -eq '.lua'

    # 1. PATH REPLACEMENTS - Apply to ALL files containing root/src/
    if ($content -match 'root/src/') {
        Write-Host "  -> Found 'root/src/' in: $($file.Name)"
        
        # Font directory paths
        $content = $content -replace 'root/src/main/com/app/root/_font/', 'main/com/app/root/_font/'
        
        # Saves directory - external folder
        $content = $content -replace 'root/src/main/com/app/root/_resources/saves/', 'saves/'
        
        # DLL directory - external natives folder
        $content = $content -replace 'root/src/main/com/app/root/env/_noise/\.build', 'natives'
        $content = $content -replace 'main/com/app/root/env/_noise/\.build', 'natives'
        
        # All other resources - remove root/src/
        $content = $content -replace 'root/src/', ''
        
        $pathCount++
        $fileModified = $true
    }

    # Skip Java-specific transformations for Lua files
    if ($isLuaFile) {
        if ($fileModified) {
            Set-Content -Path $file.FullName -Value $content -NoNewline
        }
        continue
    }

    # 2. FONTLOADER - Replace FileChannel with ResourceLoader
    if ($file.Name -eq 'FontLoader.java') {
        if ($content -match 'FileChannel') {
            Write-Host "  -> Transforming FontLoader.java (font loading)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\._text_renderer;)', "`$1`r`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Use a simpler pattern that matches the whitespace-flexible structure
            $pattern = 'try\s*\(\s*FileChannel\s+fc\s*=\s*FileChannel\.open\s*\(\s*Paths\.get\s*\(\s*fontPath\s*\)\s*,\s*StandardOpenOption\.READ\s*\)\s*\)\s*\{\s+fontData\s*=\s*BufferUtils\.createByteBuffer\s*\(\s*\(\s*int\s*\)\s*fc\.size\s*\(\s*\)\s*\+\s*1\s*\)\s*;\s+fc\.read\s*\(\s*fontData\s*\)\s*;\s+fontData\.flip\s*\(\s*\)\s*;\s+\}'
            $replacement = 'fontData = ResourceLoader.loadFontToBuffer(fontPath);'
            
            $content = $content -replace $pattern, $replacement
            
            $fontCount++
            $fileModified = $true
        }
    }

    # 3. DOCPARSER - Replace File with InputStream
    if ($file.Name -eq 'DocParser.java') {
        if ($content -match 'builder\.parse\(new File\(filePath\)\)') {
            Write-Host "  -> Transforming DocParser.java (XML parsing)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Replace all builder.parse(new File(filePath)) calls
            $content = $content -replace 'builder\.parse\(\s*new\s+File\s*\(\s*filePath\s*\)\s*\)', 'builder.parse(ResourceLoader.getXMLStream(filePath))'
            
            $xmlCount++
            $fileModified = $true
        }
    }

    # 4. NOISEGENERATORWRAPPER - Use external path for natives
    if ($file.Name -eq 'NoiseGeneratorWrapper.java') {
        if ($content -match 'DLL_PATH|\.build|noise_generator\.dll') {
            Write-Host "  -> Transforming NoiseGeneratorWrapper.java (DLL loading)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\.env;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;`nimport java.io.File;"
            }
            
            # Replace the DLL_PATH constant value
            $content = $content -replace 'private static final String DLL_PATH = "root/src/main/com/app/root/env/_noise/\.build/";', 'private static final String DLL_PATH = ResourceLoader.getExternalPath("natives");'
            $content = $content -replace 'private static final String DLL_PATH = "[^"]*env/_noise/\.build[^"]*";', 'private static final String DLL_PATH = ResourceLoader.getExternalPath("natives");'
            
            # Replace Paths.get(DLL_PATH) with new File(DLL_PATH).toPath()
            $content = $content -replace 'Path directory = Paths\.get\(DLL_PATH\);', 'Path directory = new File(DLL_PATH).toPath();'
            
            $dllCount++
            $fileModified = $true
        }
    }

    # 5. TEXTURELOADER - Wrap paths with ResourceLoader for STBImage
    if ($file.Name -eq 'TextureLoader.java') {
        if ($content -match 'STBImage\.stbi_load') {
            Write-Host "  -> Transforming TextureLoader.java (texture loading)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\._resources;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Wrap STBImage.stbi_load filePath parameter with ResourceLoader.getNativeResourcePath
            # Pattern: STBImage.stbi_load(filePath, ...
            $content = $content -replace 'STBImage\.stbi_load\s*\(\s*filePath\s*,', 'STBImage.stbi_load(ResourceLoader.getNativeResourcePath(filePath),'
            
            $fileModified = $true
        }
    }

    # 6. AUDIOLOADER - Wrap paths with ResourceLoader for Java Sound API
    if ($file.Name -eq 'AudioLoader.java') {
        if ($content -match 'new File\(filePath\)' -and $content -match 'AudioSystem') {
            Write-Host "  -> Transforming AudioLoader.java (audio loading)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\._resources;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Replace new File(filePath) with new File(ResourceLoader.getNativeResourcePath(filePath))
            # Look for the pattern in AudioSystem.getAudioInputStream context
            $content = $content -replace 'File audioFile = new File\(filePath\);', 'File audioFile = new File(ResourceLoader.getNativeResourcePath(filePath));'
            
            $fileModified = $true
        }
    }

    # 7. MESHLOADER - Fix Lua file loading and model paths
    if ($file.Name -eq 'MeshLoader.java') {
        Write-Host "  -> Transforming MeshLoader.java (Lua and model loading)"
        
        # Add imports if not present
        if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
            $content = $content -replace '(package main\.com\.app\.root\.mesh;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;`nimport java.io.InputStream;"
        }
        
        # Fix DATA_TYPES_DIR path
        $content = $content -replace 'private static final String DATA_TYPES_DIR = "root/src/main/com/app/root/mesh/types/";', 'private static final String DATA_TYPES_DIR = "main/com/app/root/mesh/types/";'
        
        # Fix loadFromFile to load Lua from JAR resources
        $content = $content -replace 'LuaValue chunk = globals\.loadfile\(DATA_TYPES_DIR \+ file\);', @'
String luaPath = DATA_TYPES_DIR + file;
            InputStream luaStream = ResourceLoader.class.getClassLoader().getResourceAsStream(luaPath);
            if (luaStream == null) throw new RuntimeException("Lua file not found: " + luaPath);
            LuaValue chunk = globals.load(luaStream, file, "t", globals);
'@
        
        $meshLoaderCount++
        $fileModified = $true
    }

    # 8. MODELMAP - Complete rewrite for JAR resource loading
    if ($file.Name -eq 'ModelMap.java') {
        Write-Host "  -> Transforming ModelMap.java (model list loading from JAR)"
        
        # Add imports if not present
        if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
            $content = $content -replace '(package main\.com\.app\.root\.mesh;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;`nimport java.io.InputStream;`nimport java.io.InputStreamReader;`nimport java.io.BufferedReader;"
        }
        
        # Fix PATH constant
        $content = $content -replace 'private static final String PATH = "root/src/main/com/app/root/mesh/types/";', 'private static final String PATH = "main/com/app/root/mesh/types/";'
        
        # Replace the entire loadData method with JAR-compatible version
        $oldLoadData = '(?s)private void loadData\(\) \{.*?catch\(Exception e\) \{.*?\}\s*\}'
        $newLoadData = @'
private void loadData() {
        try {
            // Load all .lua files from mesh/types/ directory
            loadLuaFilesFromDirectory(PATH);
            
        } catch(Exception e) {
            System.err.println("Failed to load object map!: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadLuaFilesFromDirectory(String directory) {
        try {
            // In a JAR, we need to enumerate resources differently
            // Try common model definition files
            String[] possibleFiles = {"player.lua", "env.lua", "items.lua", "enemies.lua", "objects.lua", "clouds.lua", "weapons.lua", "list.lua"};
            
            for (String fileName : possibleFiles) {
                String luaFile = directory + fileName;
                loadSingleLuaFile(luaFile);
            }
            
        } catch(Exception e) {
            System.err.println("Error loading from directory " + directory + ": " + e.getMessage());
        }
    }
    
    private void loadSingleLuaFile(String luaFile) {
        try {
            InputStream luaStream = ResourceLoader.class.getClassLoader().getResourceAsStream(luaFile);
            
            if (luaStream == null) {
                // File doesn't exist, skip silently
                return;
            }
            
            System.out.println("Found Lua file: " + luaFile);
            
            // Read first line to check if it starts with "return"
            BufferedReader reader = new BufferedReader(new InputStreamReader(luaStream));
            String firstLine = reader.readLine();
            reader.close();
            
            if (firstLine == null || !firstLine.trim().startsWith("return")) {
                System.out.println("Skipping " + luaFile + " - does not start with 'return'");
                return;
            }
            
            // Reload the stream (we consumed it above)
            luaStream = ResourceLoader.class.getClassLoader().getResourceAsStream(luaFile);
            if (luaStream == null) {
                System.err.println("Failed to reload: " + luaFile);
                return;
            }
            
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.load(luaStream, luaFile, "t", globals);
            LuaValue result = chunk.call();
            
            if(result.istable()) {
                LuaValue dataTable = result.get("data");
                if(dataTable.istable()) {
                    for(int i = 1; i <= dataTable.length(); i++) {
                        LuaValue val = dataTable.get(i);
                        if(val.istable()) {
                            String name = val.get("name").checkjstring();
                            String path = val.get("path").checkjstring();
                            
                            String texture = "";
                            LuaValue textureVal = val.get("texture");
                            if(!textureVal.isnil() && textureVal.isstring()) {
                                texture = textureVal.checkjstring();
                            }
                            
                            LuaValue sizeTable = val.get("scale");
                            if (sizeTable.isnil()) {
                                sizeTable = val.get("size");
                            }
                            
                            float[] size = new float[]{ 1.0f, 1.0f, 1.0f };
                            if(sizeTable.istable()) {
                                for(int j = 1; j <= 3 && j <= sizeTable.length(); j++) {
                                    size[j-1] = (float) sizeTable.get(j).checkdouble();
                                }
                            }

                            ModelFormat format = detectFormat(path);

                            dataMap.put(
                                name.toLowerCase(), 
                                new ModelInfo(
                                    name, 
                                    path, 
                                    texture, 
                                    size,
                                    format
                                )
                            );
                            
                            System.out.println("Loaded object: " + name + " (" + format + ") from " + path);
                        }
                    }
                }
            }
            
            luaStream.close();
        } catch(Exception e) {
            System.err.println("Error loading lua file " + luaFile + ": " + e.getMessage());
        }
    }
'@
        $content = $content -replace $oldLoadData, $newLoadData
        
        $modelMapCount++
        $fileModified = $true
    }

    # 9. OBJLOADER - Use ResourceLoader for file loading
    if ($file.Name -eq 'ObjLoader.java') {
        Write-Host "  -> Transforming ObjLoader.java (OBJ file loading)"
        
        # Add imports if not present
        if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
            $content = $content -replace '(package main\.com\.app\.root\.mesh;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;`nimport java.io.InputStream;`nimport java.io.InputStreamReader;"
        }
        
        # Replace FileReader with ResourceLoader
        $content = $content -replace 'new BufferedReader\(new FileReader\(filePath\)\)', 'new BufferedReader(new InputStreamReader(ResourceLoader.class.getClassLoader().getResourceAsStream(filePath)))'
        
        $objLoaderCount++
        $fileModified = $true
    }

    # 10. GLTFLOADER - Use ResourceLoader for GLTF/GLB file loading
    if ($file.Name -eq 'GltfLoader.java') {
        Write-Host "  -> Transforming GltfLoader.java (GLTF/GLB file loading)"
        
        # Add imports if not present
        if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
            $content = $content -replace '(package main\.com\.app\.root\.mesh;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
        }
        
        # Replace Files.readAllBytes(Paths.get(filePath)) with resource loading
        $content = $content -replace 'Files\.readAllBytes\(Paths\.get\(filePath\)\)', 'ResourceLoader.readAllBytesFromResource(filePath)'
        
        # Replace buffer file loading - this is more complex
        $content = $content -replace 'Files\.readAllBytes\(Paths\.get\(bufferPath\)\)', 'ResourceLoader.readAllBytesFromResource(bufferPath)'
        
        # Fix the buffer path resolution
        $content = $content -replace 'String bufferPath = Paths\.get\(baseDir, uri\)\.toString\(\);', 'String bufferPath = baseDir + "/" + uri;'
        
        # Fix the base directory calculation
        $content = $content -replace 'String baseDir = Paths\.get\(filePath\)\.getParent\(\)\.toString\(\);', @'
String baseDir = filePath.substring(0, filePath.lastIndexOf("/"));
            if (baseDir.isEmpty()) baseDir = filePath.substring(0, filePath.lastIndexOf("\\"));
'@
        
        $gltfLoaderCount++
        $fileModified = $true
    }

    # 11. ANIMATIONLOADER - Use ResourceLoader for animation file loading
    if ($file.Name -eq 'AnimationLoader.java') {
    Write-Host "  -> Transforming AnimationLoader.java (animation file loading with Assimp)"
    
    # Add imports if not present
    if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
        $content = $content -replace '(package main\.com\.app\.root\.mesh;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
    }
    
    # Replace aiImportFile(filePath, FLAGS) with extracted temp file wrapped in try-catch
    $content = $content -replace 'AIScene scene = aiImportFile\(filePath, FLAGS\);', @'
AIScene scene = null;
        try {
            String extractedPath = ResourceLoader.getNativeResourcePath(filePath);
            scene = aiImportFile(extractedPath, FLAGS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract model file: " + filePath, e);
        }
'@
    
    $animationLoaderCount++
    $fileModified = $true
}

    # Save if modified
    if ($fileModified) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
    }
}

Write-Host ""
Write-Host "  Transformation complete"
Write-Host "    - Path replacements: $pathCount files"
Write-Host "    - Font loading fixes: $fontCount files"
Write-Host "    - XML parsing fixes: $xmlCount files"
Write-Host "    - DLL loading fixes: $dllCount files"
Write-Host "    - MeshLoader fixes: $meshLoaderCount files"
Write-Host "    - ModelMap fixes: $modelMapCount files"
Write-Host "    - ObjLoader fixes: $objLoaderCount files"
Write-Host "    - GltfLoader fixes: $gltfLoaderCount files"
Write-Host "    - AnimationLoader fixes: $animationLoaderCount files"