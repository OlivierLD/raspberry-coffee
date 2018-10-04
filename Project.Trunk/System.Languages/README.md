## System resolution
In several languages (for [comparison](./LanguageComparison.md)), along with several samples and linear algebra utilities.

### Java Math REST Server
To be used from a Web UI, there is a Math server for linear algebra operations.

1. Compile what you need
```
 $ ../../gradlew shadowJar
```
2. Start the server
```
 $ ./runMathServer.sh
```
3. From a browser, reach http://localhost:1234/web/smoothing.rest.html

---
