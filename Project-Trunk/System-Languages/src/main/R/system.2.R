## Other method, similar to system.R
# See https://www.geeksforgeeks.org/create-matrix-from-vectors-in-r/?ref=rp
# Create one matrix instead of 3 lines.
#
mat_data <- c(12,       13,    14,
               1.345, -654,     0.001,
              23.09,     5.3, -12.34)
rowNames <- c("Row1", "Row2", "Row3")
colNames <- c("Col1", "Col2", "Col3")
matrix <- matrix(mat_data,
                 nrow=3,
                 byrow=TRUE,
                 dimnames = list(rowNames, colNames))
print(matrix)
class(matrix)

constants <- c(234, 98.87, 9.876)

result <- solve(matrix, constants)

# print the solution.
print(result)

# Proof
coeffA <- result["Col1"]
coeffB <- result["Col2"]
coeffC <- result["Col3"]
#
x <- (coeffA * mat_data[1]) + (coeffB * mat_data[2]) + (coeffC * mat_data[3])
y <- (coeffA * mat_data[4]) + (coeffB * mat_data[5]) + (coeffC * mat_data[6])
z <- (coeffA * mat_data[7]) + (coeffB * mat_data[8]) + (coeffC * mat_data[9])
#
print("Proof: X, Y, Z")
print(x)
print(y)
print(z)
