#
# Easy!
#
import sys

nb_rec = 0


def fib(n):
    # global nb_rec
    # nb_rec += 1
    if n <= 1:
        return n
    return fib(n - 1) + fib(n - 2)


def main(args):
    suite = []
    length = 30
    if len(args) == 2:
        length = int(args[1])
    for i in range(length):
        # nb_rec = 0
        x = fib(i)
        suite.append(x)
        # print(f"Fib({i}) = { x } ({nb_rec} recursions)")

    print(f"{length} elements: {suite}")


if __name__ == '__main__':
    main(sys.argv)
