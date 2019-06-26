#
# Reading a Joystick, 101.
#
JOYSTICK_INPUT_0 = "/dev/input/js0"
JOYSTICK_INPUT_1 = "/dev/input/js1"
SIMULATOR = "sample.data.dat"

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
            ba.clear()
except (FileNotFoundError, EOFError):
    print("Done reading")

print("Bye.")
