import tkinter as tk

window = tk.Tk()
greeting = tk.Label(text="Hello, Tkinter")
greeting.pack()

# The following is mandatory if the code is executed from a script.
# Not necessary from the REPL.
window.mainloop()


