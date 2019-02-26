import math
import time

#
# find the smallest route to join 4 points
#

SQUARE_SIDE_LEN = 1


def distance(delta_x, delta_y):
	return math.sqrt((delta_x ** 2) + (delta_y ** 2))


def distance_to_top_left(x, y):
	return distance(0 - x, 0 - y)


def distance_to_top_right(x, y):
	return distance(SQUARE_SIDE_LEN - x, 0 - y)


def distance_to_bottom_left(x, y):
	return distance(0 - x, SQUARE_SIDE_LEN - y)


def distance_to_bottom_right(x, y):
	return distance(SQUARE_SIDE_LEN - x, SQUARE_SIDE_LEN - y)


def find_best_couple(start_x1, start_y1, start_x2, start_y2, end_x1, end_y1, end_x2, end_y2, step, current_path_len):
	bestX1 = 0
	bestY1 = 0
	bestX2 = 0
	bestY2 = 0

	smallest_path = current_path_len
	x1 = start_x1
	while x1 <= end_x1:
		y1 = start_y1
		while y1 <= end_y1:
			x2 = start_x2
			while x2 <= end_x2:
				y2 = start_y2
				while y2 < end_y2:
					path = distance_to_top_left(x1, y1) + \
					       distance_to_top_right(x2, y2) + \
					       distance((x1 - x2), (y1 - y2)) + \
					       distance_to_bottom_left(x1, y1) + \
					       distance_to_bottom_right(x2, y2)
					if path < smallest_path:
						smallest_path = path
						bestX1 = x1
						bestY1 = y1
						bestX2 = x2
						bestY2 = y2
					y2 += step
				x2 += step
			y1 += step
		x1 += step

	return (smallest_path, bestX1, bestY1, bestX2, bestY2)


STEP_01 = 0.1
min_path_len = float("inf")
start_x1 = 0
start_y1 = 0
start_x2 = 0
start_y2 = 0
end_x1 = SQUARE_SIDE_LEN
end_y1 = SQUARE_SIDE_LEN
end_x2 = SQUARE_SIDE_LEN
end_y2 = SQUARE_SIDE_LEN

before = time.clock()
(smallest_path, bestX1, bestY1, bestX2, bestY2) = find_best_couple(start_x1, start_y1, start_x2, start_y2, end_x1, end_y1, end_x2, end_y2, STEP_01, min_path_len)
after = time.clock()
print("Pass 1 (precision 0.1) >\nFinal result: smallest path %f, P1(%.2f, %.2f), P2(%.2f, %.2f)" % (smallest_path, bestX1, bestY1, bestX2, bestY2))
print("Completed in %.3f ms\n" % (1000 * (after - before)))

STEP_02 = 0.01
min_path_len = smallest_path
start_x1 = bestX1 - STEP_01
start_y1 = bestY1 - STEP_01
start_x2 = bestX2 - STEP_01
start_y2 = bestY2 - STEP_01
end_x1 = bestX1 + STEP_01
end_y1 = bestY1 + STEP_01
end_x2 = bestX2 + STEP_01
end_y2 = bestY2 + STEP_01

before = time.clock()
(smallest_path, bestX1, bestY1, bestX2, bestY2) = find_best_couple(start_x1, start_y1, start_x2, start_y2, end_x1, end_y1, end_x2, end_y2, STEP_02, min_path_len)
after = time.clock()
print("Pass 2 (precision 0.01) >\nFinal result: smallest path %f, P1(%.3f, %.3f), P2(%.3f, %.3f)" % (smallest_path, bestX1, bestY1, bestX2, bestY2))
print("Completed in %.3f ms\n" % (1000 * (after - before)))

STEP_03 = 0.001
min_path_len = smallest_path
start_x1 = bestX1 - STEP_02
start_y1 = bestY1 - STEP_02
start_x2 = bestX2 - STEP_02
start_y2 = bestY2 - STEP_02
end_x1 = bestX1 + STEP_02
end_y1 = bestY1 + STEP_02
end_x2 = bestX2 + STEP_02
end_y2 = bestY2 + STEP_02

before = time.clock()
(smallest_path, bestX1, bestY1, bestX2, bestY2) = find_best_couple(start_x1, start_y1, start_x2, start_y2, end_x1, end_y1, end_x2, end_y2, STEP_03, min_path_len)
after = time.clock()
print("Pass 3 (precision 0.001) >\nFinal result: smallest path %f, P1(%.4f, %.4f), P2(%.4f, %.4f)" % (smallest_path, bestX1, bestY1, bestX2, bestY2))
print("Completed in %.3f ms\n" % (1000 * (after - before)))
