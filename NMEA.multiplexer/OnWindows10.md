As the Multiplexer may run somewhere else than on a Raspberry Pi (Linux or MacOS will support the same commands as on the Raspberry Pi), here are some
instructions to run it on Windows.

## Instructions for Windows 10
When asked to type commands, type only the commands in **bold**.
Others are sample outputs.

- Make sure `git` and `java` are available on your machine, from a `Terminal`, type
<pre>
 C:\Users\olivier><b>java -version</b>
 java version "1.8.0_131"
 Java(TM) SE Runtime Environment (build 1.8.0_131-b11)
 Java HotSpot(TM) Client VM (build 25.131-b11, mixed mode)

</pre>
and 
<pre>
 C:\Users\olivier><b>git --version</b>
 git version 2.13.2.windows.1

</pre>

### 1 - Clone the repo
From a DOS Terminal, in a directory of your choice (does not need to be a new one, a new one will be created):
<pre>
 C:\Users\olivier><b>git clone https://github.com/OlivierLD/raspberry-coffee.git</b>
 Cloning into 'raspberry-coffee'...
 remote: Enumerating objects: 8, done.
 remote: Counting objects: 100% (8/8), done.
 remote: Compressing objects: 100% (8/8), done.
 remote: Total 57747 (delta 0), reused 4 (delta 0), pack-reused 57739R
 Receiving objects: 100% (57747/57747), 392.09 MiB | 2.17 MiB/s, done.
 Resolving deltas:  13% (4777/36623)
 Resolving deltas: 100% (36623/36623), done.
 Checking out files: 100% (3972/3972), done.
 
 C:\Users\olivier>
</pre>

### 2 - Build the software
We will be using `gradlew`.

First (and only once), let's make sure all is in place:
<pre>
 C:\Users\olivier><b>cd raspberry-coffee</b>
 C:\Users\olivier\raspberry-coffee><b>cd NMEA.multiplexer</b>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>..\gradlew tasks</b>
 Downloading https://services.gradle.org/distributions/gradle-5.4.1-all.zip
 ......................................................................................
 . . .
 ...................
 Unzipping C:\Users\olivier\.gradle\wrapper\dists\gradle-5.4.1-all\3221gyojl5jsh0helicew7rwx\gradle-5.4.1-all.zip to C:\Users\olivier\.gradle\wrapper\dists\gradle-5.4.1-all\3221gyojl5jsh0helicew7rwx
 
 Welcome to Gradle 5.4.1!
 
 Here are the highlights of this release:
  - Run builds with JDK12
  - New API for Incremental Tasks
  - Updates to native projects, including Swift 5 support
 
 For more details see https://docs.gradle.org/5.4.1/release-notes.html
 
 Starting a Gradle Daemon (subsequent builds will be faster)
 
 > Configure project :
 >> From task compileJava (in raspberry-coffee), using java version 1.8
 >> From task compileTestJava (in raspberry-coffee), using java version 1.8
 
 > Task :NMEA.multiplexer:tasks
 
 ------------------------------------------------------------
 Tasks runnable from project :NMEA.multiplexer
 ------------------------------------------------------------
 
 Build tasks
 -----------
 assemble - Assembles the outputs of this project.
 build - Assembles and tests this project.
 buildDependents - Assembles and tests this project and all projects that depend on it.
 buildNeeded - Assembles and tests this project and all projects it depends on.
 classes - Assembles main classes.
 clean - Deletes the build directory.
 jar - Assembles a jar archive containing the main classes.
 testClasses - Assembles test classes.
 
 Documentation tasks
 -------------------
 javadoc - Generates Javadoc API documentation for the main source code.
 scaladoc - Generates Scaladoc for the main source code.
 
 Help tasks
 ----------
 buildEnvironment - Displays all buildscript dependencies declared in project ':NMEA.multiplexer'.
 components - Displays the components produced by project ':NMEA.multiplexer'. [incubating]
 dependencies - Displays all dependencies declared in project ':NMEA.multiplexer'.
 dependencyInsight - Displays the insight into a specific dependency in project ':NMEA.multiplexer'.
 dependentComponents - Displays the dependent components of components in project ':NMEA.multiplexer'. [incubating]
 help - Displays a help message.
 model - Displays the configuration model of project ':NMEA.multiplexer'. [incubating]
 projects - Displays the sub-projects of project ':NMEA.multiplexer'.
 properties - Displays the properties of project ':NMEA.multiplexer'.
 tasks - Displays the tasks runnable from project ':NMEA.multiplexer'.
 
 IDE tasks
 ---------
 cleanIdea - Cleans IDEA project files (IML, IPR)
 idea - Generates IDEA project files (IML, IPR, IWS)
 
 Shadow tasks
 ------------
 knows - Do you know who knows?
 shadowJar - Create a combined JAR of project and runtime dependencies
 
 Verification tasks
 ------------------
 check - Runs all checks.
 test - Runs the unit tests.
 
 <=============> 100% CONFIGURING [22m 35s]
 > Resolve dependencies of :NMEA.multiplexer:runtimeClasspath
 <=============> 100% CONFIGURING [20m 25s]t files of a task.
 <=============> 100% CONFIGURING [20m 22s]es the artifacts of a configuration.
 Pattern: upload<ConfigurationName>: Assembles and uploads the artifacts belonging to a configuration.
 <=============> 100% CONFIGURING [20m 19s]
 To see all tasks and more detail, run gradlew tasks --all
 <=============> 100% CONFIGURING [20m 18s]
 To see more detail about a task, run gradlew help --task <task>
 <=============> 100% CONFIGURING [20m 17s]
 Deprecated Gradle features were used in this build, making it incompatible with Gradle 6.0.
 <=============> 100% CONFIGURING [20m 12s]idual deprecation warnings.
 See https://docs.gradle.org/5.4.1/userguide/command_line_interface.html#sec:command_line_warnings
 <=============> 100% CONFIGURING [20m 11s]
 BUILD SUCCESSFUL in 12m 11s
 <=============> 100% CONFIGURING [20m 10s]
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer>
</pre>

