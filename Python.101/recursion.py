# Factorial. Recursive algorithm

def factorial(n: int) -> int:
	if n == 0:
		return 1
	else:
		return n * factorial(n - 1)


f6 = factorial(6)
print('!6 = ', f6)
