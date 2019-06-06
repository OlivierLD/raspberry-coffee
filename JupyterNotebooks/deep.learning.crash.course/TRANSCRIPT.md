# Deep Learning Crash Course
A transcript of the Manning Video `Deep Learning Crash Course` by Oliver Zeigermann.

First, some links:
- [Google Colab](https://colab.research.google.com/notebooks/welcome.ipynb)
- [TensorFlow](https://www.tensorflow.org/)
- [TensorFlow.js](https://www.tensorflow.org/js) 

### Deep Learning and AI
![Where](./img/01fig01.jpg)

And even AI (Artificial Intelligence) itself is prt of the even bigger picture called Data Science.

![Data Science](./img/DataScience.png)

(KDD: Knowledge Discovery in Databases)

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
See in this [Java Notebook](./NeuralNetworks.ipynb#A-"Classical"-way-to-program-a-rule), there is a Java implementation of such a rule.


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

How a Neuron (aka Node) works.

- It has a _single_ numerical output (usually called `y`).
- It can have several inputs (also called dimensions), usually named `x`<small><sub>`n`</sub></small>.
- It also has a constant, called the bias (noted `b`)
- Its formula is

 ![Neuron](./img/neuron.formula.png)
 
Example 

 ![Neuron](./img/neuron.example.png)

See a [Java Notebook](./NeuralNetworks.ipynb#Neurons!) for an example.

In a classification problem, a single neuron can draw a single line as a decision boundary. 

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

See this [Jupyter Java Notebook](./NeuralNetworks.ipynb) for more details.

#### Neuron Networks
One neuron is usually not enough. But a combination of them can approximate almost _any_ function.

Samples are not typically separable by a single line..., like this one for example:

![Sample 2](./img/sample.02.png)

As each neuron will generate one line (boundary, separation, whatever you call it), we will add more 
neuron to the picture to see how it goes.

We add a neuron in the first _hidden layer_, and we combine these two neurons into a so-called _fully connected layer_.

There is _no_ communication between those neurons, and they get the same input from the dimensions (age and speed here).
_**But**_ the inputs can have different weight (thickness of the line).

> Note: we have here _one_ hidden layer, made out of _two_neurons.
> In a typical setting, you might have 2 or 3 hidden layers, maybe 500 to 5000 neurons per hidden layer.
> In which case it makes it prohibitive to tune all those parameters by hand.

Kick this off by clicking the \[Start\] button again. You see the parameters of the neurons changing,
and the combined output on the right. As you would see, this network might not be able
to separate those two groups we have in our training dataset.

![Sample 3](./img/sample.03.png)

#### How does a network _learn_?
The first thing is to know how well the network works.
In the picture above, it does _not_ work great.
Again, the prediction is reflected by the color of the background, and some points are on the wrong background.

To know how well the network is working, we will consider - for each point - the difference between 
the prediction (reflected by the background color), and the reality (reflected by the point color).
We combine the value of the error for _all_ the data point in our training dataset, which we do using the 
mean squared error (MSE).

![MSE](./img/MSE.png)

- In the formula above, Y<small><sub>i</sub></small> is the prediction, and Ÿ<small><sub>i</sub></small> is the value from the training dataset.
- We compute the difference, and we square it to get rid of its sign, so positive and negative values do not erase each other
- We sum all the differences for all the data set
- We divide by the cardinality of the dataset (normalization)

To understand better, we restrict the picture to only one neuron again, to deal with only one parameter (of the neuron),
that would be the bias.

Then we can plot the value of this bias (abscissa) to the loss (ordinate).

![Loss vs Bias](./img/loss.vs.bias.png)

This loss is displayed on the diagrams of the TensorFlow playground, on the top left part (Test loss),
the curve(s) represent the value of the loss as the training goes. If the loss drops, this is good.

This is the goal of the training algorithm, it changes the bias to minimize the loss.

To store the best bias, we check the slope of the curve. If it goes down, it is good.
The next point - on the loss curve - to evaluate will be one step away, this step value is the `learning rate` that
figures in the TensorFlow diagram.

A big `learning rate` will accelerate the learning process, but might very well miss local minima if it is too big.

![Learning rate](./img/sample.04.png)

This strategy is called _Gradient Descent_.

The `epoch` represents the number of steps it went through so far. For each step, the algorithm is evaluating the error for all the points of the training dataset.
You can see the loss curve(s) as the process goes on, and the decision boundaries are updated in real time as well.

See this [complex one](http://playground.tensorflow.org/#activation=tanh&batchSize=10&dataset=spiral&regDataset=reg-plane&learningRate=0.03&regularizationRate=0&noise=40&networkShape=4,2&seed=0.31018&showTestData=true&discretize=false&percTrainData=50&x=true&y=true&xTimesY=false&xSquared=false&ySquared=false&cosX=false&sinX=false&cosY=false&sinY=false&collectStats=false&problem=classification&initZero=false&hideText=false&problem_hide=true&regularization_hide=true&batchSize_hide=true&regularizationRate_hide=true).

#### Finding the sweet spot

Over fitting vs Under fitting.

The training of a Neural Network relies on know data.

Usually, 80% of those known data are used to train the network (training dataset), a the
remaining 20% are used to test if the prediction of the network is correct (test dataset).

## Training a TensorFlow model
From existing customer data, we will use `Keras` API to train a neural network.

The code will be provided in `Colab` Notebooks.
All the required code can be found at <https://github.com/DJCordhose/deep-learning-crash-course-notebooks/blob/master/README.md>.
`Colab` requires a Google account, and works better in a Chrome browser.

> Note: Shift + Enter executes the current cell, and brings you to the next one.

 
