#!/usr/bin/env python3
"""
Generate a ZDA string from system time
every second
"""
import signal
import time
import NMEABuilder  # local script

# Now we're talking...
keep_listening: bool = True


def interrupt(signal, frame):
    global keep_listening
    print("\nCtrl+C intercepted!")
    keep_listening = False
    # sys.exit()   # DTC


signal.signal(signal.SIGINT, interrupt)  # callback, defined above.

while keep_listening:
    print(f"Generated ZDA: {NMEABuilder.build_ZDA()}")
    time.sleep(1)  # 1 sec.


print("Bon. OK.")
