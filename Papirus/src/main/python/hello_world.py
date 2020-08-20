from papirus import PapirusText

text = PapirusText([rotation = rot])

# Write text to the screen
# text.write(text)
text.write("hello world")

# Write text to the screen specifying all options
#      text.write(text, [size = <size> ],[fontPath = <fontpath>],[maxLines = <n>])
# maxLines is the max number of lines to autowrap the given text.
# New lines ('\n') in the text will not go to the next line, but are interpreted as white space.
# Use PapirusTextPos() instead which recognizes '\n'.
