# System resolution, in R

# Create 3 different vectors (lines)
# using combine method.
l1 <- c(12, 13, 14)
l2 <- c(1.345, -654, 0.001)
l3 <- c(23.09, 5.3, -12.34)
#
constants <- c(234, 98.87, 9.876)

# bind the three vectors into a matrix
# using rbind() which is basically
# row-wise binding.
matrix <- rbind(l1, l2, l3)

# print the original matrix
print(matrix)

# Use the solve() function
result <- solve(matrix, constants)

# print the solution.
print(result)
