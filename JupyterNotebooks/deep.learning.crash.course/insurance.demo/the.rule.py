red = 0
green = 1
yellow = 2

def evaluate_risk(age, speed, miles_per_year):
  if age < 25:
    if speed > 140:
      return red # Crazy young guy, car too fast
    else:
      return yellow # Car is slow enough for medium risk

  if age > 75:
    return red # Get off the road, old man!

  if miles_per_year > 30:
    return red # You drive too much

  if miles_per_year > 20:
    return yellow

  return green # otherwise, low risk
