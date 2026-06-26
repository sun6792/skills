@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==============================================
echo   Get Jobs - Auto Job Application Assistant
echo ==============================================
echo.
echo Chrome will open with a persistent profile.
echo Login to BOSS once - it will be remembered!
echo ==============================================

REM ======== 0. 停止旧的 Gradle Daemon ========
echo.
echo [0/4] Stopping old Gradle Daemon...
call gradlew.bat --stop >nul 2>&1

REM ======== 1. 先清理旧进程 ========
echo [1/4] Cleaning old port processes...

for %%P in (8889 6866) do (
    for /f "tokens=5" %%I in ('netstat -ano ^| findstr /R /C:"LISTENING" ^| findstr /C:":%%P" 2^>nul') do (
        echo   关闭端口 %%P (PID %%I)...
        taskkill /F /PID %%I >nul 2>&1
    )
)

REM ======== 2. 设置 JDK 17 ========
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

REM 验证 JDK 版本
for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    echo   Java 版本: %%V
)

cd /d "%~dp0"

echo.
echo [2/4] 启动后端服务 (端口 8889)...
echo [3/4] 前端页面将由后端在端口 6866 提供静态服务...
echo.
echo   等待约15-30秒后端启动完成...
echo   浏览器会自动打开 http://localhost:6866
echo   请勿关闭此窗口！
echo ==============================================
echo.

REM ======== 3. 延迟打开浏览器，然后启动后端 ========
start "" cmd /c "timeout /t 15 /nobreak >nul && start http://localhost:6866"

call gradlew.bat bootRun

pause
