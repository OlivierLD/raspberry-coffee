import signal
import constants
import joystick_reader as jsr
import python_websocket as pwsf

#
# Sample implementation, main script.
# Reads the joystick (one here), and feeds a WebSocket server
#

wsf = pwsf.WebSocketFeeder("ws://localhost:9876/")


def callback_example(status):
    print("--- In callback: Status {0:08b}".format(status))
    wsf.send(status)


js1 = jsr.JoystickReader(constants.SIMULATOR, callback_example)


def interrupt(signal, frame):
    print("\nCtrl+C intercepted!")
    js1.stop_reading()
    wsf.close()


signal.signal(signal.SIGINT, interrupt)
js1.start_reading()

print("Bye...")
