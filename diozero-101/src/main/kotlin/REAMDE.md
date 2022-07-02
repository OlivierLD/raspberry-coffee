
```
../gradlew shadowJar
cd src/main/kotlin/
kotlinc diozerokt/FirstTest.kt -cp ../../../build/libs/diozero-101-1.0-all.jar -include-runtime -d test01.jar
```