#
# PCA9685, servo driver
#
CENTER_PULSE = 1.5
MIN_PULSE = 1
MAX_PULSE = 2


def get_servo_value(freq: int, pulse: float) -> int:
	pulse_length = 1000000.0    # 1,000,000 us per second
	pulse_length //= float(freq)       # freq in Hz
	# print('\tFor {0} Hz, {1}us per period'.format(freq, pulse_length))
	pulse_length = float(pulse_length) / 4096.0     # 12 bits of resolution
	# print('\tFor {0} Hz, {1}us per bit (pulse length)'.format(freq, pulse_length))
	val = pulse * 1000
	val //= pulse_length
	return int(val)


def get_min_pulse(freq: int) -> int:
	return get_servo_value(freq, MIN_PULSE)


def get_max_pulse(freq: int) -> int:
	return get_servo_value(freq, MAX_PULSE)


def get_center_pulse(freq: int) -> int:
	return get_servo_value(freq, CENTER_PULSE)


def get_pulse_from_value(freq: int, value: int) -> float:
	ms_per_period = 1000.0 / float(freq)
	pulse_in_ms = ms_per_period * (float(value) / 4096.0)
	return pulse_in_ms


freq = 60
print('for freq {0} Hz, min servo value is {1}, center is {2}, max is {3}'.format(freq, get_min_pulse(freq), get_center_pulse(freq), get_max_pulse(freq)))
freq = 50
print('for freq {0} Hz, min servo value is {1}, center is {2}, max is {3}'.format(freq, get_min_pulse(freq), get_center_pulse(freq), get_max_pulse(freq)))
freq = 250
print('for freq {0} Hz, min servo value is {1}, center is {2}, max is {3}'.format(freq, get_min_pulse(freq), get_center_pulse(freq), get_max_pulse(freq)))
freq = 1000
print('for freq {0} Hz, min servo value is {1}, center is {2}, max is {3}'.format(freq, get_min_pulse(freq), get_center_pulse(freq), get_max_pulse(freq)))

freq = 60
val = 150
print('For {0} Hz, {1} gives a pulse of {2} ms'.format(freq, val, get_pulse_from_value(freq, val)))
val = 600
print('For {0} Hz, {1} gives a pulse of {2} ms'.format(freq, val, get_pulse_from_value(freq, val)))
