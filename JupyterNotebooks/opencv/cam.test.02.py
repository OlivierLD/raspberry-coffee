import cv2

cam = cv2.VideoCapture(0)
print('Default resolution is ' + str(int(cam.get(3))) + 'x' + str(int(cam.get(4))))

w = 1024
h = 768
cam.set(3, w)
cam.set(4, h)
print('Resolution is now ' + str(int(cam.get(3))) + 'x' + str(int(cam.get(4))))

while True:
    # Capture frames one by one
    ret, frame = cam.read()
    # Display
    cv2.imshow('Video Test', frame)
    # Wait for escape key to exit
    if cv2.waitKey(1) == 27:
        break  # Exit loop

print('Done!')
cam.release()
cv2.destroyAllWindows()
