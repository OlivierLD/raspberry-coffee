# Navigation Server

An illustration of the way to gather several REST Services all in one place.

- Tide REST Service
- Astro REST Service
- NMEA Multiplexer REST Service

## Try it
Build it:
```bash
 $ ../gradlew shadowJar
```

Then run the server
```bash
 $ ./runNavServer
```

Finally, use `Postman`, any `REST` client, or your browser, and reach
```
 http://localhost:9999/oplist
```

You will see it displays operations from three services.

## Use it

```
 http://localhost:9999/web/index.html
```

---

## Notice
Notice that the `RESTImplementation` only concerns `one` resource, `/oplist`.
All the others come from different projects.

Everything is pulled together by the build (`shadowJar` aggregates everything in only one jar file).
The biggest part of this project will be the web pages, served by the same HTTP server as the REST services.

---

