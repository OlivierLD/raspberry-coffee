#
# Hough Lines (not probabilistic)
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

    # Perform canny edge-detection.
    # If a pixel gradient is higher than high_threshold is considered as an edge.
    # if a pixel gradient is lower than low_threshold is is rejected , it is not an edge.
    # Bigger high_threshold values will provoque to find less edges.
    # Canny recommended ratio upper:lower  between 2:1 or 3:1
    edged = cv2.Canny(blurred, low_threshold, high_threshold)

    lines = cv2.HoughLines(edged, 1, np.pi / 180, 200)
    if lines is not None:
        for rho, theta in lines[0]:
            a = np.cos(theta)
            b = np.sin(theta)
            x0 = a * rho
            y0 = b * rho
            pts1 = (int(x0 + 1000 * (-b)), int(y0 + 1000 * (a)))
            pts2 = (int(x0 - 1000 * (-b)), int(y0 - 1000 * (a)))
            cv2.line(frame, pts1, pts2, (0, 0, 255), 2)
    else:
        print("No lines...")

    cv2.imshow("line detect test", frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# When everything is done, release the capture

camera.release()
cv2.destroyAllWindows()
