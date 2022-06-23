# PGCD in Python (python 3)

def pgcd(n1: int, n2: int) -> int:
    while n1 != n2 :
        if n1 > n2:
            n1 -= n2
        else:
            n2 -= n1
    return n2

n1: int = 60
n2: int = 36
print(f"PGCD({n1}, {n2}) = {pgcd(n1, n2)}")
