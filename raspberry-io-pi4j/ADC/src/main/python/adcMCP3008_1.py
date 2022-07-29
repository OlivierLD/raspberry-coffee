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

# create the spi bus
spi = busio.SPI(clock=board.SCK, MISO=board.MISO, MOSI=board.MOSI)

# create the cs (chip select)
cs = digitalio.DigitalInOut(board.D5)

# create the mcp object
mcp = MCP.MCP3008(spi, cs)

# create an analog input channel on pin 0
chan = AnalogIn(mcp, MCP.P0)

print('-- First display --')
print('Raw ADC Value: ', chan.value)
print('ADC Voltage: ' + str(chan.voltage) + 'V')
print('-------------------')

keep_looping: boolean = True

while keep_looping:
    try:
        value: int = chan.value
        voltage: float = chan.voltage
        adc: int = (3.3 / voltage) * 1023
#         print('Raw ADC Value: ', value)
#         print('ADC Voltage: ' + str(voltage) + 'V')
        print(f"Raw: {value}, Voltage: {str(voltage)}, ADC: {adc}")   # Look into {adc: %d}...
        sleep(0.5)
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        break
    except:
        traceback.print_exc(file=sys.stdout)

print('Bye ! Done for now with MCP3008.')
