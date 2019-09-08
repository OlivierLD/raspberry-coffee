from numpy import load

archive_name = "/Users/olediour/.keras/datasets/boston_housing.npz"
data = load(archive_name)
lst = data.files
for item in lst:
    print(item)
    print(data[item])
