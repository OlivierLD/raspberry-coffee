### Data for Routing (&#10140; WiP)
This will be integrated in teh Weather Wizard GUI.

##### Polar file
- Passed as System variable when starting the server, like in `-Dpolar.file.location=./sample.data/polars/CheoyLee42.polar-coeff`.
- Can be retrieved from the server with a `GET /nav/polar-file-location`.

##### GRIB File
- The GRIB file list (the ones stored on the server) can be obtained via a `GET /ww/composite-hierarchy`, it returns a response like that:
```json
{
    "2018": {
        "11": {
            "12": [
                {
                    "name": "PAC-0001_081150",
                    "compositeElements": [
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_0.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/12/PAC-0001_081150/_PAC-0001_0.png"
                        },
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_1.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/12/PAC-0001_081150/_PAC-0001_1.png"
                        },
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_2.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/12/PAC-0001_081150/_PAC-0001_2.png"
                        },
                        {
                            "type": "GRIB",
                            "name": "grib.grb",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/12/PAC-0001_081150/grib.grb"
                        }
                    ]
                }
            ],
            "08": [
                {
                    "name": "ATL-0001_112153",
                    "compositeElements": [
                        {
                            "type": "FAX",
                            "name": "_ATL-0001_0.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/ATL-0001_112153/_ATL-0001_0.png"
                        },
                        {
                            "type": "FAX",
                            "name": "_ATL-0001_1.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/ATL-0001_112153/_ATL-0001_1.png"
                        },
                        {
                            "type": "GRIB",
                            "name": "grib.grb",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/ATL-0001_112153/grib.grb"
                        }
                    ]
                },
                {
                    "name": "PAC-0001_112054",
                    "compositeElements": [
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_0.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/PAC-0001_112054/_PAC-0001_0.png"
                        },
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_1.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/PAC-0001_112054/_PAC-0001_1.png"
                        },
                        {
                            "type": "FAX",
                            "name": "_PAC-0001_2.png",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/PAC-0001_112054/_PAC-0001_2.png"
                        },
                        {
                            "type": "GRIB",
                            "name": "grib.grb",
                            "resource": "file:/Users/olediour/repos/raspberry-coffee/RESTNavServer/launchers/web/2018/11/08/PAC-0001_112054/grib.grb"
                        }
                    ]
                }
            ]
        }
    }
}
```

##### Routing Request
- To get the best route, `POST /grib/routing` with a payload like
```json
{
    "fromL": 37.122,
    "fromG": -122.5,
    "toL": -9.75,
    "toG": -139.1,
    "startTime": "2018-11-12T07:00:00",
    "gribName": "./web/2018/11/12/PAC-0001_081150/grib.grb",
    "polarFile": "./sample.data/polars/CheoyLee42.polar-coeff",
    "outputType": "JSON",
    "timeInterval": 24,
    "routingForkWidth": 140,
    "routingStep": 10,
    "limitTWS": -1,
    "limitTWA": -1,
    "speedCoeff": 0.75,
    "proximity": 25,
    "avoidLand": false,
    "verbose": false
}
```

