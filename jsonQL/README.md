# A JSON Utility, WiP

To use like 
```text
curl [--silent] -X GET http://192.168.42.19:8989/sf/status | java -cp ./build/libs/jsonQL-1.0-all.jar oliv.json.JsonQL --query:<Query>
```
also try
```text
curl [--silent]  -X GET http://192.168.42.19:8989/sf/status | java -Djson.debug=true -cp ./build/libs/jsonQL-1.0-all.jar oliv.json.JsonQL "--query:.*/(elevation|azimuth)"
```

### RegEx support (WiP)
Use `regex` syntax.
```text
curl [--silent] -X GET http://192.168.42.19:8989/sf/status | java -cp ./build/libs/jsonQL-1.0-all.jar oliv.json.JsonQL --query:.*_DATA/elevation
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  1130  100  1130    0     0   6890      0 --:--:-- --:--:-- --:--:--  6890
> 1: --query:.*_DATA/elevation
>> Query result for CELESTIAL_DATA/elevation: 21.134278
>> Query result for DEVICE_DATA/elevation: 21.049853
```

#### Query samples
- `--query:.*_DATA/elevation`
- `--query:.*/motor.*`
- `curl [--silent]  -X GET http://192.168.42.19:8989/sf/status | java -cp ./build/libs/jsonQL-1.0-all.jar oliv.json.JsonQL "--query:.*/(elevation|azimuth)"`

---
