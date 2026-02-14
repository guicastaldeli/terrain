@echo off
setlocal EnableDelayedExpansion

echo =====================================================
echo Building Terrain Game (Complete Ultimate Build)
echo =====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-22
set PROJECT_ROOT=C:\Users\casta\OneDrive\Desktop\vscode\terrain\root
set SRC_DIR=%PROJECT_ROOT%\src
set LIB_DIR=%PROJECT_ROOT%\lib
set BUILD_DIR=%PROJECT_ROOT%\build
set DIST_DIR=%PROJECT_ROOT%\dist
set NATIVES_DIR=%PROJECT_ROOT%\src\main\com\app\root\env\_noise\.build

set MODIFIED_SRC_DIR=%BUILD_DIR%\modified_src

echo Project Root: %PROJECT_ROOT%
echo Source Dir: %SRC_DIR%
echo Lib Dir: %LIB_DIR%

echo.
echo Cleaning previous builds...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"

mkdir "%BUILD_DIR%"
mkdir "%BUILD_DIR%\classes"
mkdir "%BUILD_DIR%\temp"
mkdir "%MODIFIED_SRC_DIR%"
mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\natives"
mkdir "%DIST_DIR%\saves"

echo.
echo Step 1: Collecting JAR dependencies...
set CLASSPATH=.
for /r "%LIB_DIR%" %%f in (*.jar) do (
    echo   Found: %%~nxf
    set CLASSPATH=!CLASSPATH!;%%f
)

echo.
echo Step 2: Copying Java source files and resources...
echo   Copying from %SRC_DIR% to %MODIFIED_SRC_DIR%...

REM Copy everything from src to modified_src
echo   Copying all files (Java, Lua, GLSL, fonts, XML, images, audio)...
robocopy "%SRC_DIR%" "%MODIFIED_SRC_DIR%" /E /NFL /NDL /NJH /NJS /NP >nul
if %errorlevel% gtr 7 (
    echo   Warning: Some files may not have been copied
)

set FILE_COUNT=0
for /r "%MODIFIED_SRC_DIR%" %%f in (*.java) do set /a FILE_COUNT+=1
set LUA_COUNT=0
for /r "%MODIFIED_SRC_DIR%" %%f in (*.lua) do set /a LUA_COUNT+=1
echo   Found %FILE_COUNT% Java files and %LUA_COUNT% Lua files

if %LUA_COUNT%==0 (
    echo   WARNING: No Lua files found! Font paths may not be transformed.
)

echo.
echo Step 3: Applying comprehensive code transformations...
echo   This will modify code to work in BOTH dev and JAR environments...
echo.

REM Copy the PowerShell script to the build directory
REM You need to have transform_code.ps1 in the same directory as this build.bat
if not exist "transform_code.ps1" (
    echo ERROR: transform_code.ps1 not found!
    echo Please ensure transform_code.ps1 is in the same directory as build.bat
    pause
    exit /b 1
)

copy "transform_code.ps1" "%BUILD_DIR%\transform_code.ps1" >nul

powershell -ExecutionPolicy Bypass -File "%BUILD_DIR%\transform_code.ps1" -ModifiedSrcDir "%MODIFIED_SRC_DIR%"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Code transformation failed!
    echo Check the PowerShell script: %BUILD_DIR%\transform_code.ps1
    pause
    exit /b 1
)

echo.
echo   Examples of transformations:
echo   • "root/src/main/.../list.lua" → "main/.../list.lua"
echo   • FileChannel.open() → ResourceLoader.loadFontToBuffer()
echo   • parse(new File()) → parse(ResourceLoader.getXMLStream())
echo   • "natives" path → ResourceLoader.getExternalPath("natives")
echo.

echo.
echo Step 4: Finding modified Java source files...
cd "%MODIFIED_SRC_DIR%"
dir /s /b *.java > "%BUILD_DIR%\sources.txt"

