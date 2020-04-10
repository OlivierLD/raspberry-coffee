#
# Original from https://learn.sparkfun.com/tutorials/python-programming-tutorial-getting-started-with-the-raspberry-pi/experiment-3-spi-and-analog-input
# may require a pip install spidev
#
import time
import spidev

spi_ch = 0

# Enable SPI
spi = spidev.SpiDev(0, spi_ch)
spi.max_speed_hz = 1200000

VERBOSE = True


def read_adc(adc_ch):
    # Make sure ADC channel is 0 or 1
    if adc_ch != 0:
        adc_ch = 1

    # Construct SPI message
    #  First bit (Start): Logic high (1)
    #  Second bit (SGL/DIFF): 1 to select single mode
    #  Third bit (ODD/SIGN): Select channel (0 or 1)
    #  Fourth bit (MSFB): 0 for LSB first
    #  Next 12 bits: 0 (don't care)
    msg = 0b11
    msg = ((msg << 1) + adc_ch) << 5
    if VERBOSE:
        print("msg: {0:b}".format(msg))
    msg = [msg, 0b00000000]

    reply = spi.xfer2(msg)

    if VERBOSE:
        print("reply: {}".format(reply))

    # Construct single integer out of the reply (2 bytes)
    adc = 0
    for n in reply:
        adc = (adc << 8) + n

    if VERBOSE:
        print("ADC after loop: {0:b}".format(adc))

    # Last bit (0) is not part of ADC value, shift to remove it
    adc = adc >> 1

    if VERBOSE:
        print("returned ADC: {0:b} ({})".format(adc, adc))

    return adc


def read_volts(adc_ch, vref=3.3):
    adc = read_adc(adc_ch)
    return (vref * adc) / 1024


# Report the channel 0 and channel 1 voltages to the terminal
READ_ONLY_ONE = True
try:
    while True:
        adc_0 = read_volts(0)
        if not READ_ONLY_ONE:
            adc_1 = read_volts(1)
            print("Ch 0: {}V, Ch1: {}V".format(round(adc_0, 2), round(adc_1, 2)))
        else:
            print("Ch 0: {}V".format(round(adc_0, 2)))
        time.sleep(0.2)

finally:
    print("Bye!")
