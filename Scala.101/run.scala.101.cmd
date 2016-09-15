@echo off
@setlocal
::
set JAVA_HOME=D:\Java\jdk1.7.0_45\jre
set SCALA_HOME=D:\Scala
set PI4J_HOME=C:\Users\olediour.ORADEV\.m2\repository\com\pi4j
::
:: set CP=%JAVA_HOME%\lib\charsets.jar
:: set CP=%CP%;%JAVA_HOME%\lib\deploy.jar
:: set CP=%CP%;%JAVA_HOME%\lib\javaws.jar
:: set CP=%CP%;%JAVA_HOME%\lib\jce.jar
:: set CP=%CP%;%JAVA_HOME%\lib\jfr.jar
:: set CP=%CP%;%JAVA_HOME%\lib\jfxrt.jar
:: set CP=%CP%;%JAVA_HOME%\lib\jsse.jar
:: set CP=%CP%;%JAVA_HOME%\lib\management-agent.jar
:: set CP=%CP%;%JAVA_HOME%\lib\plugin.jar
:: set CP=%CP%;%JAVA_HOME%\lib\resources.jar
:: set CP=%CP%;%JAVA_HOME%\lib\rt.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\access-bridge-64.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\dnsns.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\jaccess.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\localedata.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\sunec.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\sunjce_provider.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\sunmscapi.jar
:: set CP=%CP%;%JAVA_HOME%\lib\ext\zipfs.jar
::
set CP=%CP%;%SCALA_HOME%\lib\scala-actors-2.11.0.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-actors-migration_2.11-1.1.0.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-library.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-parser-combinators_2.11-1.0.3.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-reflect.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-swing_2.11-1.0.1.jar
set CP=%CP%;%SCALA_HOME%\lib\scala-xml_2.11-1.0.3.jar
::
set CP=%CP%;%PI4J_HOME%\pi4j-core\0.0.5\pi4j-core-0.0.5.jar
set CP=%CP%;..\I2C\classes
set CP=%CP%;.\out\production\Scala.101
::
java -verbose -classpath "%CP%" Scala_101
::
@endlocal