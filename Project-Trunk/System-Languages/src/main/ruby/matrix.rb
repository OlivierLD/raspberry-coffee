#
# Interactive (REPL) Ruby:
# $ irb
# or
# $ irb matrix.rb
#
# Just run:
# $ ruby matrix.rb
#
matrix = [
    [12, 13, 14],
    [1.345, -654, 0.001],
    [23.09, 5.3, -12.34]
]

coeffs = [234, 98.87, 9.876]

DEBUG = false

def print_matrix(matrix)
  dim = matrix.length
  for row in 0..(dim - 1)
    line = "|"
    for col in 0..(dim - 1)
      line += "#{matrix[row][col]}\t"
    end
    line += "|"
    puts line
  end
end

def minor(matrix, row, col)
  dim = matrix.length
  # init minor
  min = Array.new(dim - 1)
  for x in 0..(dim - 2)
    min[x] = Array.new(dim - 1, 0)
  end
  # fill minor
  for c in 0..(dim - 1)
    if c != col then
      for r in 0..(dim - 1)
        if r != row then
          minR = r < row ? r : r - 1
          minC = c < col ? c : c - 1
          min[minR][minC] = matrix[r][c]
        end
      end
    end
  end
  # Done
  return min
end

def determinant(matrix)
  v = 0.0
  if matrix.length == 1 then
    v = matrix[0][0]
  else
    dim = matrix.length
    for c in 0..(dim - 1) # walk thru first line
      minDet = determinant(minor(matrix, 0, c))
      v += (matrix[0][c] * minDet * ((-1.0) ** (c + 1 + 1)))
    end
  end
  if DEBUG
    puts "Determinant of"
    print_matrix(matrix)
    puts "is #{v}"
  end
  return v
end

def transposed(matrix)
  dim = matrix.length
  # init new matrix
  trans = Array.new(dim)
  for x in 0..(dim - 1)
    trans[x] = Array.new(dim, 0)
  end
  # fill it
  for r in 0..(dim - 1)
    for c in 0..(dim - 1)
      trans[r][c] = matrix[c][r]
    end
  end
  return trans
end

def comatrix(matrix)
  dim = matrix.length
  # init new matrix
  co = Array.new(dim)
  for x in 0..(dim - 1)
    co[x] = Array.new(dim, 0)
  end
  # fill it
  for r in 0..(dim - 1)
    for c in 0..(dim - 1)
      co[r][c] = determinant(minor(matrix, r, c)) * ((-1) ** (r + c + 2))
    end
  end
  return co
end

def multiply(matrix, n)
  dim = matrix.length
  # init new matrix
  res = Array.new(dim)
  for x in 0..(dim - 1)
    res[x] = Array.new(dim, 0)
  end
  # fill it
  for r in 0..(dim - 1)
    for c in 0..(dim - 1)
      res[r][c] = matrix[r][c] * n
    end
  end
  return res
end

def invert(matrix)
  return multiply(transposed(comatrix(matrix)), (1 / determinant(matrix)))
end

def solve_system(matrix, coeffs)
  dim = matrix.length
  result = Array.new(dim, 0)
  inverted = invert(matrix)
  # line * column
  for row in 0..(dim - 1)
    result[row] = 0
    for col in 0..(dim - 1)
      result[row] += inverted[row][col] * coeffs[col]
    end
  end
  return result
end

print_matrix(matrix)

solution = solve_system(matrix, coeffs)

puts "A is #{solution[0]}"
puts "B is #{solution[1]}"
puts "C is #{solution[2]}"
