#
# Reading a Joystick, 101.
#
import signal
import sys

import constants

ba = []
try:
    # joystick_input = open(constants.JOYSTICK_INPUT_0, "rb")  # rb: read, binary
    joystick_input = open(constants.SIMULATOR, "rb")  # rb: read, binary

    def interrupt(signal, frame):
        joystick_input.close()
        print("\nCtrl+C >> Bye!")
        sys.exit(0)

    signal.signal(signal.SIGINT, interrupt)

    while True:
        # print("        Tonk!")

        b = joystick_input.read(1)
        if len(b) == 1:
            print("read {0:08b}".format(b[0]), " 0x{:02X}".format(b[0]))
            ba.append(b[0])
        if len(ba) == 8:
            dump = ''
            for x in ba:
                dump += "0x{:02X} ".format(x)
            print(">> 8 bytes:", dump)
            status = constants.JOYSTICK_NONE
            if ba[5] == 0x80:
                if ba[7] == 0x00:
                    status = constants.JOYSTICK_DOWN
                elif ba[7] == 0x01:
                    status = constants.JOYSTICK_LEFT
            elif ba[5] == 0x7F:
                if ba[7] == 0x00:
                    status = constants.JOYSTICK_UP
                elif ba[7] == 0x01:
                    status = constants.JOYSTICK_RIGHT
            # TODO broadcast status (callback ?)
            print("--- Status {0:08b}".format(status))
            ba.clear()
except (FileNotFoundError, EOFError):
    print("Done reading")

print("Bye...")
