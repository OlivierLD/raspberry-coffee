from sense_hat import SenseHat

sense = SenseHat()


# Define the functions
def red():
  sense.clear(255, 0, 0)


def blue():
  sense.clear(0, 0, 255)


def green():
  sense.clear(0, 255, 0)


def yellow():
  sense.clear(255, 255, 0)


# Tell the program which function to associate with which direction
sense.stick.direction_up = red
sense.stick.direction_down = blue
sense.stick.direction_left = green
sense.stick.direction_right = yellow
sense.stick.direction_middle = sense.clear    # Press the enter key

while True:
  pass  # This keeps the program running to receive joystick events
