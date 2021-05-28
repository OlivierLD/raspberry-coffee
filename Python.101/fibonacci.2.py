#
# Other method, more efficient than fibonacci.py
#
import sys
from typing import List

# shift registers
a = 0
b = 0


# Calculate Nth element of the suite
def fib(n: int) -> int:
    global a
    global b
    if n == 0:
        a = 0
        return n
    elif n == 1:
        b = 1
        return n
    else:
        num = a + b
        # print(f"Iteration {n}, a={a}, b={b}, val={num}")
        a = b
        b = num
        return num


# All the suite, up to n
def fibonacci(n: int) -> List[int]:
    suite = []
    for i in range(n):
        x = fib(i)
        suite.append(x)
        # print(f"{i} => {x}")
    return suite


# Main part
def main(args: List[str]) -> None:
    length = 30
    if len(args) == 2:
        length = int(args[1])
    print(f"{length} elements: {fibonacci(length)}")


if __name__ == '__main__':
    main(sys.argv)