for /f %%i in ('find /c /v "" ^< "%BUILD_DIR%\sources.txt"') do set COMPILE_COUNT=%%i
echo Files to compile: %COMPILE_COUNT%

if "%COMPILE_COUNT%"=="0" (
    echo ERROR: No Java source files found!
    cd "%PROJECT_ROOT%"
    pause
    exit /b 1
)

echo.
echo Step 5: Compiling modified Java sources...
"%JAVA_HOME%\bin\javac" -d "%BUILD_DIR%\classes" ^
    -classpath "!CLASSPATH!" ^
    -encoding UTF-8 ^
    -sourcepath "%MODIFIED_SRC_DIR%" ^
    @"%BUILD_DIR%\sources.txt"

if %errorlevel% neq 0 (
    echo.
    echo ======================================================
    echo ERROR: Compilation failed!
    echo ======================================================
    echo.
    echo Modified sources are in: %MODIFIED_SRC_DIR%
    echo You can inspect them to see what went wrong.
    echo.
    cd "%PROJECT_ROOT%"
    pause
    exit /b 1
)

cd "%PROJECT_ROOT%"
echo   ✓ Compilation successful

echo.
echo   Verifying Main.class...
if exist "%BUILD_DIR%\classes\main\com\app\root\Main.class" (
    echo   ✓ Main.class found at: main\com\app\root\Main.class
) else (
    echo   ✗ ERROR: Main.class not found at expected location!
    dir /s /b "%BUILD_DIR%\classes\*Main.class"
    pause
    exit /b 1
)

echo.
echo Step 6: Copying ALL resource files...
echo   Copying resources (Lua, XML, GLSL, fonts, images, audio) from MODIFIED sources...

REM Copy from modified_src/main instead of src/main to preserve transformations
if exist "%MODIFIED_SRC_DIR%\main" (
    echo   Source: %MODIFIED_SRC_DIR%\main
    echo   Target: %BUILD_DIR%\classes\main
    robocopy "%MODIFIED_SRC_DIR%\main" "%BUILD_DIR%\classes\main" /E /XF *.java *.class /NFL /NDL /NJH /NJS
    if %errorlevel% gtr 7 (
        echo   Warning: Some files may not have been copied
    )
) else (
    echo   ERROR: %MODIFIED_SRC_DIR%\main not found!
    pause
    exit /b 1
)

echo.
echo   WORKAROUND: Copying shaders for relative includes...
echo   Creating unified shader directory structure under _shaders/

REM Copy env directory to _shaders for relative includes like ../env/
if exist "%BUILD_DIR%\classes\main\com\app\root\env" (
    mkdir "%BUILD_DIR%\classes\main\com\app\root\_shaders\env" 2>nul
    xcopy /S /Y "%BUILD_DIR%\classes\main\com\app\root\env\*.glsl" "%BUILD_DIR%\classes\main\com\app\root\_shaders\env\" >nul 2>&1
    if exist "%BUILD_DIR%\classes\main\com\app\root\env\skybox\shaders" (
        mkdir "%BUILD_DIR%\classes\main\com\app\root\_shaders\env\skybox\shaders" 2>nul
        xcopy /S /Y "%BUILD_DIR%\classes\main\com\app\root\env\skybox\shaders\*.*" "%BUILD_DIR%\classes\main\com\app\root\_shaders\env\skybox\shaders\" >nul
        echo   ✓ Copied env/skybox/shaders/ to _shaders/env/skybox/shaders/
    )
)

echo.
echo   ============================================
echo   SHADER STRUCTURE DIAGNOSTICS
echo   ============================================
echo.
echo   Checking if shader files exist in expected locations...
echo.
if exist "%BUILD_DIR%\classes\main\com\app\root\_shaders\main\frag.glsl" (
    echo   ✓ Found: _shaders/main/frag.glsl
) else (
    echo   ✗ MISSING: _shaders/main/frag.glsl
)