- Returns the best route only, no isochronals (for now, Nov 2018) in a response like
```json
{
    "waypoints": [
        {
            "datetime": "2018-11-11T23:00:00Z",
            "position": {
                "latitude": "37.122",
                "longitude": "-122.5"
            },
            "tws": 6.13,
            "twd": 176,
            "bsp": 2.4,
            "hdg": 148
        },
        {
            "datetime": "2018-11-12T23:00:00Z",
            "position": {
                "latitude": "36.30698886071364",
                "longitude": "-121.8646956594975"
            },
            "tws": 4.64,
            "twd": 117,
            "bsp": 4.61,
            "hdg": 209
        },
        {
            "datetime": "2018-11-13T23:00:00Z",
            "position": {
                "latitude": "34.69271202908721",
                "longitude": "-122.96381067836815"
            },
            "tws": 5.86,
            "twd": 112,
            "bsp": 4.82,
            "hdg": 199
        },
        {
            "datetime": "2018-11-14T23:00:00Z",
            "position": {
                "latitude": "32.86804197472556",
                "longitude": "-123.71970964937285"
            },
            "tws": 7.32,
            "twd": 67,
            "bsp": 4.73,
            "hdg": 189
        },
        {
            "datetime": "2018-11-15T23:00:00Z",
            "position": {
                "latitude": "30.997750800282894",
                "longitude": "-124.06875659164109"
            },
            "tws": 10.19,
            "twd": 59,
            "bsp": 5.16,
            "hdg": 190
        },
        {
            "datetime": "2018-11-16T23:00:00Z",
            "position": {
                "latitude": "28.966428883011602",
                "longitude": "-124.4822689970225"
            },
            "tws": 13.84,
            "twd": 65,
            "bsp": 5.73,
            "hdg": 200
        },
        {
            "datetime": "2018-11-17T23:00:00Z",
            "position": {
                "latitude": "26.812429234489695",
                "longitude": "-125.36928605832065"
            },
            "tws": 15.99,
            "twd": 67,
            "bsp": 6.08,
            "hdg": 200
        },
        {
            "datetime": "2018-11-18T23:00:00Z",
            "position": {
                "latitude": "24.52842881802859",
                "longitude": "-126.29162879092554"
            },
            "tws": 16.91,
            "twd": 73,
            "bsp": 6.26,
            "hdg": 200
        },
        {
            "datetime": "2018-11-19T23:00:00Z",
            "position": {
                "latitude": "22.17461744575187",
                "longitude": "-127.22478020452864"
            },
            "tws": 17.32,
            "twd": 71,
            "bsp": 6.29,
            "hdg": 200
        },
        {
            "datetime": "2018-11-20T23:00:00Z",
            "position": {
                "latitude": "19.810990688749502",
                "longitude": "-128.14623088387796"
            },
            "tws": 20.5,
            "twd": 75,
            "bsp": 6.65,
            "hdg": 190
        },
        {
            "datetime": "2018-11-21T23:00:00Z",
            "position": {
                "latitude": "17.19024386265838",
                "longitude": "-128.63352236746783"
            },
            "tws": 17.66,
            "twd": 76,
            "bsp": 6.43,
            "hdg": 191
        },
        {
            "datetime": "2018-11-22T23:00:00Z",
            "position": {
                "latitude": "14.66674741853043",
                "longitude": "-129.14362584035325"
            },
            "tws": 17.43,
            "twd": 63,
            "bsp": 6.42,
            "hdg": 182
        },
        {
            "datetime": "2018-11-23T23:00:00Z",
            "position": {
                "latitude": "12.098832840169534",
                "longitude": "-129.23580240714602"
            },
            "tws": 16.64,
            "twd": 70,
            "bsp": 6.25,
            "hdg": 194
        },
        {
            "datetime": "2018-11-24T23:00:00Z",
            "position": {
                "latitude": "9.671689360841508",
                "longitude": "-129.85204509882223"
            },
            "tws": 13.32,
            "twd": 72,
            "bsp": 5.83,
            "hdg": 195
        },
        {
            "datetime": "2018-11-25T23:00:00Z",
            "position": {
                "latitude": "7.4175472586270566",
                "longitude": "-130.46281997557162"
            },
            "tws": 11.53,
            "twd": 84,
            "bsp": 5.7,
            "hdg": 197
        },
        {
            "datetime": "2018-11-26T23:00:00Z",
            "position": {
                "latitude": "5.237729532703922",
                "longitude": "-131.13334201763823"
            },
            "tws": 11.55,
            "twd": 123,
            "bsp": 5.59,
            "hdg": 208
        },
        {
            "datetime": "2018-11-27T23:00:00Z",
            "position": {
                "latitude": "3.2634430985536547",
                "longitude": "-132.18598410471236"
            },
            "tws": 14.63,
            "twd": 125,
            "bsp": 5.88,
            "hdg": 208
        },
        {
            "datetime": "2018-11-28T23:00:00Z",
            "position": {
                "latitude": "1.1879452292513264",
                "longitude": "-133.290379051651"
            },
            "tws": 14.28,
            "twd": 103,
            "bsp": 6.02,
            "hdg": 208
        },
        {
            "datetime": "2018-11-29T23:00:00Z",
            "position": {
                "latitude": "-0.9379137120715006",
                "longitude": "-134.42072099182587"
            },
            "tws": 14.76,
            "twd": 96,
            "bsp": 6.11,
            "hdg": 208
        },
        {
            "datetime": "2018-11-30T23:00:00Z",
            "position": {
                "latitude": "-3.0971502694196706",
                "longitude": "-135.56951957330116"
            },
            "tws": 16.99,
            "twd": 87,
            "bsp": 6.3,
            "hdg": 208
        },
        {
            "datetime": "2018-12-01T23:00:00Z",
            "position": {
                "latitude": "-5.323133166437599",
                "longitude": "-136.7562981988888"
            },
            "tws": 15.5,
            "twd": 88,
            "bsp": 6.21,
            "hdg": 208
        },
        {
            "datetime": "2018-12-02T23:00:00Z",
            "position": {
                "latitude": "-7.516568926153888",
                "longitude": "-137.92992823315956"
            },
            "tws": 15.76,
            "twd": 96,
            "bsp": 6.22,
            "hdg": 207
        },
        {
            "datetime": "2018-12-03T23:00:00Z",
            "position": {
                "latitude": "-9.731791333664903",
                "longitude": "-139.07154851464384"
            },
            "tws": 15.76,
            "twd": 96,
            "bsp": 6.22,
            "hdg": 207
        }
    ]
}
```

---
