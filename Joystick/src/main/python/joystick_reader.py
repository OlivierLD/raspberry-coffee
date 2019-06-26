#
# Reading a Joystick, 101.
#
JOYSTICK_INPUT_0 = "/dev/input/js0"
JOYSTICK_INPUT_1 = "/dev/input/js1"
SIMULATOR        = "sample.data.dat"

JOYSTICK_NONE  = 0x00
JOYSTICK_LEFT  = 0x01
JOYSTICK_RIGHT = 0x01 << 1
JOYSTICK_UP    = 0x01 << 2
JOYSTICK_DOWN  = 0x01 << 3

ba = []
try:
    # joystick_input = open(JOYSTICK_INPUT_0, "rb")  # rb: read, binary
    joystick_input = open(SIMULATOR, "rb")  # rb: read, binary
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
            status = JOYSTICK_NONE
            if ba[5] == 0x80:
                if ba[7] == 0x00:
                    status = JOYSTICK_DOWN
                elif ba[7] == 0x01:
                    status = JOYSTICK_LEFT
            elif ba[5] == 0x7F:
                if ba[7] == 0x00:
                    status = JOYSTICK_UP
                elif ba[7] == 0x01:
                    status = JOYSTICK_RIGHT
            # TODO broadcast status (callback ?)
            print("--- Status {0:08b}".format(status))
            ba.clear()


except (FileNotFoundError, EOFError):
    print("Done reading")

print("Bye.")
