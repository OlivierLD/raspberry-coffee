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

---

More to come...

---

