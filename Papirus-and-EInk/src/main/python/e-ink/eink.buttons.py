import traceback
import sys
import digitalio
import board
from time import sleep

up_button = digitalio.DigitalInOut(board.D5)
up_button.switch_to_input()
down_button = digitalio.DigitalInOut(board.D6)
down_button.switch_to_input()

keep_looping = True

while keep_looping:
    try:
        if not up_button.value:
            print("Up Button Pushed")
        if not down_button.value:
            print("Down Button Pushed")
        sleep(0.5)
    except KeyboardInterrupt:
        print("\n\t\tUser interrupted, exiting.")
        keep_looping = False
        break
    except:
        traceback.print_exc(file=sys.stdout)

print("Bye!")



