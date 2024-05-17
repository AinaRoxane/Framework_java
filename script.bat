@echo off
:: variables
set "test_filepath=B:\S4\Web-dyn\GitHub\test"
set "filepath=B:\S4\Web-dyn\GitHub"
set "project_name=sprint1-2483"

:: compile all classes
javac -cp "%filepath%\%project_name%\bin;C:\Program Files\Apache Software Foundation\Tomcat 11.0\lib\servlet-api.jar" -d %filepath%\%project_name%\bin  %filepath%\%project_name%\src\annotation\*.java  %filepath%\%project_name%\src\utils\*.java %filepath%\%project_name%\src\mg\itu\prom16\*.java

:: turn the compiled classes to jar
cd /d ".\bin"
jar -cvf "%test_filepath%\lib\framework.jar" *