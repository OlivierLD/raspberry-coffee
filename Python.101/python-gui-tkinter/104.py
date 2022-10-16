import tkinter as tk
import time
import threading
import traceback


window = tk.Tk()
greeting = tk.Label(
    text="Hello, Tkinter",
    fg="white",
    bg="black",
    width=30,
    height=10
)
greeting.pack()

#
# Now try to update the field value, in a loop.
#
def updateBusiness() -> None:
    for i in range(10):
        print(f"Loop #{i}")
        greeting["text"] = f"Iteration #{i}..."
        time.sleep(2)

try:
    updater: threading.Thread = threading.Thread(name="Updater", target=updateBusiness, args=())
    updater.daemon = True  # Dies on exit
    updater.start()
except Exception as ex:
    print("Exception!")
    traceback.print_exc(file=sys.stdout)


# The following is mandatory if the code is executed from (or as) a script.
# Not necessary from the REPL.
window.mainloop()

