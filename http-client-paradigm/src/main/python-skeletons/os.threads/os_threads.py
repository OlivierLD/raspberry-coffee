import threading
import os
import sys
import traceback

#
# Starts 3 threads (wasting time)
# running OS commands
#

def execOS(cmd: str) -> None:
    print(f"Will execute {cmd}")
    stream = os.popen(cmd)
    output = stream.read()  # return something ?
    # Could also use output.rstrip() to remove trailing NL
    print(f"Returned: {output}")


try:
    cmd: str = "sleep 3; echo 'Done - 1'"
    thread1: threading.Thread = threading.Thread(name="One", target=execOS, args=(cmd,))
    thread1.daemon = True  # Dies on exit

    cmd = "sleep 2; echo 'Done - 2'"
    thread2: threading.Thread = threading.Thread(name="Two", target=execOS, args=(cmd,))
    thread2.daemon = True  # Dies on exit

    cmd = "sleep 1; echo 'Done - 3'"
    thread3: threading.Thread = threading.Thread(name="Three", target=execOS, args=(cmd,))
    thread3.daemon = True  # Dies on exit

    thread1.start()
    thread2.start()
    thread3.start()
except Exception as ex:
    print("Exception!")
    traceback.print_exc(file=sys.stdout)

keep_looping: bool = True
while keep_looping:
    print("Hit Return to exit (when done)")
    user_input: str = input()
    keep_looping = False

print("Bye!")
