import math
pas=0.01
x=0
xmin=1
ymin=1
xamin=1
yamin=1
trajetmin=5
while x<=1:
	y=0
	while y<=1:
		xa=0
		while xa<=1:
			ya=0
			while ya<1:
				trajet=math.sqrt(x**2+y**2)+math.sqrt(x**2+(y-1)**2)+math.sqrt((x-xa)**2+(y-ya)**2)+math.sqrt((xa-1)**2+ya**2)+math.sqrt((xa-1)**2+(ya-1)**2)
				if trajet<trajetmin:
					trajetmin=trajet
					xmin=x
					ymin=y
					xamin=xa
					yamin=ya
				ya=ya+pas
			xa=xa+pas
		y=y+pas
	x=x+pas
print (trajetmin,xmin,ymin,xamin,yamin)

#
# Original output:
# Path: 2.732055362658948
# P1: 0.2900000000000001 0.5000000000000002
# P2: 0.7100000000000004 0.5000000000000002
#
