#!/usr/bin/env python3
#
# Basic sanity check.
# Valids imports
# Read data, show content
#
import warnings
import pandas as pd
import numpy as np
import seaborn as sns
import tensorflow as tf
import os.path
import subprocess as sp
import sys

warnings.filterwarnings('ignore')

print("Panda", pd.__version__)

print("Numpy", np.__version__)

print("Seaborn", sns.__version__)

tf_version = tf.__version__
print("TensorFlow", tf_version)

found_data = False;
if os.path.isfile('./insurance-customers-1500.csv'):
    found_data = True

if not found_data:
    print("Data file insurance-customers-1500.csv is not here")
    userInput = input("Do you want to download it now ? Y/n > ")
    if userInput == '' or userInput == 'y' or userInput == 'Y':
        print("Downloading...")
        sp.run(["curl", "-O",
                "https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv"])
    else:
        print("Ok, exiting.")
        sys.exit()

try:
    print("Reading data frame...")
    df = pd.read_csv('./insurance-customers-1500.csv', sep=';')
    print("-- head --")
    print(df.head())
    print("\n-- description --")
    print(df.describe())
except FileNotFoundError:  # Should not happen, but who knows!
    print("Data file not found, have you run the curl command?")
    print(
        "Like 'curl -O https://raw.githubusercontent.com/DJCordhose/deep-learning-crash-course-notebooks/master/data/insurance-customers-1500.csv'")

print("\nBye")
