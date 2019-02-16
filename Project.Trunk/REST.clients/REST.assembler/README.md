# From scratch to a sensor flow

Let's says you have:
- A Soil Humidity sensor, planted in a pot, at your home
- A peristaltic pump, that can be started and stopped at will

Those two devices are connected to a Raspberry Pi that can read data from the sensor, and drive the pump.

Now, we want to expose those features to a network and build some logic around them, in order to start the pump when the soil
humidity drops below a given threshold.

## Create the REST services
We start from the code that allows you to read data emitted by various sensors,
many such examples are available in this repository.

> For example:
> Look in the `I2C.SPI` module.

### Using Swagger (aka Open API)
[`Swagger`](https://swagger.io/) has been designed to facilitate the development of REST Services.
You can start from the service definition (in `json` or `yaml` format, `yaml` being the easiest to deal with), and then
you can generate the skeleton of your implementation (in the language of your choice), for the client, for the server, _as well as the documentation_ of your services,
based on the `json` or `yaml` definition you started from.
This documentation part is a very cool feature.

#### Micro Service?
Serverless... Actually means that the server can be anywhere, everywhere, etc.

##### Helidon
Quite mature. Its `micro-profile` feature is quite appealing.

##### fnProject
Still in development, but quite promising.

### Using a light custom HTTP Server
Less snappy than `Swagger`, but eventually lighter, in term of footprint.
For small boards (like the Raspberry Pi Zero), this would be my preferred option.

## Run the server


## Build a flow using Node-RED


## Try it!