Now, we can start the actual build:
<pre>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>..\gradlew shadowJar -x :NMEA.multiplexer:compileScala</b>
 > Configure project :
 >> From task compileJava (in raspberry-coffee), using java version 1.8
 >> From task compileTestJava (in raspberry-coffee), using java version 1.8
 > Task :NMEA.multiplexer:compileJava
 Note: Some input files use unchecked or unsafe operations.
 Note: Recompile with -Xlint:unchecked for details.
 Deprecated Gradle features were used in this build, making it incompatible with Gradle 6.0.
 Use '--warning-mode all' to show the individual deprecation warnings.
 See https://docs.gradle.org/5.4.1/userguide/command_line_interface.html#sec:command_line_warnings
 <span style="color: green;">BUILD SUCCESSFUL</span> in 43s
 15 actionable tasks: 3 executed, 12 up-to-date
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer>
</pre>

### 3 - Install `java-librxtx`
Download and install a recent version of `java-librxtx`, from <http://rxtx.qbang.org/wiki/index.php/Download> for example.

Notice where this has been installed, we will need this location.

### 4 - Modify the `windows.test.bat`
In a text editor (`Notepad`, `Atom`, ...), open `windows.test.bat`.
<pre>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>Notepad windows.test.bat</b>
</pre>
You need to modify this line (#13), so it matches your settings:
<pre>
 set RXTX_HOME=<i>C:\Users\olivier\rxtx-2.1-7-bins-r2</i>
</pre>
As you would see, there are more instructions in the script itself.
### 5 - Setup the config file
This step will tell the multiplexer what to read, and what to write.
In a text editor, edit `nmea.mux.2.serial.yaml`:
<pre>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>Notepad nmea.mux.2.serial.yaml</b>
</pre>
#### Example:
See this file:
<pre>
#
# MUX definition.
#
name: "Read 2 serial ports, generate one log file, for Windows."
context:
  with.http.server: false
  init.cache: false
channels:
  - type: serial
    port: COM3
    baudrate: 4800
    verbose: false
  - type: serial
    port: COM4
    baudrate: 4800
    verbose: false
forwarders:
  - type: file
    filename: data.nmea
</pre>
This would read 2 serial ports, `COM3` and `COM4`, and merge the output into a single file named `data.nmea`.

### 6 - Run the program
This step will start the Multiplexer. As instructed by the `yaml` file above, it will read 2 serial ports and produce a log file. 
<pre>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>windows.test</b>
</pre>
Use `[Ctrl+C]` in the console where it is running to stop it when needed.

### Bonus: turn the log file into csv
Once you have completed you logging session, the log file can be turned into
a Comma Separated Value (csv) file, so it can be opened as a spreadsheet,
we have a utility for that.

<pre>
 C:\Users\olivier\raspberry-coffee\NMEA.multiplexer><b>log.to.csv data.nmea 2019Dec06.csv</b>
</pre>

---
