#
# pip install opencv-python
#
import cv2

print('Using OpenCV version', cv2.__version__)
cam = cv2.VideoCapture(0)
ret, image = cam.read()
if ret:
    cv2.imshow('Snap', image)
    print("Hit return on the image to close it")
    cv2.waitKey(0)
    cv2.destroyWindow('Snap')
else:
    print('Oops!')
cam.release()
