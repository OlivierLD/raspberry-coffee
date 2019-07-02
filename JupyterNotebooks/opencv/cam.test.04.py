#
# pip install opencv-python
#
import cv2

print('Using OpenCV version', cv2.__version__)
cam = cv2.VideoCapture(0)
w = 512
h = 384
cam.set(3, w)
cam.set(4, h)

while True:
    ret, image = cam.read()
    if ret:
        cv2.imshow('Original', image)
        # cv2.imwrite('./snap1.jpg', image)

        img_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        cv2.imshow('Gray', img_gray)

        ret, thresh = cv2.threshold(img_gray, 127, 255, 0)
        cv2.imshow('Thresh', thresh)
        try:
            # Only 2 prms returned!!!
            contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            print("Contours were found!! list of {} elements (ndarrays)".format(len(contours)))
            for i in range(len(contours)):
                nb_points = len(contours[i])
                if nb_points > 50:
                    print("Contour {} has {} points".format(i, nb_points))

            cv2.drawContours(image, contours, -1, (0, 255, 0), 3)
            cv2.imshow('Contours', image)
        except ValueError as ve:
            # keep going
            if False:
                print(ve)
    else:
        print('Oops!')
    key = cv2.waitKey(1) & 0xFF
    if key != 255:   # ord('q'):
        print("Key {}".format(key))
        break

cv2.destroyAllWindows()
cam.release()
