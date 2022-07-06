# A Maven repo on GitHub for RaspberryCoffee and related projects
See good article at <https://gist.github.com/fernandezpablo85/03cf8b0cd2e7d8527063>

> Another possibility: <https://repsy.io/>

### Sample command:

From `raspberry-coffee/common-utils` (`master` branch, or whatever branch you work on), install (generating the jar is good enough, actually):
```
$ ../gradlew install
```
From the root of the `repository` branch:
```
$ mvn install:install-file \
      -DgroupId=oliv.raspi.coffee \
      -DartifactId=common-utils \
      -Dversion=1.0 \
      -Dfile=${HOME}/.m2/repository/oliv/raspi/coffee/common-utils/1.0/common-utils-1.0.jar \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -DlocalRepositoryPath=.  \
      -DcreateChecksum=true
```
or also
```
GROUP=oliv.raspi.coffee
ARTIFACT=http-tiny-server
VERSION=1.0
$ mvn install:install-file \
      -DgroupId=${GROUP} \
      -DartifactId=${ARTIFACT} \
      -Dversion=${VERSION} \
      -Dfile=${HOME}/.m2/repository/oliv/raspi/coffee/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar \
      -Dpackaging=jar \
      -DgeneratePom=true \
      -DlocalRepositoryPath=. \
      -DcreateChecksum=true
```
> The script `push.sh` will help you with that, prompting you for the required data.

then `git add <whatever-you-added>`, `git commit`, and `git push` on the `repository` branch.

Repo URL: <https://raw.githubusercontent.com/OlivierLD/raspberry-coffee/repository>

Example: <https://raw.githubusercontent.com/OlivierLD/raspberry-coffee/repository/oliv/raspi/coffee/common-utils/1.0/common-utils-1.0.pom>

> Note: When adding the files (`git add`), you might want to use the `-f` flag to force the jars in.

### Examples
- From Maven
```xml
<!-- https://raw.githubusercontent.com/OlivierLD/raspberry-coffee/repository -->
<dependency>
    <groupId>oliv.raspi.coffee</groupId>
    <artifactId>common-utils</artifactId>
    <version>1.0</version>
</dependency>
```

- From Gradle
```groovygit
    . . .
    implementation 'oliv.raspi.coffee:common-utils:1.0'
    . . .
}    
```

> **Note, for Java**
> - Make sure the artifacts are compiled with the right Java version before committing and pushing them, some Raspberry Pi Zero might not like Java above version 8...

---
