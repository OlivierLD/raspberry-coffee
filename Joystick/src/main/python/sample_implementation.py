import signal
import constants
import joystick_reader as jsr

#
# Sample implementation, main script.
#


def callback_example(status):
    print("--- In callback: Status {0:08b}".format(status))


js1 = jsr.JoystickReader(constants.SIMULATOR, callback_example)


def interrupt(signal, frame):
    print("\nCtrl+C intercepted!")
    js1.stop_reading()


signal.signal(signal.SIGINT, interrupt)

js1.start_reading()

print("Bye...")
