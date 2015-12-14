@setlocal
@echo off
set JAVA_HOME=D:\Java\jdk1.7.0_45
set PATH=%JAVA_HOME%\bin;%PATH%
javac -source 1.7 -target 1.7 -sourcepath .\src -d .\classes -classpath .\classes -g .\src\jnisample\HelloWorld.java 
javah -jni -cp .\classes -d C jnisample.HelloWorld
@endlocal
