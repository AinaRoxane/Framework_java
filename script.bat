@echo off
:: variables
set "test_filepath=B:\S4\Web-dyn\Framework_sprint_test"

:: compile all classes
for /r src %%i in (*.java) do (
    javac -cp "bin\;C:\Program Files\Apache Software Foundation\Tomcat 11.0\lib\servlet-api.jar" -d bin\ %%i
)

:: turn the compiled classes to jar
cd /d ".\bin"
jar -cvf "%test_filepath%\lib\framework.jar" *