import tkinter as tk
import time

window = tk.Tk()
greeting = tk.Label(
    text="Hello, Tkinter",
    fg="white",
    bg="black",
    width=30,
    height=10
)
greeting.pack()

# The following is mandatory if the code is executed from (or as) a script.
# Not necessary from the REPL.
window.mainloop()

