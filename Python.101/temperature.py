import os

# execute "vcgencmd measure_temp" on Raspberry Pi.
stream = os.popen('vcgencmd measure_temp')
output = stream.read()  # return something like "temp=67.7'C"
output = output[output.index('=') + 1: -2]
print(f"CPU Temperature: {output}\272C")