if exist "%BUILD_DIR%\classes\main\com\app\root\_shaders\env\skybox\shaders\sb_frag.glsl" (
    echo   ✓ Found: _shaders/env/skybox/shaders/sb_frag.glsl
) else (
    echo   ✗ MISSING: _shaders/env/skybox/shaders/sb_frag.glsl
    echo.
    echo   Searching for sb_frag.glsl anywhere...
    dir /s /b "%BUILD_DIR%\classes\*sb_frag.glsl" 2>nul
)

echo.
echo   Complete _shaders directory tree:
if exist "%BUILD_DIR%\classes\main\com\app\root\_shaders" (
    tree /F "%BUILD_DIR%\classes\main\com\app\root\_shaders" | more
) else (
    echo   ERROR: _shaders directory does not exist!
)
echo   ============================================

echo.
echo   Resource file counts:
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.lua" 2^>nul ^| find /c /v ""') do echo     Lua files: %%i
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.xml" 2^>nul ^| find /c /v ""') do echo     XML files: %%i
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.glsl" 2^>nul ^| find /c /v ""') do echo     GLSL files: %%i
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.ttf" 2^>nul ^| find /c /v ""') do echo     TTF fonts: %%i
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.wav" 2^>nul ^| find /c /v ""') do echo     WAV files: %%i
for /f %%i in ('dir /s /b "%BUILD_DIR%\classes\main\*.png" 2^>nul ^| find /c /v ""') do echo     PNG files: %%i

echo.
echo Step 7: Extracting library JARs...
cd "%BUILD_DIR%\temp"
for /r "%LIB_DIR%" %%f in (*.jar) do (
    echo   Extracting: %%~nxf
    "%JAVA_HOME%\bin\jar" xf "%%f"
)

echo   Merging extracted classes into build...
xcopy /E /Y /Q "%BUILD_DIR%\temp\*" "%BUILD_DIR%\classes\" >nul 2>&1

REM Clean up META-INF signatures (prevent JAR signing conflicts)
if exist "%BUILD_DIR%\classes\META-INF\*.SF" del /q "%BUILD_DIR%\classes\META-INF\*.SF" 2>nul
if exist "%BUILD_DIR%\classes\META-INF\*.RSA" del /q "%BUILD_DIR%\classes\META-INF\*.RSA" 2>nul
if exist "%BUILD_DIR%\classes\META-INF\*.DSA" del /q "%BUILD_DIR%\classes\META-INF\*.DSA" 2>nul

echo.
echo Step 8: Copying native libraries to dist/natives...
echo   Copying custom DLLs...
if exist "%NATIVES_DIR%\noise_generator.dll" (
    copy "%NATIVES_DIR%\noise_generator.dll" "%DIST_DIR%\natives\" >nul
    echo   ✓ noise_generator.dll
) else (
    echo   ⚠ noise_generator.dll not found
)

if exist "%NATIVES_DIR%\libcrypto-3-x64.dll" (
    copy "%NATIVES_DIR%\libcrypto-3-x64.dll" "%DIST_DIR%\natives\" >nul
    echo   ✓ libcrypto-3-x64.dll
) else (
    echo   ⚠ libcrypto-3-x64.dll not found
)

if exist "%NATIVES_DIR%\libssl-3-x64.dll" (
    copy "%NATIVES_DIR%\libssl-3-x64.dll" "%DIST_DIR%\natives\" >nul
    echo   ✓ libssl-3-x64.dll
) else (
    echo   ⚠ libssl-3-x64.dll not found
)

echo   Copying LWJGL natives...
set LWJGL_FOUND=0
for /r "%LIB_DIR%\LWJGL" %%f in (*.dll) do (
    copy "%%f" "%DIST_DIR%\natives\" >nul 2>&1
    echo   ✓ %%~nxf
    set LWJGL_FOUND=1
)

