# History
- Before... who knows!
  - The repo starts growing bigger and bigger..., eventually too big
- July 5, 2021
  - Separated the `AstroComputer` in its own repo, used as a git sub-module.
  - Next, Separate NMEA/Nav from Raspberry Pi breakout boards
    - will move the NMEA/RESTServer parts in another repo
- July 2022
  - Put the `AstroComputer` sub-module back in this project, as another Gradle module.
  - Separated the NMEA and Sailing-related parts as Gradle modules
  - Done the same for the modules relying on `PI4J`, providing this way the infrastructure for
    other IO libraries, like `diozero`
  - We now have
    ```
    + astro-computer
    | + AstroComputer
    + raspberry-io-pi4j
    | + ADC
    | + ADC-benchmark
    | + . . .
    | + I2C-SPI
    | + . . .
    | + Utils
    + raspberry-sailor
    | + NMEA-Parser
    | + TideEngine
    ```
  - The point of truth for the dependencies remains the `build.gradle` of each module.

# TODO
- Separate REST and Navigation (NMEA) parts in another repo
  - May take some time...
- Sync the Maven branches.

# Dependencies (known ones)
- Depends on
    - [~~AstroComputer~~](https://github.com/OlivierLD/AstroComputer)
    

--- 

