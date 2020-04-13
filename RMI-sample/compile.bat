@echo off
@endlocal
set JAVA_HOME=C:\Java\jdk1.8.0_11
set PATH=%PATH%;%JAVA_HOME%\bin
:: 1. Interfaces
echo Interface Jars
mkdir build > NUL
mkdir build\classes > NUL
mkdir build\libs > NUL
javac -d build\classes -sourcepath src\java src\java\compute\Task.java 
cd build\classes
jar -cvf ..\libs\compute.jar compute\*.class
cd ..\..
:: 2. Server
echo Server classes
javac -d build\classes -sourcepath src\java -cp build\libs\compute.jar src\java\engine\ComputeEngine.java
:: 3. Client
echo Client classes
javac -d build\classes -sourcepath src\java -cp build\libs\compute.jar src\java\client\*.java
@endlocal