if !LWJGL_FOUND!==0 (
    echo   ⚠ No LWJGL DLLs found in lib\LWJGL
    echo   Extracting from natives-windows JARs...
    for /r "%LIB_DIR%" %%f in (*natives-windows*.jar) do (
        echo   Extracting natives from: %%~nxf
        mkdir "%BUILD_DIR%\natives_temp" >nul 2>&1
        cd "%BUILD_DIR%\natives_temp"
        "%JAVA_HOME%\bin\jar" xf "%%f"
        copy *.dll "%DIST_DIR%\natives\" >nul 2>&1
        cd "%PROJECT_ROOT%"
        rmdir /s /q "%BUILD_DIR%\natives_temp"
    )
)

echo.
echo Step 9: Creating manifest...
(
    echo Manifest-Version: 1.0
    echo Main-Class: main.com.app.root.Main
    echo Class-Path: .
) > "%BUILD_DIR%\MANIFEST.MF"

echo.
echo Step 10: Building Fat JAR...
cd "%BUILD_DIR%\classes"
"%JAVA_HOME%\bin\jar" cfm "%DIST_DIR%\TerrainGame.jar" "%BUILD_DIR%\MANIFEST.MF" .

if %errorlevel% neq 0 (
    echo ERROR: JAR creation failed!
    cd "%PROJECT_ROOT%"
    pause
    exit /b 1
)

cd "%PROJECT_ROOT%"
echo   ✓ JAR created successfully

echo.
echo Step 11: Verifying JAR contents...
echo Main class:
"%JAVA_HOME%\bin\jar" tf "%DIST_DIR%\TerrainGame.jar" | findstr "Main.class"

echo.
echo ResourceLoader utility:
"%JAVA_HOME%\bin\jar" tf "%DIST_DIR%\TerrainGame.jar" | findstr "ResourceLoader.class"

echo.
echo Sample resources in JAR:
"%JAVA_HOME%\bin\jar" tf "%DIST_DIR%\TerrainGame.jar" | findstr /i "\.ttf$ \.xml$ \.lua$ \.glsl$" | more

echo.
echo Step 12: Creating launcher scripts...

REM Windows Launcher (with console)
(
    echo @echo off
    echo echo =====================================================
    echo echo         TERRAIN GAME
    echo echo =====================================================
    echo echo.
    echo.
    echo REM Create saves directory if needed
    echo if not exist saves (
    echo     echo Creating saves directory...
    echo     mkdir saves
    echo     echo ✓ Saves directory created
    echo ^)
    echo.
    echo REM Check for natives
    echo if not exist natives (
    echo     echo ERROR: natives folder not found!
    echo     echo Please make sure natives folder is in the same directory as TerrainGame.jar
    echo     pause
    echo     exit /b 1
    echo ^)
    echo.
    echo echo Starting game...
    echo java -Djava.library.path=natives -jar TerrainGame.jar
    echo.
    echo if errorlevel 1 (
    echo     echo.
    echo     echo ========================================
    echo     echo ERROR: Game crashed!
    echo     echo ========================================
    echo     echo Check the error messages above.
    echo     pause
    echo ^)
) > "%DIST_DIR%\run.bat"

REM Windows Launcher (no console)
(
    echo @echo off
    echo if not exist saves mkdir saves
    echo start javaw -Djava.library.path=natives -jar TerrainGame.jar
) > "%DIST_DIR%\run-no-console.bat"

REM Linux/Mac Launcher
(
    echo #!/bin/bash
    echo echo "Starting Terrain Game..."
    echo mkdir -p saves
    echo java -Djava.library.path=natives -jar TerrainGame.jar
) > "%DIST_DIR%\run.sh"

