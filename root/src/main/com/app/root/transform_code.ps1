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