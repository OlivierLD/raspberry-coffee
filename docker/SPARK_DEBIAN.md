# Spark on Debian, Ideas, hints and comments
- Doc at <http://spark.apache.org>, then `Documentation` > `Latest` > `Programming Guides` > ... `ML Libs`
- Spark repo <https://github.com/apache/spark/tree/7deb67c28f948cca4e768317ade6d68d2534408f> 
- Connect to the Docker image:
```
$ docker run -it --rm -e USER=root oliv-spark:latest /bin/bash
 #####
#     #  #####     ##    #####   #    #
#        #    #   #  #   #    #  #   #
 #####   #    #  #    #  #    #  ####
      #  #####   ######  #####   #  #
#     #  #       #    #  #   #   #   #
 #####   #       #    #  #    #  #    #

git version 2.20.1
openjdk version "11.0.8" 2020-07-14
OpenJDK Runtime Environment (build 11.0.8+10-post-Debian-1deb10u1)
OpenJDK 64-Bit Server VM (build 11.0.8+10-post-Debian-1deb10u1, mixed mode, sharing)
Scala code runner version 2.12.12 -- Copyright 2002-2020, LAMP/EPFL and Lightbend, Inc.
-------------------------
From /workdir, cd spark-3.0.1-bin-hadoop2.7-hive1.2 spark.tgz
Then ./bin/spark-shell
as well as ./bin/pyspark
or ./bin/run-example org.apache.spark.examples.SparkPi
-------------------------
root@4e268c648d07:/workdir#
```
You might want to run Python3 instead of Python, edit `bin/pyspak`:
```
# Default to standard python interpreter unless told otherwise
if [[ -z "$PYSPARK_PYTHON" ]]; then
  PYSPARK_PYTHON=python3
fi
```
Then go down one level and start the PyShell:
```
$ cd spark-3.0.1-bin-hadoop2.7-hive1.2/
$ ./bin/pyspark
  Python 3.7.3 (default, Jul 25 2020, 13:03:44) 
  [GCC 8.3.0] on linux2
  Type "help", "copyright", "credits" or "license" for more information.
  WARNING: An illegal reflective access operation has occurred
  WARNING: Illegal reflective access by org.apache.spark.unsafe.Platform (file:/workdir/spark-3.0.1-bin-hadoop2.7-hive1.2/jars/spark-unsafe_2.12-3.0.1.jar) to constructor java.nio.DirectByteBuffer(long,int)
  WARNING: Please consider reporting this to the maintainers of org.apache.spark.unsafe.Platform
  WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
  WARNING: All illegal access operations will be denied in a future release
  20/10/09 19:56:10 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
  Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
  Setting default log level to "WARN".
  To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
  /workdir/spark-3.0.1-bin-hadoop2.7-hive1.2/python/pyspark/context.py:225: DeprecationWarning: Support for Python 2 and Python 3 prior to version 3.6 is deprecated as of Spark 3.0. See also the plan for dropping Python 2 support at https://spark.apache.org/news/plan-for-dropping-python-2-support.html.
    DeprecationWarning)
  Welcome to
        ____              __
       / __/__  ___ _____/ /__
      _\ \/ _ \/ _ `/ __/  '_/
     /__ / .__/\_,_/_/ /_/\_\   version 3.0.1
        /_/
  
  Using Python version 2.7.16 (default, Oct 10 2019 22:02:15)
  SparkSession available as 'spark'.
  >>>
```
Then try this:
```
>>> from pyspark.sql import SparkSession
>>> spark = SparkSession.builder.appName("cluster").getOrCreate()
>>> # Do your stuff here
>>> . . .
>>> spark.stop()
```

## Scala
Run ` ./bin/spark-shell `
```
. . .
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
Spark context Web UI available at http://4e268c648d07:4040
Spark context available as 'sc' (master = local[*], app id = local-1602275094801).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.0.1
      /_/
         
Using Scala version 2.12.10 (OpenJDK 64-Bit Server VM, Java 11.0.8)
Type in expressions to have them evaluated.
Type :help for more information.

scala>
```

The `spark-shell` creates a session named spark,but you can create your own.

Then
```scala
import org.apache.spark.{SparkConf, SparkContext, SparkException}
import org.apache.spark.sql.SparkSession

val session = SparkSession.builder().appName("oliv-test").getOrCreate()
// . . . 
session.stop()

```


### Docker reminder
To be able to save the state of a docker container, and then reuse it, you need to do the following:
- Let's say you've run the commands above, to build the APEX instance
- From the host, run a 
```
 $ docker ps -a
CONTAINER ID        IMAGE                       COMMAND                  CREATED             STATUS                  PORTS                                              NAMES
20b372c88eb6        oracle/database:18.4.0-xe   "/bin/sh -c 'exec $O…"   23 hours ago        Up 23 hours (healthy)   0.0.0.0:51521->1521/tcp, 0.0.0.0:55500->5500/tcp   myxedb
 $
```
- Then you can save the container state into a new image
```
 $ docker commit 20b372c88eb6 apex:2020-09-20
```
- You can exit the docker session.
```
[oracle@73018a29f867 /]$ exit
```
- To reuse the image, as it was when archived by the `commit`
```
$ docker run --name myxedb -d -p 51521:1521 -p 55500:5500 -e ORACLE_PWD=mysecurepassword -e ORACLE_CHARACTERSET=AL32UT apex:2020-09-20
```

#### Send a file to a docker container
```
$ docker cp some.zip myxedb:/path/to-file/some.zip
```             
where `myxedb` is the name or the `ID` of the container.

---

... More here soon.
