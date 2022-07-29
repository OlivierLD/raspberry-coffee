#
# Adapted from Adafruit code.
# MCP3008 Single Ended
#
import busio
import digitalio
import board
from time import sleep
import adafruit_mcp3xxx.mcp3008 as MCP
from adafruit_mcp3xxx.analog_in import AnalogIn


verbose: bool = True

# create the spi bus
spi: busio.SPI = busio.SPI(clock=board.SCK, MISO=board.MISO, MOSI=board.MOSI)
if verbose:
    print(">> SPI type: {}".format(type(spi)))

# create the cs (chip select)
cs: digitalio.DigitalInOut = digitalio.DigitalInOut(board.D5)
if verbose:
    print(">> ChipSelect type: {}".format(type(cs)))

# create the mcp object
mcp: adafruit_mcp3xxx.mcp3008.MCP3008 = MCP.MCP3008(spi, cs)
if verbose:
    print(">> MCP type: {}".format(type(mcp)))

# create an analog input channel on pin 0
chan: adafruit_mcp3xxx.analog_in.AnalogIn = AnalogIn(mcp, MCP.P0)
if verbose:
    print(">> Channel type: {}".format(type(chan)))


print('-- First display --')
print('Raw ADC Value: ', chan.value)
print('ADC Voltage: ' + str(chan.voltage) + 'V')
print('-------------------')

keep_looping: bool = True

while keep_looping:
    try:
        value: int = chan.value
        voltage: float = chan.voltage
        adc: int = int((voltage / 3.3) * 1023)
#         print('Raw ADC Value: ', value)
#         print('ADC Voltage: ' + str(voltage) + 'V')
        print("Raw: {}, Voltage: {}, ADC: {}".format(value, str(voltage), adc))   # Look into {adc: %d}...
        sleep(0.5)
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        break
    except:
        traceback.print_exc(file=sys.stdout)

print('Bye ! Done for now with MCP3008.')
