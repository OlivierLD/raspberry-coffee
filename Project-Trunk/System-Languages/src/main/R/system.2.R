## Other method, similar
# See https://www.geeksforgeeks.org/create-matrix-from-vectors-in-r/?ref=rp
#
mat_data <- c(12, 13, 14,
              1.345, -654, 0.001,
              23.09, 5.3, -12.34)
rowNames <- c("Row1", "Row2", "Row3")
colNames <- c("Col1", "Col2", "Col3")
matrix <- matrix(mat_data,
                 nrow=3,
                 byrow=TRUE,
                 dimnames = list(rowNames, colNames))
print(matrix)
class(matrix)
result <- solve(matrix, constants)

# print the solution.
print(result)
