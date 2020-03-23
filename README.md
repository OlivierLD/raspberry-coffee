# Maven repo for RaspberryCoffee
See good article at <https://gist.github.com/fernandezpablo85/03cf8b0cd2e7d8527063>

### Sample command:

From `raspberry-coffee/common-utils`:
```
$ ../gradlew install
```
From the root of the `repository` branch:
```
$ mvn install:install-file -DgroupId=oliv.raspi.coffee -DartifactId=common-utils -Dversion=1.0 -Dfile=/Users/olediour/.m2/repository/oliv/raspi/coffee/common-utils/1.0/common-utils-1.0.jar -Dpackaging=jar -DgeneratePom=true -DlocalRepositoryPath=.  -DcreateChecksum=true
```

---

