#
# Low Pass Filter, Python
#
import random

ALPHA = 0.015


def low_pass(alpha, value, acc):
    return (value * alpha) + (acc * (1 - alpha))


double_data = []
# double_data.append(12.34)
random.seed(12345)
for x in range(0, 100):
    double_data.append(random.random())
print(double_data)

filtered_data = []
acc = 0
for d in double_data:
    acc = low_pass(ALPHA, d, acc)
    filtered_data.append(acc)
print(filtered_data)
