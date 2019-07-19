#
# Color detection
#
import numpy as np
import argparse
import cv2

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--image", help="path to the image")
args = vars(ap.parse_args())

# load the image
# image = cv2.imread(args["image"])
image = cv2.imread('./snap.jpg')

# define the list of boundaries.
# Warning: those are BGR, not RGB
# Good color picker at https://www.w3schools.com/colors/colors_picker.asp
boundaries = [
    ([17, 15, 100], [50, 56, 200]),     # red
    ([86, 31, 4], [220, 88, 50]),       # blue
    ([25, 146, 190], [62, 174, 250]),   # yellow
    ([0, 153, 255], [0, 255, 255]),     # yellow, bright
    ([103, 86, 65], [145, 133, 128]),   # gray
    ([0, 0, 0], [65, 65, 65])           # black (dark)
]

white = (255, 255, 255)
# loop over the boundaries
for (lower, upper) in boundaries:

    # print("before", lower, upper)

    # create NumPy arrays from the boundaries
    lower = np.array(lower, dtype="uint8")
    upper = np.array(upper, dtype="uint8")

    # print("after", lower, upper)

    # find the colors within the specified boundaries and apply
    # the mask
    mask = cv2.inRange(image, lower, upper)
    output = cv2.bitwise_and(image, image, mask=mask)
    from_color = (int(lower[0]), int(lower[1]), int(lower[2]))
    to_color = (int(upper[0]), int(upper[1]), int(upper[2]))

    # print(from_color, to_color)

    cv2.circle(output, (15, 15), 12, white, -1)  # negative thickness: filled
    cv2.circle(output, (15, 15), 10, from_color, -1)  # negative thickness: filled
    cv2.putText(output, '<- between this {}'.format(str(from_color)), (30, 25), cv2.FONT_HERSHEY_SIMPLEX, 1, white, 2)
    cv2.circle(output, (15, 40), 12, white, -1)  # negative thickness: filled
    cv2.circle(output, (15, 40), 10, to_color, -1)  # negative thickness: filled
    cv2.putText(output, '<- and that {}'.format(str(to_color)), (30, 50), cv2.FONT_HERSHEY_SIMPLEX, 1, white, 2)

    # show the images
    cv2.imshow("images", np.hstack([image, output]))
    cv2.waitKey(0)
