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
`Swagger` has been designed to facilitate the development of REST Services.

### Using a light custom HTTP Server


### Micro Service?

#### Helidon

#### fnProject

## Run the server


## Build a flow using Node-RED


## Try it!

