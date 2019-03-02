## Close to production
This project is the one to run to have the features you are interested in also available from a Web UI.

Pick and choose your features in other modules, grab the web ages you need, modify the `index.html`, etc, and
run the script `to.prod.sh` to package everything for distribution.

The "_pick and choose_" part could be scripted as well.

This project is not supposed to contain any source file. 

It pulls the `NMEA.multiplexer`, `RESTNavServer` (or whatever you want) and the `NMEA.mux.extensions` projects.
This is what you would to tweak to fit your requirements.

The file `rc.local` is to give you some inspiration, so you can modify the one in `/etc/rc.local`
on the destination machine to start the Multiplexer at boot time.

The script `to.prod.sh` is not carved in stone. It is also here for inspiration.

## Warning!
This project directory is a play ground, again, it is here for **you** to _compose_ your own server.

**You**. 

Means not **me**.

---
