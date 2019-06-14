#!/usr/bin/env python3
#
# Basics. Read data, show content
#
import warnings
import pandas as pd
import numpy as np
import seaborn as sns
import tensorflow as tf

warnings.filterwarnings('ignore')

# %matplotlib inline
# %pylab inline
# import matplotlib.pyplot as plt

print("Panda", pd.__version__)

print("Numpy", np.__version__)

print("Seaborn", sns.__version__)

tf_version = tf.__version__
print("TensorFlow", tf_version)

# !curl -O https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv

df = pd.read_csv('./insurance-customers-1500.csv', sep=';')
df.head()
df.describe()

# sample_df = df.sample(n=100, random_state=42)
# sns.pairplot(sample_df, hue="group", palette={0: '#AA4444', 1: '#006000', 2: '#EEEE44'})
