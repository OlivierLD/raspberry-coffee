# Send Emails from the Raspberry PI
This is a quick example, in order to present the way to use it in other projects.

### To build it
- Clone the repo as usual.
- Then
```bash
 cd PI4J.email
 ../gradlew --daemon clean shadowJar
```

### Run the example
This shows how to send emails, and how to _listen_ to emails, expecting a special one to stop the program.

The example provided here assumes that you have enabled the camera on the Raspberry PI,
and started the script named `motion.sh` as follow:
```bash
 sudo ./motion.sh &
```
This command takes a snapshot named `snap.jpg` every 10 seconds.

Then, you need to provide your email account(s) information, in a file like
`email.properties.sample`.

Make a copy of this file, named `email.properties`, and provide your account(s) info.
This  [link](http://www.arclab.com/products/amlc/list-of-smtp-and-pop3-servers-mailserver-list.html) might help.

Then you can run the command
```bash
 ./run -send:google -receive:yahoo -sendto:me@home.net,you@somewhere.else
```
This will send several emails to the `google` account (as defined in your `email.properties`), and listen to the emails in the `yahoo` account.
The sent emails will have the `snap.jpg` attached to it.

The program is finally sending an `exit` email, that terminates the program.

