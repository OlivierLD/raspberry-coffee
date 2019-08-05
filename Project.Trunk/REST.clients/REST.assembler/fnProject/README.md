## fnProject

The doc to get started is [here](https://fnproject.io/tutorials/JavaFDKIntroduction/)

We need three features:
- read the photo cell to get tha ambient light
- set the relay status
- get the relay status

```
 $ fn init --runtime java --trigger http ambientlight
```

Then you can 
```
 $ cd ambientlight
 $ mvn [-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80] clean package
``` 
> Note: It seems that the step above requires Java 9.

```
 $ fn start
```

Then create an app
```
 $ fn create app java-light
Successfully created app:  java-light 
 $ fn list apps
  ...
``` 

In the `ambientlight` folder:
```
 $ fn --verbose deploy --app java-light --local
Deploying ambientlight to app: java-light
Bumped to version 0.0.2
Building image fndemouser/ambientlight:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   47.1kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-1.0.98 as build-stage
...
```
