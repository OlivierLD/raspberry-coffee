# Some C clients samples
Each sub-directory contains the sc ripts used to compile and run the code.

- In `http101`, `httpClient.c`
  - Make REST requests from C to the NMEA-Multiplexer.
    - The `compile.sh` script will compile, nd possibly run the generated executable
    - The runtime can take several user parameters
    - `./httpClient --verbose:true --machine-name:localhost --port:9999 --query:/oplist`
    - All the parameters have default values, you are prompted when running the `run.sh` script, itself launched from the `compile.sh`, upon user's request  
      ```text
      $ ./compile.sh 
      Compiling httpClient.c
      -- GCC Version --
      Configured with: --prefix=/Library/Developer/CommandLineTools/usr --with-gxx-include-dir=/Library/Developer/CommandLineTools/SDKs/MacOSX.sdk/usr/include/c++/4.2.1
      Apple clang version 11.0.3 (clang-1103.0.32.62)
      Target: x86_64-apple-darwin21.6.0
      Thread model: posix
      InstalledDir: /Library/Developer/CommandLineTools/usr/bin
      -----------------
      Enable _DEBUG flag y|n ? > n
      Compilation OK
      Do we run the program  y|n ? > y
      This requires a Multiplexer to be running, with a given HTTP Port
      ==> Enter Multiplexer machine name or IP (default 'localhost'):
      ==> Enter Multiplexer HTTP port (default 9999):
      ==> Enter REST query (default '/mux/cache'): /oplist
      ==> With verbose option (default false):
      Command will be: ./httpClient --query:/oplist
      With jq y|n > ? y
      {
        "Damping": 30,
        "HDG Offset": 0,
        "To Waypoint": "TNGRVA  ",
        "CDR": {
          "angle": 29.666116882664085
        },
      . . .
      ```
    - default REST Query is `/mux/cache`, also try `/oplist`...

More to come

--- 

