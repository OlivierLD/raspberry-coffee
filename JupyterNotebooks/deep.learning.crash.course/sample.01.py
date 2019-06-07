import warnings
warnings.filterwarnings('ignore')

#%matplotlib inline
#%pylab inline
# import matplotlib.pyplot as plt

import pandas as pd
print(pd.__version__)

import numpy as np
print(np.__version__)

import seaborn as sns
print(sns.__version__)

# !curl -O https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv

df = pd.read_csv('./insurance-customers-1500.csv', sep=';')
df.head()
df.describe()

# sample_df = df.sample(n=100, random_state=42)
# sns.pairplot(sample_df, hue="group", palette={0: '#AA4444', 1: '#006000', 2: '#EEEE44'})

