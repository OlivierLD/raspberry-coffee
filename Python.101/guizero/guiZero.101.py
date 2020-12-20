#
# Basic Sample
#
from guizero import App, Text, Picture
from guizero import system_config

app = App(title="Hello World")
app.bg = (251, 251, 208)
message = Text(app, text="Deal with\nconfinement")
message.text_size = 50
message.font = "Courier"

print(system_config.supported_image_types)

maze = Picture(app, image='./confinement.jpg')
(width, height) = (maze.width // 6, maze.height // 6)
maze = maze.resize(width=width, height=height)

app.display()
