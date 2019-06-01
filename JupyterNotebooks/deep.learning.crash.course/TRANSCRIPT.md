# Deep Learning Crash Course
A transcript of the Manning Video `Deep Learning Crash Course` by Oliver Zeigermann.

First, some links:
- [Google Colab](https://colab.research.google.com/notebooks/welcome.ipynb)
- [TensorFlow](https://www.tensorflow.org/)
- [TensorFlow.js](https://www.tensorflow.org/js) 

### Deep Learning and AI
![Where](./img/01fig01.jpg)

### How it is different from Programming
![Where](./img/01fig02.jpg)

### We start from real data (from a database)
![Real data](./img/real.data.png)

- red: high risk
- yellow: medium risk
- green: low risk

We actually have 3 dimensions:
- Max Speed
- Age
- Risk group, encoded with the color.

A programmer would write rules like this (in Python):
```python
if age < 25:
  if speed > 140:
    return red # Crazy young guy, car too fast
  else:
    return yellow # Car is slow enough for medium risk
    
if age > 75
  return red # Get off the road, old man!
  
if miles_per_year > 30:
  return red # You drive too much
    
if miles_per_year > 20:
  return yellow 
  
return green # otherwise, low risk  
      
```

If we plot this on the diagram (red-yellow-green), we have
![Plot](./img/rules.plot.png)

### Deep - supervised - machine learning
