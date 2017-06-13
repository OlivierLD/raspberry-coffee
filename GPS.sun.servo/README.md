## WIP
Requires resource from other projects:

Check this one https://github.com/OlivierLD/oliv-soft-project-builder

Build it as instructed.

Then you need to do those things to `install` (Maven) the nautical almanac components in your local Maven repository:

```bash
 $ cd olivsoft
 $ cd javanauticalalmanac
 $ ../gradlew clean build install
 $ cd ../geomutils
 $ ../gradlew clean build install
```

Then the build of this project should work:
```bash
 GPS.sun.servo $ ../gradlew clean shadowJar
```