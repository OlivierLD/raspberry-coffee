#
# pip install opencv-python
#
import cv2

print('Using OpenCV version', cv2.__version__)
# listing all capture devices
index = 0
cam_arr = []
while True:
    print("Trying camera #{}".format(index))
    try:
        cap = cv2.VideoCapture(index)
    except Exception as ex:  # No Exception fired even if index does not exist.
        print("Error: {}".format(ex))
    print('Trying to read #{}'.format(index))
    if not cap.read()[0]:
        print("\t>> No #{}".format(index))
        break
    else:
        print("\t>> Found cam #{}".format(index))
        cam_arr.append(index)
    cap.release()
    index += 1
print("Camera(s) found:", cam_arr)
