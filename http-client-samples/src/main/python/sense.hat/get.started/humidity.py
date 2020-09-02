from sense_hat import SenseHat
sense = SenseHat()

green = (0, 255, 0)
white = (255, 255, 255)

keep_looping = True

while keep_looping:
    try:
        hum = sense.humidity
        hum_value = 64 * hum / 100
        pixels = [green if i < hum_value else white for i in range(64)]
        sense.set_pixels(pixels)
    except KeyboardInterrupt:
        keep_looping = False
        sense.show_message("Bye!")

print("Bye!")
