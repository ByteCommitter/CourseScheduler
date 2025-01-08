@echo off
echo Checking environment...

REM Set explicit paths with proper escaping
set "M2_HOME=C:\Program Files\Apache\maven\apache-maven-3.9.9"
set "JAVA_HOME=C:\Users\Dell\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.8.7-hotspot"
set "MVN_CMD=%M2_HOME%\bin\mvn.cmd"
set "PATH=%JAVA_HOME%\bin;%M2_HOME%\bin;%PATH%"

echo Maven command: %MVN_CMD%
echo Maven home: %M2_HOME%
echo Java home: %JAVA_HOME%

REM Verify Maven exists
if not exist "%MVN_CMD%" (
    echo Maven executable not found at: %MVN_CMD%
    echo Please check your Maven installation
    pause
    exit /b 1
)

REM Store the original directory
pushd %~dp0

REM Build the parent project first
echo Building parent project...
cd build
call "%MVN_CMD%" -f .. verify -DskipTests
if errorlevel 1 goto error

REM Now start the school timetabling application
echo Starting School Timetabling application...
cd ..\use-cases\school-timetabling

REM Use 'call' to prevent script from exiting after Maven command
call "%MVN_CMD%" clean quarkus:dev ^
    -Dquarkus.http.port=8081 ^
    -Dquarkus.http.cors=true ^
    -Ddebug=false ^
    -Dquarkus.http.host=0.0.0.0 ^
    -Dstartup-open-browser=true ^
    -Dquarkus.live-reload.url=http://localhost:8080

if errorlevel 1 goto error

:success
echo Application started successfully
echo Visit http://localhost:8080 in your browser
goto end

:error
popd
echo An error occurred
pause
exit /b 1

:end
popd
pause
