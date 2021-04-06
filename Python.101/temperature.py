import os

# execute "vcgencmd measure_temp" on Raspberry Pi.
stream = os.popen('vcgencmd measure_temp')
output = stream.read()  # return something like "temp=67.7'C\n"
# Could use output.rstrip() to remove trailing NL
output = output[output.index('=') + 1 : -3]
print(f"CPU Temperature: {output}\272C")
