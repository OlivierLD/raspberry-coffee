# Deep Learning Crash Course
A transcript of the Manning Video `Deep Learning Crash Course` by Oliver Zeigermann.

First, some links:
- [Google Colab](https://colab.research.google.com/notebooks/welcome.ipynb)
- [TensorFlow](https://www.tensorflow.org/)
- [TensorFlow.js](https://www.tensorflow.org/js) 

### Deep Learning and AI
![Where](./img/01fig01.jpg)

### How it is different from (Classical) Programming
![Where](./img/01fig02.jpg)

Classical programming applies rules on data to get answers.

The goal of machine learning is to produce the rules, by finding the path between the original data and the answers/facts/conclusions produced subsequently.

### We start from real data (from a database)
![Real data](./img/real.data.png)

- red: high risk
- yellow: medium risk
- green: low risk

We actually have 3 dimensions:
- Max Speed of the car
- Age of the driver
- Risk group, encoded with the color.

Let's introduce another dimension, the number of thousands of miles driven per year.
A programmer would write _rules_ like this (in Python):
```python
if age < 25:
  if speed > 140:
    return red # Crazy young guy, car too fast
  else:
    return yellow # Car is slow enough for medium risk
    
if age > 75:
  return red # Get off the road, old man!
  
if miles_per_year > 30:
  return red # You drive too much
    
if miles_per_year > 20:
  return yellow 
  
return green # otherwise, low risk  
      
```

If we plot this on the diagram (red-yellow-green), we have:

![Plot](./img/rules.plot.png)

### Deep - supervised - machine learning

The `model` will replace the `rules` we had before.

It will be able to make predictions like this:

![Model](./img/Model.png)

We will use Neural Networks to elaborate the model.

And specifically the `TensorFlow playground`.

<!-- 2. BASIC CONCEPTS OF DEEP SUPERVISED MACHINE LEARNING -->

### TensorFlow Playground
TensorFlow playground at <https://playground.tensorflow.org>

How a Neuron works.

- It has a _single_ numerical output (usually called `y`).
- It can have several inputs (also called dimensions), usually named `x`<small><sub>`n`</sub></small>.
- It also has a constant, called the bias (noted `b`)
- Its formula is

 ![Neuron](./img/neuron.formula.png)
 
Example 

 ![Neuron](./img/neuron.example.png)

See a [Java Notebook](./Neural%20Networks.ipynb) for an example.

#### Neurons at work

![TensorFlow Playground](./img/tf.playground.png)

In the screenshot above:
- We have 2 input dimensions, x<small><sub>1</sub></small> (abscissa) and x<small><sub>2</sub></small> (ordinate).
- The weight of each dimension is represented by the thickness of the line between the dimension and the neuron(s)
- The bias of the neuron is represented by the (very) small little gray square at the bottom left of the neuron(s).
- The output will be a single number, ranging from -1 to +1.
- The background of the graphic on the right will reflect the prediction, it will be orange if y < 0, and blue if y > 0.
- You can change the weights associated with every boxes (dimensions and neuron(s)), as well as the bias of each neuron.
 
<!--
 Use ReLU Activation
 Change dimension weights to 1
 Change neuron's bias to 0
 --> 
When you hit the \[Start\] <!-- &#10162; --> button, we iterate over all those parameters.
The tuning step of those parameters **_is_** what Machine Learning is all about.

##### What we want
We want to obtain the parameters (weights, bias) that will generate the right background for the points of our training dataset.

We know what data we start from (training data), we are trying to minimize the number 
of misclassified points (an orange dot on a blue background and vice-versa). 

In this example, the classification can be operated by a single neuron.

#### Activation functions
The Activation Function sits between the calculated output, and the actual output of the neuron (`y`).
It compresses the calculated output between 2 other values (like 0 and 1, -1 and +1, etc).
Notice that the output `y` becomes the `x` of the Activation Function.

Among them, we have:
- Step functions (0 or 1),
- Sigmoids (\[0, 1\])
- Hyperbolic Tangents (\[-1, +1\])
- Rectified Linear Unit (aka ReLU), (\[0, n\])

For example, if we use a Sigmoid for the value used above (`17`), we would probably return a value around `0.91`...

| Sigmoid  | Hyperbolic Tangent | ReLU     |
|:--------:|:------------------:|:--------:|
| ![Sigmoid](./img/Sigmoid.png) | ![H Tan](./img/HTan.jpg) | ![Sigmoid](./img/ReLU.png) |

See this [Jupyter Java Notebook](./NeuralNetworks.ipynb)
