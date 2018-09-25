@setlocal
@echo off
@echo off
set JAVA_HOME=D:\Java\jdk1.7.0_45
set PATH=%JAVA_HOME%\bin;%PATH%
echo Compiling
javac -source 1.7 -target 1.7 -sourcepath ./src -d ./classes -classpath ./classes -g ./src/rangesensor/JNI_HC_SR04.java 
echo Running javah
javah -jni -cp ./classes -d C rangesensor.JNI_HC_SR04
echo Here you should the C part
cd C
:: g++ -Wall -shared -I$JAVA_HOME/include -I$JAVA_HOME/include/linux HelloWorld.c -lwiringPi -o libHelloWorld.so
::cd ..
::echo \>\> Now running the class invoking the native lib:
::export LD_LIBRARY_PATH=./C
::# ls -l $LD_LIBRARY_PATH/*.so
::java -Djava.library.path=$LD_LIBRARY_PATH -cp ./classes jnisample.HelloWorld
echo Done.
@endlocal