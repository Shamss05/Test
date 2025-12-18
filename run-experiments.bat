@echo off
REM Run Command-Line Experiments Only

echo ================================================
echo Running Batch Experiments
echo ================================================
echo.

call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo Build failed
    pause
    exit /b 1
)

echo.
echo Running experiments...
echo.

call mvn exec:java -Dexec.mainClass="edu.montecarlo.experiment.PiExperimentRunner"

echo.
pause
