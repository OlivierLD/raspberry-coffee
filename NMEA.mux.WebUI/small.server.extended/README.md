This example shows how to _**extend**_  the `NavServer`.

It shows how to add features like **TWO** push-buttons, and take ownership of a screen (Nokia, SSD1306...)

It comes with a class named `navserver.ServerWithKewlButtons`, that extends the `navrest.NavServer`.
As a result, it's driven by the exact same `properties` file.

To see how to interact with the buttons (to start and stop the logging for example, or
to shutdown the whole server), look for the variables named `buttonOne` and `buttonTwo`.

Implements `simple-click`, `double-click`, and `long-click`.

```java
final static PushButtonMaster pbmOne = new PushButtonMaster();
final static PushButtonMaster pbmShift = new PushButtonMaster();
```

This is built just like the other examples in this module, just run
```
 $ ./builder.sh
 ```
 and follow the instructions in the console.
 
---
 
More to come. Diagrams, screenshots, pictures.

See [here](../../Project.Trunk/REST.clients/TCP.Watch.01/README.md#raspberry-pi-zero-w-and-ssd1306-128x64).
