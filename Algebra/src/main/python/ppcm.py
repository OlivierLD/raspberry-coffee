# PPCM for Python3

def ppcm(n1: int, n2: int) -> int:
    product: int = n1 * n2
    remainder: int = n1 % n2
    while remainder != 0:
        n1 = n2
        n2 = remainder
        remainder = n1 % n2
    return int(product / n2)

n1: int = 60
n2: int = 36
print(f"PPCM({n1}, {n2}) = {ppcm(n1, n2)}")
n1 = 355
n2 = 113
print(f"PPCM({n1}, {n2}) = {ppcm(n1, n2)}")
