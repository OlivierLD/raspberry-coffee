import cv2

print('Using OpenCV version', cv2.__version__)
cam = cv2.VideoCapture(0)

w = 800
h = 600
cam.set(3, w)
cam.set(4, h)

ret, image = cam.read()
if ret:
    cv2.imshow('Snap', image)
    cv2.imwrite('./snap.jpg', image)
    print('snap.jpg was written on disk')
else:
    print('Oops!')
cam.release()

img = cv2.imread('snap.jpg')
if img is not None:
    print('Image read OK')
    print('Shape', img.shape)
    print('Size', img.size)
    print('Type', img.dtype)
else:
    print('snap.jpg was not found')
