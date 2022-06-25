import threading
import os
import sys
import traceback
import time

"""
 Starts 3 threads (wasting time)
 running OS commands
"""


nbThreadCompleted: int = 0


def execOS(cmd: str) -> None:
    global nbThreadCompleted
    print(f"Will execute {cmd}")
    stream = os.popen(cmd)
    output = stream.read()  # return something ?
    # Could also use output.rstrip() to remove trailing NL
    print(f"Thread execution returned: {output}")
    nbThreadCompleted += 1


try:
    cmd: str = "sleep 5; echo 'Done - 1'"
    thread1: threading.Thread = threading.Thread(name="One", target=execOS, args=(cmd,))
    thread1.daemon = True  # Dies on exit

    cmd = "sleep 3; echo 'Done - 2'"
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

# keep_looping: bool = True
while nbThreadCompleted < 3:
    print(f"\t...{nbThreadCompleted} thread(s) completed.")
    time.sleep(1)

# print("All threads completed, Hit Return to exit.")
user_input: str = input("All threads completed, Hit Return to exit.")
# keep_looping = False

print("Bye!")
