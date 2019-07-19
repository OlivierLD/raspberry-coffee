#
# Hough Circles
#
import time
import cv2
import numpy as np

Kernel_size = 15
low_threshold = 40
high_threshold = 120

rho = 10
threshold = 15
theta = np.pi / 180
minLineLength = 10
maxLineGap = 1

# Initialize camera
camera = cv2.VideoCapture(0)

width  = 640
height = 480
camera.set(3, width)
camera.set(4, height)


while True:
    # CAPTURE FRAME-BY-FRAME
    ret, frame = camera.read()
    time.sleep(0.1)
    # Convert to Grayscale
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    # Blur image to reduce noise. if Kernel_size is bigger the image will be more blurry
    blurred = cv2.GaussianBlur(gray, (Kernel_size, Kernel_size), 0)

    circles = cv2.HoughCircles(blurred,
                               method=cv2.HOUGH_GRADIENT, dp=1, minDist=200,
                               param1=50, param2=13, minRadius=30, maxRadius=175)
    if circles is not None:
        for i in circles[0, :]:
            cv2.circle(frame, (i[0], i[1]), i[2], (0, 255, 0), 2)
            cv2.circle(frame, (i[0], i[1]), 2, (0, 0, 255), 3)
    else:
        print("No circles...")

    cv2.imshow("circle detect test", frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything is done, release the capture

camera.release()
cv2.destroyAllWindows()
