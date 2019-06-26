#
# Reading a Joystick, 101.
# class JoystickReader
#
import constants


class JoystickReader:

    def __init__(self, name, callback):
        self.name = name
        self.callback = callback
        self.keep_reading = True

    def stop_reading(self):
        self.keep_reading = False

    def start_reading(self):
        ba = []
        try:
            joystick_input = open(self.name, "rb")  # rb: read, binary
            while self.keep_reading:
                # print("        Tonk!")
                b = joystick_input.read(1)
                if len(b) == 1:
                    # print("read {0:08b}".format(b[0]), " 0x{:02X}".format(b[0]))
                    ba.append(b[0])
                if len(ba) == 8:
                    dump = ''
                    for x in ba:
                        dump += "0x{:02X} ".format(x)
                    # print(">> 8 bytes:", dump)
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
                    # broadcast status (callback ?)
                    if self.callback is not None:
                        self.callback(status)
                    else:
                        print("--- Status {0:08b}".format(status))
                    ba.clear()
            print("Releasing {}".format(self.name))
            joystick_input.close()
        except (FileNotFoundError, EOFError):
            print("Done reading")
