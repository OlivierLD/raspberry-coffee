from math import radians, sin, cos, sqrt, asin, acos, degrees

def haversine(lat1, lon1, lat2, lon2):
    """
    Return the haversine approximation to the great-circle distance between two
    points (in meters).
    """
    R = 6372.8 # Earth radius in kilometers

    dLat = radians(lat2 - lat1)
    dLon = radians(lon2 - lon1)

    lat1 = radians(lat1)
    lat2 = radians(lat2)

    a = sin(dLat / 2.0)**2 + cos(lat1) * cos(lat2) * sin(dLon / 2.0)**2
    c = 2.0 * asin(sqrt(a))

    return R * c * 1000.0


def great_circle(lat1, lon1, lat2, lon2):
    """GC dist in nautical miles"""
    cos_val = sin(radians(lat1)) * sin(radians(lat2)) + \
              cos(radians(lat1)) * cos(radians(lat2)) * \
              cos(radians(lon2) - radians(lon1))
    dist = acos(cos_val)
    return degrees(dist) * 60


def great_circle_km(lat1, lon1, lat2, lon2):
    """GC dist in km"""
    return great_circle(lat1, lon1, lat2, lon2) * 1.852

lat1 = 37.75
lon1 = -122.5
lat2 = 36.0
lon2 = -123.0

gc_km = great_circle_km(lat1, lon1, lat2, lon2)
haversine = haversine(lat1, lon1, lat2, lon2)

print("Haversine {} m, GC {}m, diff {:.4f} => {:.3f}%".format(\
    haversine, gc_km * 1000, \
    abs(haversine - (gc_km * 1000)), \
    100 * (abs(haversine - (gc_km * 1000)) / max(haversine, gc_km * 1000))))

