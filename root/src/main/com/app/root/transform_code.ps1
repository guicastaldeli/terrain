param(
    [string]$ModifiedSrcDir
)

$ErrorActionPreference = 'Stop'

Write-Host "Starting code transformations..."
Write-Host "Target directory: $ModifiedSrcDir"
Write-Host ""

$files = Get-ChildItem -Path $ModifiedSrcDir -Filter *.java -Recurse
Write-Host "Processing $($files.Count) Java files..."

$pathCount = 0
$fontCount = 0
$xmlCount = 0
$dllCount = 0

foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    $fileModified = $false

    # 1. PATH REPLACEMENTS
    if ($content -match 'root/src/') {
        Write-Host "  -> Found 'root/src/' in: $($file.Name)"
        # Saves directory - external folder
        $content = $content -replace 'root/src/main/com/app/root/_resources/saves/', 'saves/'
        
        # DLL directory - external natives folder
        $content = $content -replace 'root/src/main/com/app/root/env/_noise/\\.build', 'natives'
        $content = $content -replace 'main/com/app/root/env/_noise/\\.build', 'natives'
        
        # All other resources - remove root/src/
        $content = $content -replace 'root/src/', ''
        
        $pathCount++
        $fileModified = $true
    }

    # 2. FONTLOADER - Replace FileChannel with ResourceLoader
    if ($file.Name -eq 'FontLoader.java') {
        if ($content -match 'FileChannel') {
            Write-Host "  -> Transforming FontLoader.java (font loading)"
            Write-Host "     Original file length: $($content.Length) chars"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\._text_renderer;)', "`$1`r`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Save original for debugging
            $originalFontLoader = $content
            
            # Replace the FileChannel loading block
            # Pattern: try (FileChannel fc = FileChannel.open(..., StandardOpenOption.READ)) { 
            #            fontData = ByteBuffer.allocate((int) fc.size());
            #            fc.read(fontData);
            #            fontData.flip();
            #          }
            $pattern = '(?s)try\s*\(\s*FileChannel\s+fc\s*=\s*FileChannel\.open\([^)]+\)\s*\)\s*\{[^}]*?fontData\s*=\s*ByteBuffer\.allocate[^;]*;[^}]*?fc\.read[^;]*;[^}]*?fontData\.flip[^;]*;[^}]*?\}'
            $replacement = 'fontData = ResourceLoader.loadFontToBuffer(fontPath);'
            
            $content = $content -replace $pattern, $replacement
            
            Write-Host "     After replacement: $($content.Length) chars"
            Write-Host "     Pattern matched: $(if ($originalFontLoader -ne $content) { 'YES' } else { 'NO' })"
            
            # If nothing changed, the pattern didn't match - save for inspection
            if ($originalFontLoader -eq $content) {
                Write-Host "     WARNING: Pattern did not match! Saving original for inspection..."
                Set-Content -Path "$($file.Directory.FullName)\FontLoader.java.ORIGINAL" -Value $originalFontLoader -NoNewline
            }
            
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
        if ($content -match 'DLL_DIR|\.build|noise_generator\.dll') {
            Write-Host "  -> Transforming NoiseGeneratorWrapper.java (DLL loading)"
            
            # Add import if not present
            if ($content -notmatch 'import main\.com\.app\.root\.utils\.ResourceLoader') {
                $content = $content -replace '(package main\.com\.app\.root\.env;)', "`$1`nimport main.com.app.root.utils.ResourceLoader;"
            }
            
            # Replace any string literal containing the old path
            $content = $content -replace '"main/com/app/root/env/_noise/\.build"', '"natives"'
            $content = $content -replace '"[^"]*env/_noise/\.build"', '"natives"'
            
            # Wrap string literals for natives path with ResourceLoader
            $content = $content -replace 'String\s+DLL_DIR\s*=\s*"natives";', 'String DLL_DIR = ResourceLoader.getExternalPath("natives");'
            $content = $content -replace 'new\s+File\s*\(\s*"natives"\s*\)', 'new File(ResourceLoader.getExternalPath("natives"))'
            
            # Wrap existing File constructions
            $content = $content -replace 'File\s+dllDir\s*=\s*new\s+File\s*\(\s*DLL_DIR\s*\)', 'File dllDir = new File(ResourceLoader.getExternalPath(DLL_DIR))'
            
            # Fix System.load calls to use ResourceLoader
            $content = $content -replace 'new\s+File\s*\(\s*DLL_DIR\s*,\s*"([^"]+)"\s*\)\.getAbsolutePath\(\)', 'new File(ResourceLoader.getExternalPath(DLL_DIR), "$1").getAbsolutePath()'
            
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