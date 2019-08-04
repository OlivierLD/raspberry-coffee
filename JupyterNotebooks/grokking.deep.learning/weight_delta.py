#
# As in the notebook with the same name
#
weight = 0.5
goal_pred = 0.8
input = 0.5

for iteration in range(30):
    pred = input * weight
    error = (pred - goal_pred) ** 2
    direction_and_amount = (pred - goal_pred) * input
    weight -= direction_and_amount

    print("Iteration #{} => Error: {}, Prediction: {}".format((iteration + 1), str(error), str(pred)))
