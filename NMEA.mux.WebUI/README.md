## Close to production
This project is the one to run to have the features you are interested in also available from a Web UI.

Pick and choose your features in other modules, grab the web ages you need, modify the `index.html`, etc, and
run the script `to.prod.sh` to package everything for distribution.

The "_pick and choose_" part could be scripted as well.

This project is not supposed to contain any source file except web resources.

It pulls the `NMEA.multiplexer`, `RESTNavServer` (or whatever you want) and the `NMEA.mux.extensions` projects.
This is what you would to tweak to fit your requirements.

When available, the file `rc.local` is to give you some inspiration, so you can modify the one in `/etc/rc.local`
on the destination machine to start the Multiplexer at boot time.

The script `to.prod.sh` is not carved in stone. It is also here for inspiration.

> Note: The build process might be a bit too heavy for a Raspberry Pi Zero...
> I usually built on a bigger board (A, or B), and the `scp` the result to a Raspberry Pi Zero if I need one.

## Warning!
This project directory is a play ground, again, it is here for **you** to _compose_ your own server.

**You**. 

Means not **me**. 🤓

## Examples
- Full Nav Server (all features)
```
 $ cd full.server
 $ ./builder.sh
```

- Minimal Multiplexer
```
 $ cd minimal.mux
 $ ./builder.sh
```

... More to come

---
