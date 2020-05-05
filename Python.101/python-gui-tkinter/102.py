import tkinter as tk

window = tk.Tk()
greeting = tk.Label(
    text="Hello, Tkinter",
    fg="white",
    bg="black",
    width=20,
    height=10
)
greeting.pack()

# The following is mandatory if the code is executed from a script.
# Not necessary from the REPL.
window.mainloop()


