param(
    [string]$ModifiedSrcDir
)

Write-Host "======================================"
Write-Host "DIAGNOSTIC SCRIPT"
Write-Host "======================================"
Write-Host ""
Write-Host "Modified source directory: $ModifiedSrcDir"
Write-Host ""

# Find FontLoader.java
Write-Host "Searching for FontLoader.java..."
$fontLoader = Get-ChildItem -Path $ModifiedSrcDir -Filter "FontLoader.java" -Recurse
if ($fontLoader) {
    Write-Host "FOUND: $($fontLoader.FullName)"
    $content = Get-Content -Path $fontLoader.FullName -Raw
    Write-Host ""
    Write-Host "Checking for patterns:"
    if ($content -match 'root/src/') { Write-Host "  - Contains 'root/src/': YES" } else { Write-Host "  - Contains 'root/src/': NO" }
    if ($content -match 'FileChannel') { Write-Host "  - Contains 'FileChannel': YES" } else { Write-Host "  - Contains 'FileChannel': NO" }
    if ($content -match 'ResourceLoader') { Write-Host "  - Contains 'ResourceLoader': YES" } else { Write-Host "  - Contains 'ResourceLoader': NO" }
    
    Write-Host ""
    Write-Host "First 50 lines of FontLoader.java:"
    Write-Host "-----------------------------------"
    $lines = $content -split "`n"
    for ($i = 0; $i -lt [Math]::Min(50, $lines.Length); $i++) {
        Write-Host "$($i+1): $($lines[$i])"
    }
} else {
    Write-Host "NOT FOUND!"
}

Write-Host ""
Write-Host "======================================"
Write-Host ""

# Find NoiseGeneratorWrapper.java
Write-Host "Searching for NoiseGeneratorWrapper.java..."
$noiseWrapper = Get-ChildItem -Path $ModifiedSrcDir -Filter "NoiseGeneratorWrapper.java" -Recurse
if ($noiseWrapper) {
    Write-Host "FOUND: $($noiseWrapper.FullName)"
    $content = Get-Content -Path $noiseWrapper.FullName -Raw
    Write-Host ""
    Write-Host "Checking for patterns:"
    if ($content -match 'root/src/') { Write-Host "  - Contains 'root/src/': YES" } else { Write-Host "  - Contains 'root/src/': NO" }
    if ($content -match 'DLL_DIR') { Write-Host "  - Contains 'DLL_DIR': YES" } else { Write-Host "  - Contains 'DLL_DIR': NO" }
    if ($content -match '\.build') { Write-Host "  - Contains '.build': YES" } else { Write-Host "  - Contains '.build': NO" }
    if ($content -match 'ResourceLoader') { Write-Host "  - Contains 'ResourceLoader': YES" } else { Write-Host "  - Contains 'ResourceLoader': NO" }
    
    Write-Host ""
    Write-Host "First 60 lines of NoiseGeneratorWrapper.java:"
    Write-Host "----------------------------------------------"
    $lines = $content -split "`n"
    for ($i = 0; $i -lt [Math]::Min(60, $lines.Length); $i++) {
        Write-Host "$($i+1): $($lines[$i])"
    }
} else {
    Write-Host "NOT FOUND!"
}

Write-Host ""
Write-Host "======================================"
Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
