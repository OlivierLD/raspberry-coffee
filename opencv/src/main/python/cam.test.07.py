#
# pip install opencv-python
#
import cv2

print('Using OpenCV version', cv2.__version__)
# listing all capture devices
index = 0
cam_arr = []
while True:
    cap = cv2.VideoCapture(index)
    if not cap.read()[0]:
        break
    else:
        cam_arr.append(index)
    cap.release()
    index += 1
print("Camera found:", cam_arr)
