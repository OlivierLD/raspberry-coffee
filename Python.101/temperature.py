import os

# execute "vcgencmd measure_temp" on Raspberry Pi.
stream = os.popen('vcgencmd measure_temp')
output = stream.read()
print(f"CPU Temperature: {output}")