echo.
echo Step 13: Creating README...
(
    echo =====================================================
    echo TERRAIN GAME - Distribution Package
    echo =====================================================
    echo.
    echo QUICK START:
    echo   Windows: Double-click run.bat
    echo   Linux/Mac: chmod +x run.sh ^&^& ./run.sh
    echo.
    echo REQUIREMENTS:
    echo   - Java 21 or higher
    echo   - OpenGL 3.3+ compatible graphics card
    echo.
    echo FOLDER STRUCTURE (DO NOT MODIFY):
    echo   dist/
    echo   ├── TerrainGame.jar       Main game file
    echo   ├── natives/              Native DLL libraries (REQUIRED)
    echo   ├── saves/                Save files (auto-created)
    echo   ├── run.bat               Windows launcher
    echo   ├── run-no-console.bat    Silent Windows launcher
    echo   └── run.sh                Linux/Mac launcher
    echo.
    echo IMPORTANT:
    echo   - Keep ALL files in the same folder
    echo   - Do NOT extract or modify TerrainGame.jar
    echo   - Do NOT move or delete the natives folder
    echo   - Saves are automatically created in saves/
    echo.
    echo TROUBLESHOOTING:
    echo.
    echo   Problem: "java: command not found"
    echo   Solution: Install Java 21+ from adoptium.net
    echo            Verify: java -version
    echo.
    echo   Problem: "UnsatisfiedLinkError"
    echo   Solution: Ensure natives/ folder exists with all .dll files
    echo            Check that you're running from the correct directory
    echo.
    echo   Problem: "Resource not found" or "File not found"
    echo   Solution: Do NOT extract the JAR file
    echo            Run from this folder with run.bat
    echo.
    echo   Problem: Can't save games
    echo   Solution: Check saves/ folder permissions
    echo            Ensure you have write access to this directory
    echo.
    echo DISTRIBUTION:
    echo   To share this game:
    echo   1. ZIP the entire dist/ folder
    echo   2. Upload to GameJolt, itch.io, etc.
    echo   3. Tell users to extract and run run.bat
    echo.
) > "%DIST_DIR%\README.txt"

echo.
echo =====================================================
echo BUILD COMPLETE!
echo =====================================================
echo.
echo Distribution package: %DIST_DIR%
echo.
echo Contents:
dir /b "%DIST_DIR%" | findstr /v ".tmp"
echo.
for %%f in ("%DIST_DIR%\TerrainGame.jar") do (
    set /a MB=%%~zf/1048576
    echo JAR Size: !MB! MB (%%~zf bytes)
)
echo.
for /f %%i in ('dir /b "%DIST_DIR%\natives\*.dll" 2^>nul ^| find /c /v ""') do (
    echo Native DLLs: %%i files in natives/
)
echo.
echo =====================================================
echo TRANSFORMATION SUMMARY
echo =====================================================
echo.
echo The build script automatically modified your code to work
echo in BOTH development and JAR distribution:
echo.
echo 1. PATH REPLACEMENTS:
echo    • "root/src/main/..." → "main/..." (resources)
echo    • ".../saves/" → "saves/" (external folder)
echo    • ".../natives" → "natives" (external DLLs)
echo.
echo 2. FONT LOADING (FontLoader.java):
echo    • FileChannel.open() → ResourceLoader.loadFontToBuffer()
echo    • Now loads .ttf files from JAR resources
echo.
echo 3. XML PARSING (DocParser.java):
echo    • parse(new File()) → parse(ResourceLoader.getXMLStream())
echo    • Now loads .xml files from JAR resources
echo.
echo 4. DLL LOADING (NoiseGeneratorWrapper.java):
echo    • Direct paths → ResourceLoader.getExternalPath()
echo    • Now finds DLLs in external natives/ folder
echo.
echo Your ORIGINAL source code is UNCHANGED!
echo Modified copies were used only for compilation.
echo.
echo Modified sources location (for inspection):
echo %MODIFIED_SRC_DIR%
echo.
echo =====================================================
echo TESTING
echo =====================================================
echo To test the build:
echo   cd %DIST_DIR%
echo   run.bat
echo.
echo =====================================================
echo DISTRIBUTION
echo =====================================================
echo To distribute:
echo   1. ZIP the entire dist/ folder
echo   2. Upload to your platform (GameJolt, itch.io)
echo   3. Users extract and run
echo.
echo Ready for distribution!
echo =====================================================
echo.
pause