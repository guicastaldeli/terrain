REM filepath: c:\Users\casta\OneDrive\Desktop\vscode\terrain\root\src\main\com\app\root\env\map\noise\.build\build.bat
@echo off
setlocal EnableDelayedExpansion

echo Building Terrain Generator with Visual Studio Compiler
echo =====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-22
set SRC_DIR=C:\Users\casta\OneDrive\Desktop\vscode\terrain\root\src\main\com\app\root\env\map\noise
set MAP_DIR=C:\Users\casta\OneDrive\Desktop\vscode\terrain\root\src\main\com\app\root\env\map

echo.
set VS_PATH=C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build

if exist "%VS_PATH%\vcvars64.bat" (
    call "%VS_PATH%\vcvars64.bat"
    echo Visual Studio environment loaded :D
) else (
    echo ERROR: Visual Studio not found. Please install Visual Studio Build Tools.
    pause
    exit /b 1
)

echo.
echo Cleaning previous builds...
del *.obj 2>nul
del map_generator.dll 2>nul

echo.
echo Compiling with CL.EXE...

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\main.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile main.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%MAP_DIR%\map_generator_jni.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile map_generator_jni.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\noise.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile noise.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\map_generator.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile map_generator.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\poisson_disk.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile poisson_disk.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\point_generator.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile point_generator.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\domain_warp.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile domain_warp.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\erosion.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile erosion.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32" "%SRC_DIR%\file_saver.c"
if %errorlevel% neq 0 (
    echo ERROR: Failed to compile file_saver.c
    pause
    exit /b 1
)

echo.
echo Linking DLL with link.exe...
link /nologo /DLL /OUT:map_generator.dll ^
    main.obj ^
    map_generator_jni.obj ^
    noise.obj ^
    map_generator.obj ^
    poisson_disk.obj ^
    point_generator.obj ^
    domain_warp.obj ^
    erosion.obj ^
    file_saver.obj ^
    ws2_32.lib ^
    advapi32.lib

if %errorlevel% neq 0 (
    echo ERROR: Linking failed
    pause
    exit /b 1
)

echo.
echo Copying DLL to Java resources directory...
set RESOURCES_DIR=%SRC_DIR%\..\..\..\..\..\_resources\native
if not exist "%RESOURCES_DIR%" (
    mkdir "%RESOURCES_DIR%"
)
copy map_generator.dll "%RESOURCES_DIR%\" >nul
if %errorlevel% neq 0 (
    echo ERROR: Failed to copy DLL to resources directory
) else (
    echo Copied map_generator.dll to %RESOURCES_DIR%
)

echo.
echo Final verification...
if exist map_generator.dll (
    echo BUILD SUCCESSFUL!
    echo.
    echo Created files:
    dir *.dll /B
    echo.
    echo DLL copied to: %RESOURCES_DIR%\map_generator.dll
) else (
    echo BUILD FAILED :/
)

echo.
pause