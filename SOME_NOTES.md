Raspberry Zero - and others, older versions (A+, B2, etc) might not like a JDK
version above 8.

---

If you have a message like this during the build:
```
sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```
just re-install your JDK!
```
sudo apt-get update 
sudo apt-get install openjdk-8-jdk-headless
```

---

To switch between JDK versions:
```
$ [sudo] update-alternatives --config java
$ [sudo] update-alternatives --config javac
```

---
