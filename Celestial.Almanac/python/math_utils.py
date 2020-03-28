# Math and trigonometric utils

import math


# Sine of angles in degrees
def sind(x):
    return math.sin(math.radians(x))


# Cosine of angles in degrees
def cosd(x):
    return math.cos(math.radians(x))


# Tangent of angles in degrees
def tand(x):
    return math.tan(math.radians(x))


def norm_360_deg(x):
    while x < 0:
        x += 360
    while x > 360:
        x -= 360
    return x


# Radians
def norm2_pi_rad(x):
    while x < 0:
        x += (2 * math.pi)
    while x > (2 * math.pi):
        x -= (2 * math.pi)
    return x


# Cosine of normalized angle (in radians)
def cost(x):
    return math.cos(norm2_pi_rad(x))
