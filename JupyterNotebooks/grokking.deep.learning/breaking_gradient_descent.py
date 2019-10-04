
weight = 0.5
goal_pred = 0.8
input = 0.5

print("---- Input = {} -----".format(input))
for iteration in range(20):
    pred = input * weight
    error = (pred - goal_pred) ** 2
    delta = pred - goal_pred
    weight_delta = input * delta
    weight -= weight_delta
    print("Error: {}, Prediction: {}".format(error, pred))

weight = 0.5
input = 2
print("---- Input = {} -----".format(input))
for iteration in range(20):
    pred = input * weight
    error = (pred - goal_pred) ** 2
    delta = pred - goal_pred
    weight_delta = input * delta
    weight -= weight_delta
    print("Error: {}, Prediction: {}".format(error, pred))

input = 2
weight = 0.5
alpha = 0.1
print("---- Input = {}, alpha {} -----".format(input, alpha))
for iteration in range(20):
    pred = input * weight
    error = (pred - goal_pred) ** 2
    derivative = input * (pred - goal_pred)
    weight -= (alpha * derivative)
    print("Error: {}, Prediction: {}".format(error, pred))