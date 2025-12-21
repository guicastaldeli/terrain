@echo off
setlocal EnableDelayedExpansion

echo Building Terrain Generator with Visual Studio Compiler
echo =====================================================

set JAVA_HOME=C:\Program Files\Java\jdk-22
set VCPKG_ROOT=C:\Users\casta\OneDrive\Desktop\vscode\messages\main\vcpkg
set OPENSSL_INCLUDE=%VCPKG_ROOT%\installed\x64-windows\include
set OPENSSL_LIB=%VCPKG_ROOT%\installed\x64-windows\lib

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
del libcrypto-3-x64.dll 2>nul
del libssl-3-x64.dll 2>nul

echo.
echo Compiling with CL.EXE...

rem Compile all C files with OpenSSL includes
cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\main.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile main.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%MAP_DIR%\map_generator_jni.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile map_generator_jni.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\noise.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile noise.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\map_generator.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile map_generator.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\poisson_disk.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile poisson_disk.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\point_generator.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile point_generator.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\domain_warp.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile domain_warp.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\erosion.c"

if %errorlevel% neq 0 (
    echo ERROR: Failed to compile erosion.c
    pause
    exit /b 1
)

cl /nologo /c /O2 /EHsc /std:c17 ^
    /I"%JAVA_HOME%\include" ^
    /I"%JAVA_HOME%\include\win32" ^
    /I"%OPENSSL_INCLUDE%" ^
    "%SRC_DIR%\file_saver.c"

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
    /LIBPATH:"%OPENSSL_LIB%" ^
    libssl.lib ^
    libcrypto.lib ^
    ws2_32.lib ^
    advapi32.lib

if %errorlevel% neq 0 (
    echo ERROR: Linking failed
    pause
    exit /b 1
)

echo.
echo Copying required runtime DLLs...
if exist "%OPENSSL_LIB%\..\bin\libcrypto-3-x64.dll" (
    copy "%OPENSSL_LIB%\..\bin\libcrypto-3-x64.dll" . >nul
    echo Copied libcrypto-3-x64.dll
) else (
    echo WARNING: libcrypto-3-x64.dll not found
    echo Searching for alternative crypto DLL...
    dir "%OPENSSL_LIB%\..\bin\*crypto*.dll" /B
)

if exist "%OPENSSL_LIB%\..\bin\libssl-3-x64.dll" (
    copy "%OPENSSL_LIB%\..\bin\libssl-3-x64.dll" . >nul
    echo Copied libssl-3-x64.dll
) else (
    echo WARNING: libssl-3-x64.dll not found
    echo Searching for alternative ssl DLL...
    dir "%OPENSSL_LIB%\..\bin\*ssl*.dll" /B
)

echo.
echo Final verification...
echo DLLs in current directory:
dir *.dll /B

echo.
echo Checking for required files...
set MISSING=0
if not exist map_generator.dll (
    echo ERROR: map_generator.dll not created!
    set MISSING=1
)

if not exist libcrypto-3-x64.dll (
    echo WARNING: libcrypto-3-x64.dll not found
    echo This file is required at runtime!
    echo Checking for alternative names...
    for %%f in (libcrypto*.dll) do (
        echo Found: %%f - renaming to libcrypto-3-x64.dll
        copy "%%f" libcrypto-3-x64.dll >nul
    )
)

if not exist libssl-3-x64.dll (
    echo WARNING: libssl-3-x64.dll not found
    echo This file is required at runtime!
    echo Checking for alternative names...
    for %%f in (libssl*.dll) do (
        echo Found: %%f - renaming to libssl-3-x64.dll
        copy "%%f" libssl-3-x64.dll >nul
    )
)

if %MISSING% equ 0 (
    echo.
    echo BUILD SUCCESSFUL!
    echo.
    echo Files created in: %CD%
    echo 1. map_generator.dll
    echo 2. libcrypto-3-x64.dll
    echo 3. libssl-3-x64.dll
    echo.
    echo Make sure these files are in your Java DLL path:
    echo %MAP_DIR%\.build\
) else (
    echo.
    echo BUILD FAILED :(
)

echo.
pause