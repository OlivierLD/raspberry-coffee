[
    {
        "id": "2de39f46.caf55",
        "type": "log-replay",
        "z": "f29139a8.882258",
        "path": "/home/pi/raspberry-coffee/NMEA-multiplexer/sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea",
        "freq": 1,
        "loop": false,
        "verbose": false,
        "x": 190.5,
        "y": 297,
        "wires": [
            [
                "400c2d33.94f444",
                "df868434.a7ac68"
            ]
        ]
    },
    {
        "id": "400c2d33.94f444",
        "type": "debug",
        "z": "f29139a8.882258",
        "name": "Replayed Data",
        "active": false,
        "console": "false",
        "complete": "true",
        "x": 590.5,
        "y": 292,
        "wires": []
    },
    {
        "id": "df868434.a7ac68",
        "type": "function",
        "z": "f29139a8.882258",
        "name": "NMEA Parser",
        "func": "/*\n * Invoke the NMEA Parser mentioned in the Global settings.\n */\nvar parser = context.global.NMEAParser;\nvar processed = {};\nif (parser !== undefined) {\n    var nmeaSentence = msg.payload;\n    var verbose = msg.verbose;\n\n    if (verbose === true) {\n        console.log(\"Raw data:\", msg);\n    }\n\n    if (nmeaSentence !== undefined && nmeaSentence.trim().length > 0) {\n        while (nmeaSentence.endsWith('\\n') ||\n        nmeaSentence.endsWith('\\r')) {\n            nmeaSentence = nmeaSentence.substring(0, nmeaSentence.length - 1);\n        }\n        var id;\n        try {\n            if (verbose === true) {\n                console.log('Validating', nmeaSentence);\n            }\n            id = parser.validate(nmeaSentence); // Validation!\n        } catch (err) {\n            console.log('Validation error:', err);\n        }\n        if (verbose === true) {\n            console.log(\"Sentence ID for \" + nmeaSentence + \":\", id);\n        }\n        if (id !== undefined) {\n            try {\n                var autoparsed = parser.autoparse(nmeaSentence);\n                if (autoparsed !== undefined) {\n                    processed = autoparsed;\n                }\n            } catch (err) {\n                console.log('Parsing Error:', err);\n            }\n        }\n    }\n} else {\n    console.log(\"no NMEAParser was found.\");\n}\nreturn {payload: processed};",
        "outputs": 1,
        "noerr": 0,
        "x": 399.5,
        "y": 506,
        "wires": [
            [
                "8eb77214.d64d2"
            ]
        ]
    },
    {
        "id": "8eb77214.d64d2",
        "type": "debug",
        "z": "f29139a8.882258",
        "name": "Parsed Data",
        "active": false,
        "console": "false",
        "complete": "payload",
        "x": 691.5,
        "y": 506,
        "wires": []
    }
]