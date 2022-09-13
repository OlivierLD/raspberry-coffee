# Externalize the Mux's properties
This shows how to have properties file(s) and related artifacts
in some specific folder, in order to keep the place cleaner.

For example, in the structure seen here, you could - from the root of this module (where `mux.sh` lives), run a command like
```
$ ./mux.sh mux.props/nmea.mux.replay.big.log.yaml
```
This will tell the multiplexer to go and get the properties file named `nmea.mux.replay.big.log.yaml`
in the `mux.props` directory.

> It is important to keep in mind that the file names potentially present in the 
> properties files are to be given relatively to the location of `mux.sh` ⚠️  
>
> For example:
> - in `nmea.mux.replay.big.log.yaml`:   
>   `filename: ./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea.zip`    
>   refers to a file in `sample.data`, _sibling of_ `mux.props`, _not **under**_ `mux.props`.  
> - in `log.sqlite.yaml`:  
>   - `deviation.file.name: "dp_2011_04_15.csv"` 
>   - `filename: ./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea.zip`
>   - `properties: mux.props/sqlite.fwd.properties`  
>   The files above are named relatively to the location of `mux.sh`.  
>   Notice that `sqlite.fwd.properties` is a sibling of `log.sqlite.yaml`.  
>   Then, `sqlite.fwd.properties` itself refers to resources, `db.url=jdbc:sqlite:nmea.db` refers
>   to the DB (`nmea.db`) located in the root directory, next to `mux.sh`

