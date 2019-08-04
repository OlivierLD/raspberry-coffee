#
# Adapted from https://github.com/abidrahmank/OpenCV2-Python-Tutorials/blob/master/source/py_tutorials/py_ml/py_knn/py_knn_opencv/py_knn_opencv.rst
# OCR, letters
#
# Using OpenCV ML capabilities.
#
import cv2
import numpy as np
# import matplotlib.pyplot as plt

# Load the data, converters convert the letter to a number
data = np.loadtxt('letter-recognition.data', dtype='float32', delimiter=',',
                  converters={0: lambda ch: ord(ch) - ord('A')})

# split the data to two, 10000 each for train and test
train, test = np.vsplit(data, 2)

# split trainData and testData to features and responses
responses, trainData = np.hsplit(train, [1])
labels, testData = np.hsplit(test, [1])

# Initiate the kNN, classify, measure accuracy.
knn = cv2.ml.KNearest_create()  # cv2.KNearest()
knn.train(train, cv2.ml.ROW_SAMPLE, responses)
ret, result, neighbours, dist = knn.findNearest(test, k=5)

correct = np.count_nonzero(result == labels)
accuracy = correct * 100.0 / 10000
print("Accuracy {}%".format(accuracy))
