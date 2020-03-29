import math
import math_utils as mu

from earth import Earth
from venus import Venus
from mars import Mars
from jupiter import Jupiter
from saturn import Saturn

DEBUG = False


class LongTermAlmanac:
    # local variables
    T = 0
    T2 = 0
    T3 = 0
    T4 = 0
    T5 = 0
    TE = 0
    TE2 = 0
    TE3 = 0
    TE4 = 0
    TE5 = 0
    Tau = 0
    Tau2 = 0
    Tau3 = 0
    Tau4 = 0
    Tau5 = 0
    deltaT = 0
    eps0 = 0
    eps = 0
    deltaPsi = 0
    deltaEps = 0
    Le = 0
    Be = 0
    Re = 0
    kappa = 0
    pi0 = 0
    e = 0
    lambdaSun = 0
    RASun = 0
    DECSun = 0
    GHASun = 0
    SDSun = 0
    HPSun = 0
    EoT = 0
    fmtEoT = 0
    EoE = 0
    EoEout = 0
    Lsun_true = 0
    Lsun_prime = 0
    dES = 0
    dayFraction = 0
    GHAAmean = 0
    RAVenus = 0
    DECVenus = 0
    GHAVenus = 0
    SDVenus = 0
    HPVenus = 0
    RAMars = 0
    DECMars = 0
    GHAMars = 0
    SDMars = 0
    HPMars = 0
    RAJupiter = 0
    DECJupiter = 0
    GHAJupiter = 0
    SDJupiter = 0
    HPJupiter = 0
    RASaturn = 0
    DECSaturn = 0
    GHASaturn = 0
    SDSaturn = 0
    HPSaturn = 0
    RAMoon = 0
    DECMoon = 0
    GHAMoon = 0
    SDMoon = 0
    HPMoon = 0
    RAPol = 0
    DECPol = 0
    GHAPol = 0
    OoE = 0
    tOoE = 0
    LDist = 0
    JD0h = 0
    JD = 0
    JDE = 0
    lambdaMapp = 0
    SidTm = ""
    GHAAtrue = 0
    SidTa = ""
    moonPhaseAngle = 0
    moonPhase = ""
    DoW = ""
    illumMoon = 0
    illumVenus = 0
    illumMars = 0
    illumJupiter = 0
    illumSaturn = 0

    #
    # Main function
    # @param year Number, UTC year
    # @param month Number, UTC month, [1..12]
    # @param day Number, UTC day of month
    # @param hour Number, UTC hour
    # @param minute Number, UTC minute
    # @param second Number, UTC second
    # @param delta_t Number, DeltaT
    #
    @staticmethod
    def calculate(year, month, day, hour, minute, second, delta_t):
        LongTermAlmanac.calculateJulianDate(year, month, day, hour, minute, second, delta_t)
        LongTermAlmanac.calculateNutation()
        LongTermAlmanac.calculateAberration()
        LongTermAlmanac.calculateAries()
        LongTermAlmanac.calculateSun()
        LongTermAlmanac.calculateVenus()
        LongTermAlmanac.calculateMars()
        LongTermAlmanac.calculateJupiter()
        LongTermAlmanac.calculateSaturn()
        LongTermAlmanac.calculateMoon()
        LongTermAlmanac.calculatePolaris()
        LongTermAlmanac.calculateMoonPhase()
        LongTermAlmanac.calculateWeekDay()
        return

    @staticmethod
    def isLeapYear(year):
        ly = False
        if year / 4 - math.floor(year / 4) == 0:
            ly = True
        if year / 100 - math.floor(year / 100) == 0:
            ly = False
        if year / 400 - math.floor(year / 400) == 0:
            ly = True
        return ly

    #
    # Input data conversion and reworking
    # All data are UTC data (except detlaT)
    #
    # @param year Number, UTC year
    # @param month Number, UTC month, [1..12]
    # @param day Number, UTC day of month
    # @param hour Number, UTC hour
    # @param minute Number, UTC minute
    # @param second Number, UTC second
    # @param delta_t Number, DeltaT
    #
    @staticmethod
    def calculateJulianDate(year, month, day, hour, minute, second, delta_t):

        LongTermAlmanac.dayFraction = (hour + minute / 60 + second / 3600) / 24
        if LongTermAlmanac.dayFraction < 0 or LongTermAlmanac.dayFraction > 1:
            raise ValueError("Time out of range! Please restart calculation.")

        LongTermAlmanac.deltaT = delta_t

        # Calculating Julian date, century, and millennium

        # Julian date (UT1)
        if month <= 2:
            year -= 1
            month += 12

        A = math.floor(year / 100)
        B = 2 - A + math.floor(A / 4)
        LongTermAlmanac.JD0h = math.floor(365.25 * (year + 4716)) + math.floor(30.6001 * (month + 1)) + day + B - 1524.5

        LongTermAlmanac.JD = LongTermAlmanac.JD0h + LongTermAlmanac.dayFraction

        # Julian centuries (UT1) from 2000 January 0.5
        LongTermAlmanac.T = (LongTermAlmanac.JD - 2451545) / 36525
        LongTermAlmanac.T2 = LongTermAlmanac.T * LongTermAlmanac.T
        LongTermAlmanac.T3 = LongTermAlmanac.T * LongTermAlmanac.T2
        LongTermAlmanac.T4 = LongTermAlmanac.T * LongTermAlmanac.T3
        LongTermAlmanac.T5 = LongTermAlmanac.T * LongTermAlmanac.T4

        # Julian ephemeris date (TDT)
        LongTermAlmanac.JDE = LongTermAlmanac.JD + LongTermAlmanac.deltaT / 86400

        # Julian centuries (TDT) from 2000 January 0.5
        LongTermAlmanac.TE = (LongTermAlmanac.JDE - 2451545) / 36525
        LongTermAlmanac.TE2 = LongTermAlmanac.TE * LongTermAlmanac.TE
        LongTermAlmanac.TE3 = LongTermAlmanac.TE * LongTermAlmanac.TE2
        LongTermAlmanac.TE4 = LongTermAlmanac.TE * LongTermAlmanac.TE3
        LongTermAlmanac.TE5 = LongTermAlmanac.TE * LongTermAlmanac.TE4

        # Julian millenniums (TDT) from 2000 January 0.5
        LongTermAlmanac.Tau = 0.1 * LongTermAlmanac.TE
        LongTermAlmanac.Tau2 = LongTermAlmanac.Tau * LongTermAlmanac.Tau
        LongTermAlmanac.Tau3 = LongTermAlmanac.Tau * LongTermAlmanac.Tau2
        LongTermAlmanac.Tau4 = LongTermAlmanac.Tau * LongTermAlmanac.Tau3
        LongTermAlmanac.Tau5 = LongTermAlmanac.Tau * LongTermAlmanac.Tau4

        if DEBUG:
            print("DayFraction {}, JD0h: {}, JD: {}".format(LongTermAlmanac.dayFraction, LongTermAlmanac.JD0h, LongTermAlmanac.JD))
        return

    # Output Hour Angle
    @staticmethod
    def outHA(x):
        GHAdeg = math.floor(x)
        GHAmin = math.floor(60 * (x - GHAdeg))
        GHAsec = round(3600 * ((x - GHAdeg) - (GHAmin / 60)))
        if GHAsec == 60:
            GHAsec = 0
            GHAmin += 1

        if GHAmin == 60:
            GHAmin = 0
            GHAdeg += 1

        if GHAdeg == 0:
            GHAdeg = "000"
        elif GHAdeg < 10:
            GHAdeg = "00" + str(GHAdeg)
        elif GHAdeg < 100:
            GHAdeg = "0" + str(GHAdeg)

        if GHAmin == 0:
            GHAmin = "00"
        elif GHAmin < 10:
            GHAmin = "0" + str(GHAmin)

        if GHAsec < 10:
            GHAsec = "0" + str(GHAsec)

        return "{}\xb0 {}' {}\"".format(GHAdeg, GHAmin, GHAsec)

    # Output Right Ascension
    @staticmethod
    def outRA(x):
        t = x / 15
        RAh = math.floor(t)
        RAmin = math.floor(60 * (t - RAh))
        RAsec = round(10 * (3600 * (t - RAh - RAmin / 60))) / 10
        if RAsec == 60:
            RAsec = 0
            RAmin += 1

        if RAmin == 60:
            RAmin = 0
            RAh += 1

        if RAh == 0:
            RAh = "00"
        elif RAh < 10:
            RAh = "0" + str(RAh)

        if RAmin == 0:
            RAmin = "00"
        elif RAmin < 10:
            RAmin = "0" + str(RAmin)

        if RAsec < 10:
            RAsec = "0" + str(RAsec)

        return "{}h {}m {}s".format(RAh, RAmin, RAsec)

    # Equation of Time
    @staticmethod
    def outEoT(x):
        sign = ""
        if x < 0:
            sign = "-"
        else:
            sign = "+"

        EoT = abs(x)
        EOTmin = math.floor(EoT)
        EOTsec = round(600 * (EoT - EOTmin)) / 10
        if EOTsec - math.floor(EOTsec) == 0:
            EOTsec += ".0"

        if EOTmin == 0:
            EoT = " {} {}s".format(sign, EOTsec)
        else:
            EoT = "{} {}m {}s ".format(sign, EOTmin, EOTsec)

        return EoT

    # Output Obliquity of Ecliptic
    @staticmethod
    def outECL(x):
        ECLdeg = math.floor(x)
        ECLmin = math.floor(60 * (x - ECLdeg))
        ECLsec = round(3600000 * (x - ECLdeg - ECLmin / 60)) / 1000
        if ECLsec == 60:
            ECLsec = 0
            ECLmin += 1

        if ECLmin == 60:
            ECLmin = 0
            ECLdeg += 1

        if ECLmin == 0:
            ECLmin = "00"
        elif ECLmin < 10:
            ECLmin = "0" + ECLmin

        if ECLsec < 10:
            ECLsec = "0" + ECLsec

        return "{}\xb0 {}' {}\"".format(ECLdeg, ECLmin, ECLsec)

    # Output Sidereal Time
    @staticmethod
    def outSideralTime(x):
        GMSTdecimal = x / 15
        GMSTh = math.floor(GMSTdecimal)
        GMSTmdecimal = 60 * (GMSTdecimal - GMSTh)
        GMSTm = math.floor(GMSTmdecimal)
        GMSTsdecimal = 60 * (GMSTmdecimal - GMSTm)
        GMSTs = round(1000 * GMSTsdecimal) / 1000
        if GMSTs - math.floor(GMSTs) == 0:
            GMSTs += ".000"
        elif 10 * GMSTs - math.floor(10 * GMSTs) == 0:
            GMSTs += "00"
        elif 100 * GMSTs - math.floor(100 * GMSTs) == 0:
            GMSTs += "0"

        return "{}h {}m {}s".format(GMSTh, GMSTm, GMSTs)

    # Output Declination
    @staticmethod
    def outDec(x):
        name = "N"
        signDEC = 0
        if x < 0:
            signDEC = -1
            name = "S"
        else:
            signDEC = 1
            name = "N"

        DEC = abs(x)
        DECdeg = math.floor(DEC)
        DECmin = math.floor(60 * (DEC - DECdeg))
        DECsec = round(3600 * (DEC - DECdeg - DECmin / 60))
        if DECsec == 60:
            DECsec = 0
            DECmin += 1

        if DECmin == 60:
            DECmin = 0
            DECdeg += 1

        if DECdeg == 0:
            DECdeg = "00"
        elif DECdeg < 10:
            DECdeg = "0" + str(DECdeg)

        if DECmin == 0:
            DECmin = "00"
        elif DECmin < 10:
            DECmin = "0" + str(DECmin)

        if DECsec < 10:
            DECsec = "0" + str(DECsec)

        return "{}  {}\xb0 {}' {}\"".format(name, DECdeg, DECmin, DECsec)

    # Output SD and HP
    @staticmethod
    def outSdHp(x):
        x = round(10 * x) / 10
        if x - math.floor(x) == 0:
            x = str(x) + ".0"

        return "{}\"".format(x)

    #
    # Astronomical functions
    #
    # Nutation, obliquity of the ecliptic
    @staticmethod
    def calculateNutation():
        # IAU 1980 calculateNutation theory:
    
        # Mean anomaly of the Moon
        Mm = 134.962981389 + 198.867398056 * LongTermAlmanac.TE + mu.norm_360_deg(477000 * LongTermAlmanac.TE) + 0.008697222222 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 56250
    
        # Mean anomaly of the Sun
        M = 357.527723333 + 359.05034 * LongTermAlmanac.TE + mu.norm_360_deg(35640 * LongTermAlmanac.TE) - 0.0001602777778 * LongTermAlmanac.TE2 - LongTermAlmanac.TE3 / 300000
    
        # Mean distance of the Moon from ascending node
        F = 93.271910277 + 82.017538055 * LongTermAlmanac.TE + mu.norm_360_deg(483120 * LongTermAlmanac.TE) - 0.0036825 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 327272.7273
    
        # Mean elongation of the Moon
        D = 297.850363055 + 307.11148 * LongTermAlmanac.TE + mu.norm_360_deg(444960 * LongTermAlmanac.TE) - 0.001914166667 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 189473.6842
    
        # Longitude of the ascending node of the Moon
        omega = 125.044522222 - 134.136260833 * LongTermAlmanac.TE - mu.norm_360_deg(1800 * LongTermAlmanac.TE) + 0.002070833333 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 450000
    
        # Periodic terms for nutation
        nut = [
            [  0,  0,  0,  0,  1, -171996, -174.2,  92025,  8.9 ],
            [  0,  0,  2, -2,  2,  -13187,   -1.6,   5736, -3.1 ],
            [  0,  0,  2,  0,  2,   -2274,   -0.2,    977, -0.5 ],
            [  0,  0,  0,  0,  2,    2062,    0.2,   -895,  0.5 ],
            [  0, -1,  0,  0,  0,   -1426,    3.4,     54, -0.1 ],
            [  1,  0,  0,  0,  0,     712,    0.1,     -7,  0.0 ],
            [  0,  1,  2, -2,  2,    -517,    1.2,    224, -0.6 ],
            [  0,  0,  2,  0,  1,    -386,   -0.4,    200,  0.0 ],
            [  1,  0,  2,  0,  2,    -301,    0.0,    129, -0.1 ],
            [  0, -1,  2, -2,  2,     217,   -0.5,    -95,  0.3 ],
            [ -1,  0,  0,  2,  0,     158,    0.0,     -1,  0.0 ],
            [  0,  0,  2, -2,  1,     129,    0.1,    -70,  0.0 ],
            [ -1,  0,  2,  0,  2,     123,    0.0,    -53,  0.0 ],
            [  1,  0,  0,  0,  1,      63,    0.1,    -33,  0.0 ],
            [  0,  0,  0,  2,  0,      63,    0.0,     -2,  0.0 ],
            [ -1,  0,  2,  2,  2,     -59,    0.0,     26,  0.0 ],
            [ -1,  0,  0,  0,  1,     -58,   -0.1,     32,  0.0 ],
            [  1,  0,  2,  0,  1,     -51,    0.0,     27,  0.0 ],
            [ -2,  0,  0,  2,  0,     -48,    0.0,      1,  0.0 ],
            [ -2,  0,  2,  0,  1,      46,    0.0,    -24,  0.0 ],
            [  0,  0,  2,  2,  2,     -38,    0.0,     16,  0.0 ],
            [  2,  0,  2,  0,  2,     -31,    0.0,     13,  0.0 ],
            [  2,  0,  0,  0,  0,      29,    0.0,     -1,  0.0 ],
            [  1,  0,  2, -2,  2,      29,    0.0,    -12,  0.0 ],
            [  0,  0,  2,  0,  0,      26,    0.0,     -1,  0.0 ],
            [  0,  0,  2, -2,  0,     -22,    0.0,      0,  0.0 ],
            [ -1,  0,  2,  0,  1,      21,    0.0,    -10,  0.0 ],
            [  0,  2,  0,  0,  0,      17,   -0.1,      0,  0.0 ],
            [  0,  2,  2, -2,  2,     -16,    0.1,      7,  0.0 ],
            [ -1,  0,  0,  2,  1,      16,    0.0,     -8,  0.0 ],
            [  0,  1,  0,  0,  1,     -15,    0.0,      9,  0.0 ],
            [  1,  0,  0, -2,  1,     -13,    0.0,      7,  0.0 ],
            [  0, -1,  0,  0,  1,     -12,    0.0,      6,  0.0 ],
            [  2,  0, -2,  0,  0,      11,    0.0,      0,  0.0 ],
            [ -1,  0,  2,  2,  1,     -10,    0.0,      5,  0.0 ],
            [  1,  0,  2,  2,  2,      -8,    0.0,      3,  0.0 ],
            [  0, -1,  2,  0,  2,      -7,    0.0,      3,  0.0 ],
            [  0,  0,  2,  2,  1,      -7,    0.0,      3,  0.0 ],
            [  1,  1,  0, -2,  0,      -7,    0.0,      0,  0.0 ],
            [  0,  1,  2,  0,  2,       7,    0.0,     -3,  0.0 ],
            [ -2,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 ],
            [  0,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 ],
            [  2,  0,  2, -2,  2,       6,    0.0,     -3,  0.0 ],
            [  1,  0,  0,  2,  0,       6,    0.0,      0,  0.0 ],
            [  1,  0,  2, -2,  1,       6,    0.0,     -3,  0.0 ],
            [  0,  0,  0, -2,  1,      -5,    0.0,      3,  0.0 ],
            [  0, -1,  2, -2,  1,      -5,    0.0,      3,  0.0 ],
            [  2,  0,  2,  0,  1,      -5,    0.0,      3,  0.0 ],
            [  1, -1,  0,  0,  0,       5,    0.0,      0,  0.0 ],
            [  1,  0,  0, -1,  0,      -4,    0.0,      0,  0.0 ],
            [  0,  0,  0,  1,  0,      -4,    0.0,      0,  0.0 ],
            [  0,  1,  0, -2,  0,      -4,    0.0,      0,  0.0 ],
            [  1,  0, -2,  0,  0,       4,    0.0,      0,  0.0 ],
            [  2,  0,  0, -2,  1,       4,    0.0,     -2,  0.0 ],
            [  0,  1,  2, -2,  1,       4,    0.0,     -2,  0.0 ],
            [  1,  1,  0,  0,  0,      -3,    0.0,      0,  0.0 ],
            [  1, -1,  0, -1,  0,      -3,    0.0,      0,  0.0 ],
            [ -1, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 ],
            [  0, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 ],
            [  1, -1,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
            [  3,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
            [ -2,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
            [  1,  0,  2,  0,  0,       3,    0.0,      0,  0.0 ],
            [ -1,  0,  2,  4,  2,      -2,    0.0,      1,  0.0 ],
            [  1,  0,  0,  0,  2,      -2,    0.0,      1,  0.0 ],
            [ -1,  0,  2, -2,  1,      -2,    0.0,      1,  0.0 ],
            [  0, -2,  2, -2,  1,      -2,    0.0,      1,  0.0 ],
            [ -2,  0,  0,  0,  1,      -2,    0.0,      1,  0.0 ],
            [  2,  0,  0,  0,  1,       2,    0.0,     -1,  0.0 ],
            [  3,  0,  0,  0,  0,       2,    0.0,      0,  0.0 ],
            [  1,  1,  2,  0,  2,       2,    0.0,     -1,  0.0 ],
            [  0,  0,  2,  1,  2,       2,    0.0,     -1,  0.0 ],
            [  1,  0,  0,  2,  1,      -1,    0.0,      0,  0.0 ],
            [  1,  0,  2,  2,  1,      -1,    0.0,      1,  0.0 ],
            [  1,  1,  0, -2,  1,      -1,    0.0,      0,  0.0 ],
            [  0,  1,  0,  2,  0,      -1,    0.0,      0,  0.0 ],
            [  0,  1,  2, -2,  0,      -1,    0.0,      0,  0.0 ],
            [  0,  1, -2,  2,  0,      -1,    0.0,      0,  0.0 ],
            [  1,  0, -2,  2,  0,      -1,    0.0,      0,  0.0 ],
            [  1,  0, -2, -2,  0,      -1,    0.0,      0,  0.0 ],
            [  1,  0,  2, -2,  0,      -1,    0.0,      0,  0.0 ],
            [  1,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 ],
            [  2,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 ],
            [  0,  0,  2,  4,  2,      -1,    0.0,      0,  0.0 ],
            [  0,  0,  2, -1,  2,      -1,    0.0,      0,  0.0 ],
            [ -2,  0,  2,  4,  2,      -1,    0.0,      1,  0.0 ],
            [  2,  0,  2,  2,  2,      -1,    0.0,      0,  0.0 ],
            [  0, -1,  2,  0,  1,      -1,    0.0,      0,  0.0 ],
            [  0,  0, -2,  0,  1,      -1,    0.0,      0,  0.0 ],
            [  0,  0,  4, -2,  2,       1,    0.0,      0,  0.0 ],
            [  0,  1,  0,  0,  2,       1,    0.0,      0,  0.0 ],
            [  1,  1,  2, -2,  2,       1,    0.0,     -1,  0.0 ],
            [  3,  0,  2, -2,  2,       1,    0.0,      0,  0.0 ],
            [ -2,  0,  2,  2,  2,       1,    0.0,     -1,  0.0 ],
            [ -1,  0,  0,  0,  2,       1,    0.0,     -1,  0.0 ],
            [  0,  0, -2,  2,  1,       1,    0.0,      0,  0.0 ],
            [  0,  1,  2,  0,  1,       1,    0.0,      0,  0.0 ],
            [ -1,  0,  4,  0,  2,       1,    0.0,      0,  0.0 ],
            [  2,  1,  0, -2,  0,       1,    0.0,      0,  0.0 ],
            [  2,  0,  0,  2,  0,       1,    0.0,      0,  0.0 ],
            [  2,  0,  2, -2,  1,       1,    0.0,     -1,  0.0 ],
            [  2,  0, -2,  0,  1,       1,    0.0,      0,  0.0 ],
            [  1, -1,  0, -2,  0,       1,    0.0,      0,  0.0 ],
            [ -1,  0,  0,  1,  1,       1,    0.0,      0,  0.0 ],
            [ -1, -1,  0,  2,  1,       1,    0.0,      0,  0.0 ],
            [  0,  1,  0,  1,  0,       1,    0.0,      0,  0.0 ]
        ]
    
        # Reading periodic terms
        fMm = 0
        fM = 0 
        fF = 0 
        fD = 0  
        f_omega = 0 
        dp = 0
        de = 0 
    
        x = 0
        while x < len(nut):
            fMm = nut[x][0]
            fM = nut[x][1]
            fF = nut[x][2]
            fD = nut[x][3]
            f_omega = nut[x][4]
            dp += (nut[x][5] + LongTermAlmanac.TE * nut[x][6]) * mu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)
            de += (nut[x][7] + LongTermAlmanac.TE * nut[x][8]) * mu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)
            x += 1
    
        # Corrections (Herring, 1987)
        corr = [
            [ 0, 0, 0, 0, 1,-725, 417, 213, 224 ],
            [ 0, 1, 0, 0, 0, 523,  61, 208, -24 ],
            [ 0, 0, 2,-2, 2, 102,-118, -41, -47 ],
            [ 0, 0, 2, 0, 2, -81,   0,  32,   0 ]
        ]
        x = 0
        while x < len(corr):
            fMm = corr[x][0]
            fM = corr[x][1]
            fF = corr[x][2]
            fD = corr[x][3]
            f_omega = corr[x][4]
            dp += 0.1 * (corr[x][5] * mu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][6] * mu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega))
            de += 0.1 * (corr[x][7] * mu.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][8] * mu.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega))
            x += 1
    
        # calculateNutation in longitude
        LongTermAlmanac.deltaPsi = dp / 36000000
    
        # calculateNutation in obliquity
        LongTermAlmanac.deltaEps = de / 36000000
    
        # Mean obliquity of the ecliptic
        LongTermAlmanac.eps0 = (84381.448 - 46.815 * LongTermAlmanac.TE - 0.00059 * LongTermAlmanac.TE2 + 0.001813 * LongTermAlmanac.TE3) / 3600
    
        # True obliquity of the ecliptic
        LongTermAlmanac.eps = LongTermAlmanac.eps0 + LongTermAlmanac.deltaEps
        return

    # aberration
    @staticmethod
    def calculateAberration():
        LongTermAlmanac.kappa = math.radians(20.49552 / 3600)
        LongTermAlmanac.pi0 = math.radians(102.93735 + 1.71953 * LongTermAlmanac.TE + 0.00046 * LongTermAlmanac.TE2)
        LongTermAlmanac.e = 0.016708617 - 0.000042037 * LongTermAlmanac.TE - 0.0000001236 * LongTermAlmanac.TE2
        return

    # GHA Aries, GAST, GMST, equation of the equinoxes
    @staticmethod
    def calculateAries():
        # Mean GHA Aries
        LongTermAlmanac.GHAAmean = mu.norm_360_deg(280.46061837 + 360.98564736629 * (LongTermAlmanac.JD - 2451545) + 0.000387933 * LongTermAlmanac.T2 - LongTermAlmanac.T3 / 38710000)
    
        # GMST
        LongTermAlmanac.SidTm = LongTermAlmanac.outSideralTime(LongTermAlmanac.GHAAmean)
    
        # True GHA Aries
        LongTermAlmanac.GHAAtrue = mu.norm_360_deg(LongTermAlmanac.GHAAmean + LongTermAlmanac.deltaPsi * mu.cosd(LongTermAlmanac.eps))
    
        # GAST
        LongTermAlmanac.SidTa = LongTermAlmanac.outSideralTime(LongTermAlmanac.GHAAtrue)
    
        # Equation of the equinoxes
        LongTermAlmanac.EoE = 240 * LongTermAlmanac.deltaPsi * mu.cosd(LongTermAlmanac.eps)
        LongTermAlmanac.EoEout = round(1000 * LongTermAlmanac.EoE) / 1000
        # EoEout = " " + EoEout + "s"
        return


    # Calculations for the Sun
    @staticmethod
    def calculateSun():
        # Mean longitude of the Sun
        LongTermAlmanac.Lsun_mean = mu.norm_360_deg(280.4664567 + 360007.6982779 * LongTermAlmanac.Tau + 0.03032028 * LongTermAlmanac.Tau2 + LongTermAlmanac.Tau3 / 49931 - LongTermAlmanac.Tau4 / 15299 - LongTermAlmanac.Tau5 / 1988000)
    
        # Heliocentric longitude of the Earth
        LongTermAlmanac.Le = Earth.l_earth(LongTermAlmanac.Tau)
    
        # Geocentric longitude of the Sun
        LongTermAlmanac.Lsun_true = mu.norm_360_deg(LongTermAlmanac.Le + 180 - 0.000025)
    
        # Heliocentric latitude of Earth
        LongTermAlmanac.Be = Earth.b_earth(LongTermAlmanac.Tau)
    
        # Geocentric latitude of the Sun
        beta = mu.norm_360_deg(-LongTermAlmanac.Be)
    
        # Corrections
        LongTermAlmanac.Lsun_prime = mu.norm_360_deg(LongTermAlmanac.Le + 180 - 1.397 * LongTermAlmanac.TE - 0.00031 * LongTermAlmanac.TE2)
    
        beta = beta + 0.000011 * (mu.cosd(LongTermAlmanac.Lsun_prime) - mu.sind(LongTermAlmanac.Lsun_prime))
    
        # Distance Earth-Sun
        LongTermAlmanac.Re = Earth.r_earth(LongTermAlmanac.Tau)
        LongTermAlmanac.dES = 149597870.691 * LongTermAlmanac.Re
    
        # Apparent longitude of the Sun
        LongTermAlmanac.lambdaSun = mu.norm_360_deg(LongTermAlmanac.Lsun_true + LongTermAlmanac.deltaPsi - 0.005691611 / LongTermAlmanac.Re)
    
        # Right ascension of the Sun, apparent
        LongTermAlmanac.RASun = math.degrees(mu.norm2_pi_rad(math.atan2((mu.sind(LongTermAlmanac.lambdaSun) * mu.cosd(LongTermAlmanac.eps) - mu.tand(beta) * mu.sind(LongTermAlmanac.eps)), mu.cosd(LongTermAlmanac.lambdaSun))))
    
        # Declination of the Sun, apparent
        LongTermAlmanac.DECSun = math.degrees(math.asin(mu.sind(beta) * mu.cosd(LongTermAlmanac.eps) + mu.cosd(beta) * mu.sind(LongTermAlmanac.eps) * mu.sind(LongTermAlmanac.lambdaSun)))
    
        # GHA of the Sun
        LongTermAlmanac.GHASun = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RASun)
    
        # Semidiameter of the Sun
        LongTermAlmanac.SDSun = 959.63 / LongTermAlmanac.Re
    
        #Horizontal parallax of the Sun
        LongTermAlmanac.HPSun = 8.794 / LongTermAlmanac.Re
    
        # Equation of time
        # EoT = 4*(Lsun_mean-0.0057183-0.0008-RASun+deltaPsi*mu.cosd(eps))
        LongTermAlmanac.EoT = 4 * LongTermAlmanac.GHASun + 720 - 1440 * LongTermAlmanac.dayFraction
        if LongTermAlmanac.EoT > 20:
            LongTermAlmanac.EoT -= 1440
    
        if LongTermAlmanac.EoT < -20:
            LongTermAlmanac.EoT += 1440

        return

    # Calculations for Venus
    @staticmethod
    def calculateVenus():
        # Heliocentric spherical coordinates
        L = Venus.l_venus(LongTermAlmanac.Tau)
        B = Venus.b_venus(LongTermAlmanac.Tau)
        R = Venus.r_venus(LongTermAlmanac.Tau)

        # Rectangular coordinates
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        # Geocentric spherical coordinates
        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # Distance from Earth / light time
        d = math.sqrt(x * x + y * y + z * z)
        lt = 0.0057755183 * d

        # Time correction
        Tau_corr = (LongTermAlmanac.JDE - lt - 2451545) / 365250

        # Coordinates corrected for light time
        L = Venus.l_venus(Tau_corr)
        B = Venus.b_venus(Tau_corr)
        R = Venus.r_venus(Tau_corr)
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # aberration
        dlambda = (LongTermAlmanac.e * LongTermAlmanac.kappa * math.cos(LongTermAlmanac.pi0 - _lambda) - LongTermAlmanac.kappa * math.cos(math.radians(LongTermAlmanac.Lsun_true) - _lambda)) / math.cos(beta)
        dbeta = -LongTermAlmanac.kappa * math.sin(beta) * (math.sin(math.radians(LongTermAlmanac.Lsun_true) - _lambda) - LongTermAlmanac.e * math.sin(LongTermAlmanac.pi0 - _lambda))

        _lambda += dlambda
        beta += dbeta

        # FK5
        lambda_prime = _lambda - math.radians(1.397) * LongTermAlmanac.TE - math.radians(0.00031) * LongTermAlmanac.TE2

        dlambda = math.radians(-0.09033) / 3600 + math.radians(0.03916) / 3600 * (math.cos(lambda_prime) + math.sin(lambda_prime)) * math.tan(beta)
        dbeta = math.radians(0.03916) / 3600 * (math.cos(lambda_prime) - math.sin(lambda_prime))

        _lambda += dlambda
        beta += dbeta

        # calculateNutation in longitude
        _lambda += math.radians(LongTermAlmanac.deltaPsi)

        # Right ascension, apparent
        LongTermAlmanac.RAVenus = math.degrees(mu.norm2_pi_rad(math.atan2((math.sin(_lambda) * mu.cosd(LongTermAlmanac.eps) - math.tan(beta) * mu.sind(LongTermAlmanac.eps)), math.cos(_lambda))))

        # Declination of Venus, apparent
        LongTermAlmanac.DECVenus = math.degrees(math.asin(math.sin(beta) * mu.cosd(LongTermAlmanac.eps) + math.cos(beta) * mu.sind(LongTermAlmanac.eps) * math.sin(_lambda)))

        # GHA of Venus
        LongTermAlmanac.GHAVenus = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RAVenus)

        # Semi-diameter of Venus (including cloud layer)
        LongTermAlmanac.SDVenus = 8.41 / d

        # Horizontal parallax of Venus
        LongTermAlmanac.HPVenus = 8.794 / d

        # Illumination of the planet's disk
        k = 100 * (1 + ((R - LongTermAlmanac.Re * mu.cosd(B) * mu.cosd(L - LongTermAlmanac.Le)) / d)) / 2
        LongTermAlmanac.illumVenus = round(10 * k) / 10

        return

    # Calculations for Mars
    @staticmethod
    def calculateMars():
        # Heliocentric coordinates
        L = Mars.l_mars(LongTermAlmanac.Tau)
        B = Mars.b_mars(LongTermAlmanac.Tau)
        R = Mars.r_mars(LongTermAlmanac.Tau)

        # Rectangular coordinates
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        # Geocentric coordinates
        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # Distance from earth / light time
        d = math.sqrt(x * x + y * y + z * z)
        lt = 0.0057755183 * d

        # Time correction
        Tau_corr = (LongTermAlmanac.JDE - lt - 2451545) / 365250

        # Coordinates corrected for light time
        L = Mars.l_mars(Tau_corr)
        B = Mars.b_mars(Tau_corr)
        R = Mars.r_mars(Tau_corr)
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # aberration
        dlambda = (LongTermAlmanac.e * LongTermAlmanac.kappa * math.cos(LongTermAlmanac.pi0 - _lambda) - LongTermAlmanac.kappa * math.cos(math.radians(LongTermAlmanac.Lsun_true) - _lambda)) / math.cos(beta)
        dbeta = -LongTermAlmanac.kappa * math.sin(beta) * (math.sin(math.radians(LongTermAlmanac.Lsun_true) - _lambda) - LongTermAlmanac.e * math.sin(LongTermAlmanac.pi0 - _lambda))

        _lambda += dlambda
        beta += dbeta

        # FK5
        lambda_prime = _lambda - math.radians(1.397) * LongTermAlmanac.TE -  math.radians(0.00031) * LongTermAlmanac.TE2

        dlambda = math.radians(-0.09033) / 3600 + math.radians(0.03916) / 3600 * (math.cos(lambda_prime) + math.sin(lambda_prime)) * math.tan(beta)
        dbeta = math.radians(0.03916) / 3600 * (math.cos(lambda_prime) - math.sin(lambda_prime))

        _lambda += dlambda
        beta += dbeta

        # calculateNutation in longitude
        _lambda +=  math.radians(LongTermAlmanac.deltaPsi)

        # Right ascension, apparent
        LongTermAlmanac.RAMars = math.degrees(mu.norm2_pi_rad(math.atan2((math.sin(_lambda) * mu.cosd(LongTermAlmanac.eps) - math.tan(beta) * mu.sind(LongTermAlmanac.eps)), math.cos(_lambda))))

        # Declination of Mars, apparent
        LongTermAlmanac.DECMars = math.degrees(math.asin(math.sin(beta) * mu.cosd(LongTermAlmanac.eps) + math.cos(beta) * mu.sind(LongTermAlmanac.eps) * math.sin(_lambda)))

        # GHA of Mars
        LongTermAlmanac.GHAMars = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RAMars)

        # Semi-diameter of Mars
        LongTermAlmanac.SDMars = 4.68 / d

        # Horizontal parallax of Mars
        LongTermAlmanac.HPMars = 8.794 / d

        # Illumination of the planet's disk
        k = 100 * (1 + ((R - LongTermAlmanac.Re * mu.cosd(B) * mu.cosd(L - LongTermAlmanac.Le)) / d)) / 2
        LongTermAlmanac.illumMars = round(10 * k) / 10

        return

    # Calculations for Jupiter
    @staticmethod
    def calculateJupiter():
        # Heliocentric coordinates
        L = Jupiter.l_jupiter(LongTermAlmanac.Tau)
        B = Jupiter.b_jupiter(LongTermAlmanac.Tau)
        R = Jupiter.r_jupiter(LongTermAlmanac.Tau)

        # Rectangular coordinates
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        # Geocentric coordinates
        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # Distance from earth / light time
        d = math.sqrt(x * x + y * y + z * z)
        lt = 0.0057755183 * d

        # Time correction
        Tau_corr = (LongTermAlmanac.JDE - lt - 2451545) / 365250

        # Coordinates corrected for light time
        L = Jupiter.l_jupiter(Tau_corr)
        B = Jupiter.b_jupiter(Tau_corr)
        R = Jupiter.r_jupiter(Tau_corr)
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # aberration
        dlambda = (LongTermAlmanac.e * LongTermAlmanac.kappa * math.cos(LongTermAlmanac.pi0 - _lambda) - LongTermAlmanac.kappa * math.cos( math.radians(LongTermAlmanac.Lsun_true) - _lambda)) / math.cos(beta)
        dbeta = -LongTermAlmanac.kappa * math.sin(beta) * (math.sin( math.radians(LongTermAlmanac.Lsun_true) - _lambda) - LongTermAlmanac.e * math.sin(LongTermAlmanac.pi0 - _lambda))

        _lambda += dlambda
        beta += dbeta

        # FK5
        lambda_prime = _lambda -  math.radians(1.397) * LongTermAlmanac.TE -  math.radians(0.00031) * LongTermAlmanac.TE2

        dlambda = math.radians(-0.09033) / 3600 +  math.radians(0.03916) / 3600 * (math.cos(lambda_prime) + math.sin(lambda_prime)) * math.tan(beta)
        dbeta = math.radians(0.03916) / 3600 * (math.cos(lambda_prime) - math.sin(lambda_prime))

        _lambda += dlambda
        beta += dbeta

        # calculateNutation in longitude
        _lambda += math.radians(LongTermAlmanac.deltaPsi)

        # Right ascension, apparent
        LongTermAlmanac.RAJupiter = math.degrees(mu.norm2_pi_rad(math.atan2((math.sin(_lambda) * mu.cosd(LongTermAlmanac.eps) - math.tan(beta) * mu.sind(LongTermAlmanac.eps)), math.cos(_lambda))))

        # Declination of Jupiter, apparent
        LongTermAlmanac.DECJupiter = math.degrees(math.asin(math.sin(beta) * mu.cosd(LongTermAlmanac.eps) + math.cos(beta) * mu.sind(LongTermAlmanac.eps) * math.sin(_lambda)))

        # GHA of Jupiter
        LongTermAlmanac.GHAJupiter = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RAJupiter)

        # Semi-diameter of Jupiter (equatorial)
        LongTermAlmanac.SDJupiter = 98.44 / d

        # Horizontal parallax of Jupiter
        LongTermAlmanac.HPJupiter = 8.794 / d

        # Illumination of the planet's disk
        k = 100 * (1 + ((R - LongTermAlmanac.Re * mu.cosd(B) * mu.cosd(L - LongTermAlmanac.Le)) / d)) / 2
        LongTermAlmanac.illumJupiter = round(10 * k) / 10

        return

    # Calculations for Saturn
    @staticmethod
    def calculateSaturn():
        # Heliocentric coordinates
        L = Saturn.l_saturn(LongTermAlmanac.Tau)
        B = Saturn.b_saturn(LongTermAlmanac.Tau)
        R = Saturn.r_saturn(LongTermAlmanac.Tau)

        # Rectangular coordinates
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        # Geocentric coordinates
        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # Distance from earth / light time
        d = math.sqrt(x * x + y * y + z * z)
        lt = 0.0057755183 * d

        # Time correction
        Tau_corr = (LongTermAlmanac.JDE - lt - 2451545) / 365250

        # Coordinates corrected for light time
        L = Saturn.l_saturn(Tau_corr)
        B = Saturn.b_saturn(Tau_corr)
        R = Saturn.r_saturn(Tau_corr)
        x = R * mu.cosd(B) * mu.cosd(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.cosd(LongTermAlmanac.Le)
        y = R * mu.cosd(B) * mu.sind(L) - LongTermAlmanac.Re * mu.cosd(LongTermAlmanac.Be) * mu.sind(LongTermAlmanac.Le)
        z = R * mu.sind(B) - LongTermAlmanac.Re * mu.sind(LongTermAlmanac.Be)

        _lambda = math.atan2(y, x)
        beta = math.atan(z / math.sqrt(x * x + y * y))

        # aberration
        dlambda = (LongTermAlmanac.e * LongTermAlmanac.kappa * math.cos(LongTermAlmanac.pi0 - _lambda) - LongTermAlmanac.kappa * math.cos( math.radians(LongTermAlmanac.Lsun_true) - _lambda)) / math.cos(beta)
        dbeta = -LongTermAlmanac.kappa * math.sin(beta) * (math.sin( math.radians(LongTermAlmanac.Lsun_true) - _lambda) - LongTermAlmanac.e * math.sin(LongTermAlmanac.pi0 - _lambda))

        _lambda += dlambda
        beta += dbeta

        # FK5
        lambda_prime = _lambda - math.radians(1.397) * LongTermAlmanac.TE - math.radians(0.00031) * LongTermAlmanac.TE2
        dlambda = math.radians(-0.09033) / 3600 + math.radians(0.03916) / 3600 * (math.cos(lambda_prime) + math.sin(lambda_prime)) * math.tan(beta)
        dbeta = math.radians(0.03916) / 3600 * (math.cos(lambda_prime) - math.sin(lambda_prime))

        _lambda += dlambda
        beta += dbeta

        # calculateNutation in longitude
        _lambda += math.radians(LongTermAlmanac.deltaPsi)

        # Right ascension, apparent
        LongTermAlmanac.RASaturn = math.degrees(mu.norm2_pi_rad(math.atan2((math.sin(_lambda) * mu.cosd(LongTermAlmanac.eps) - math.tan(beta) * mu.sind(LongTermAlmanac.eps)), math.cos(_lambda))))

        # Declination of Saturn, apparent
        LongTermAlmanac.DECSaturn = math.degrees(math.asin(math.sin(beta) * mu.cosd(LongTermAlmanac.eps) + math.cos(beta) * mu.sind(LongTermAlmanac.eps) * math.sin(_lambda)))

        # GHA of Saturn
        LongTermAlmanac.GHASaturn = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RASaturn)

        # Semi-diameter of Saturn (equatorial)
        LongTermAlmanac.SDSaturn = 82.73 / d

        # Horizontal parallax of Saturn
        LongTermAlmanac.HPSaturn = 8.794 / d

        # Illumination of the planet's disk
        k = 100 * (1 + ((R - LongTermAlmanac.Re * mu.cosd(B) * mu.cosd(L - LongTermAlmanac.Le)) / d)) / 2
        LongTermAlmanac.illumSaturn = round(10 * k) / 10

        return

    # Calculations for the moon
    @staticmethod
    def calculateMoon():
        # Mean longitude of the moon
        Lmm = mu.norm_360_deg(218.3164591 + 481267.88134236 * LongTermAlmanac.TE - 0.0013268 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 538841 - LongTermAlmanac.TE4 / 65194000)
    
        # Mean elongation of the moon
        D = mu.norm_360_deg(297.8502042 + 445267.1115168 * LongTermAlmanac.TE - 0.00163 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 545868 - LongTermAlmanac.TE4 / 113065000)
    
        # Mean anomaly of the sun
        Msm = mu.norm_360_deg(357.5291092 + 35999.0502909 * LongTermAlmanac.TE - 0.0001536 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 24490000)
    
        # Mean anomaly of the moon
        Mmm = mu.norm_360_deg(134.9634114 + 477198.8676313 * LongTermAlmanac.TE + 0.008997 * LongTermAlmanac.TE2 + LongTermAlmanac.TE3 / 69699 - LongTermAlmanac.TE4 / 14712000)
    
        # Mean distance of the moon from ascending node
        F = mu.norm_360_deg(93.2720993 + 483202.0175273 * LongTermAlmanac.TE - 0.0034029 * LongTermAlmanac.TE2 - LongTermAlmanac.TE3 / 3526000 + LongTermAlmanac.TE4 / 863310000)
    
        # Corrections
        A1 = mu.norm_360_deg(119.75 + 131.849 * LongTermAlmanac.TE)
        A2 = mu.norm_360_deg(53.09 + 479264.29 * LongTermAlmanac.TE)
        A3 = mu.norm_360_deg(313.45 + 481266.484 * LongTermAlmanac.TE)
        fE = 1 - 0.002516 * LongTermAlmanac.TE - 0.0000074 * LongTermAlmanac.TE2
        fE2 = fE * fE
    
        # Periodic terms for the moon:
    
        # Longitude and distance
        ld = [
            [ 0,  0,  1,  0, 6288774, -20905355 ],
            [ 2,  0, -1,  0, 1274027,  -3699111 ],
            [ 2,  0,  0,  0,  658314,  -2955968 ],
            [ 0,  0,  2,  0,  213618,   -569925 ],
            [ 0,  1,  0,  0, -185116,     48888 ],
            [ 0,  0,  0,  2, -114332,     -3149 ],
            [ 2,  0, -2,  0,   58793,    246158 ],
            [ 2, -1, -1,  0,   57066,   -152138 ],
            [ 2,  0,  1,  0,   53322,   -170733 ],
            [ 2, -1,  0,  0,   45758,   -204586 ],
            [ 0,  1, -1,  0,  -40923,   -129620 ],
            [ 1,  0,  0,  0,  -34720,    108743 ],
            [ 0,  1,  1,  0,  -30383,    104755 ],
            [ 2,  0,  0, -2,   15327,     10321 ],
            [ 0,  0,  1,  2,  -12528,         0 ],
            [ 0,  0,  1, -2,   10980,     79661 ],
            [ 4,  0, -1,  0,   10675,    -34782 ],
            [ 0,  0,  3,  0,   10034,    -23210 ],
            [ 4,  0, -2,  0,    8548,    -21636 ],
            [ 2,  1, -1,  0,   -7888,     24208 ],
            [ 2,  1,  0,  0,   -6766,     30824 ],
            [ 1,  0, -1,  0,   -5163,     -8379 ],
            [ 1,  1,  0,  0,    4987,    -16675 ],
            [ 2, -1,  1,  0,    4036,    -12831 ],
            [ 2,  0,  2,  0,    3994,    -10445 ],
            [ 4,  0,  0,  0,    3861,    -11650 ],
            [ 2,  0, -3,  0,    3665,     14403 ],
            [ 0,  1, -2,  0,   -2689,     -7003 ],
            [ 2,  0, -1,  2,   -2602,         0 ],
            [ 2, -1, -2,  0,    2390,     10056 ],
            [ 1,  0,  1,  0,   -2348,      6322 ],
            [ 2, -2,  0,  0,    2236,     -9884 ],
            [ 0,  1,  2,  0,   -2120,      5751 ],
            [ 0,  2,  0,  0,   -2069,         0 ],
            [ 2, -2, -1,  0,    2048,     -4950 ],
            [ 2,  0,  1, -2,   -1773,      4130 ],
            [ 2,  0,  0,  2,   -1595,         0 ],
            [ 4, -1, -1,  0,    1215,     -3958 ],
            [ 0,  0,  2,  2,   -1110,         0 ],
            [ 3,  0, -1,  0,    -892,      3258 ],
            [ 2,  1,  1,  0,    -810,      2616 ],
            [ 4, -1, -2,  0,     759,     -1897 ],
            [ 0,  2, -1,  0,    -713,     -2117 ],
            [ 2,  2, -1,  0,    -700,      2354 ],
            [ 2,  1, -2,  0,     691,         0 ],
            [ 2, -1,  0, -2,     596,         0 ],
            [ 4,  0,  1,  0,     549,     -1423 ],
            [ 0,  0,  4,  0,     537,     -1117 ],
            [ 4, -1,  0,  0,     520,     -1571 ],
            [ 1,  0, -2,  0,    -487,     -1739 ],
            [ 2,  1,  0, -2,    -399,         0 ],
            [ 0,  0,  2, -2,    -381,     -4421 ],
            [ 1,  1,  1,  0,     351,         0 ],
            [ 3,  0, -2,  0,    -340,         0 ],
            [ 4,  0, -3,  0,     330,         0 ],
            [ 2, -1,  2,  0,     327,         0 ],
            [ 0,  2,  1,  0,    -323,      1165 ],
            [ 1,  1, -1,  0,     299,         0 ],
            [ 2,  0,  3,  0,     294,         0 ],
            [ 2,  0, -1, -2,       0,      8752 ]
        ]
    
        lat = [
            [ 0,  0,  0,  1, 5128122 ],
            [ 0,  0,  1,  1,  280602 ],
            [ 0,  0,  1, -1,  277693 ],
            [ 2,  0,  0, -1,  173237 ],
            [ 2,  0, -1,  1,   55413 ],
            [ 2,  0, -1, -1,   46271 ],
            [ 2,  0,  0,  1,   32573 ],
            [ 0,  0,  2,  1,   17198 ],
            [ 2,  0,  1, -1,    9266 ],
            [ 0,  0,  2, -1,    8822 ],
            [ 2, -1,  0, -1,    8216 ],
            [ 2,  0, -2, -1,    4324 ],
            [ 2,  0,  1,  1,    4200 ],
            [ 2,  1,  0, -1,   -3359 ],
            [ 2, -1, -1,  1,    2463 ],
            [ 2, -1,  0,  1,    2211 ],
            [ 2, -1, -1, -1,    2065 ],
            [ 0,  1, -1, -1,   -1870 ],
            [ 4,  0, -1, -1,    1828 ],
            [ 0,  1,  0,  1,   -1794 ],
            [ 0,  0,  0,  3,   -1749 ],
            [ 0,  1, -1,  1,   -1565 ],
            [ 1,  0,  0,  1,   -1491 ],
            [ 0,  1,  1,  1,   -1475 ],
            [ 0,  1,  1, -1,   -1410 ],
            [ 0,  1,  0, -1,   -1344 ],
            [ 1,  0,  0, -1,   -1335 ],
            [ 0,  0,  3,  1,    1107 ],
            [ 4,  0,  0, -1,    1021 ],
            [ 4,  0, -1,  1,     833 ],
            [ 0,  0,  1, -3,     777 ],
            [ 4,  0, -2,  1,     671 ],
            [ 2,  0,  0, -3,     607 ],
            [ 2,  0,  2, -1,     596 ],
            [ 2, -1,  1, -1,     491 ],
            [ 2,  0, -2,  1,    -451 ],
            [ 0,  0,  3, -1,     439 ],
            [ 2,  0,  2,  1,     422 ],
            [ 2,  0, -3, -1,     421 ],
            [ 2,  1, -1,  1,    -366 ],
            [ 2,  1,  0,  1,    -351 ],
            [ 4,  0,  0,  1,     331 ],
            [ 2, -1,  1,  1,     315 ],
            [ 2, -2,  0, -1,     302 ],
            [ 0,  0,  1,  3,    -283 ],
            [ 2,  1,  1, -1,    -229 ],
            [ 1,  1,  0, -1,     223 ],
            [ 1,  1,  0,  1,     223 ],
            [ 0,  1, -2, -1,    -220 ],
            [ 2,  1, -1, -1,    -220 ],
            [ 1,  0,  1,  1,    -185 ],
            [ 2, -1, -2, -1,     181 ],
            [ 0,  1,  2,  1,    -177 ],
            [ 4,  0, -2, -1,     176 ],
            [ 4, -1, -1, -1,     166 ],
            [ 1,  0,  1, -1,    -164 ],
            [ 4,  0,  1, -1,     132 ],
            [ 1,  0, -1, -1,    -119 ],
            [ 4, -1,  0, -1,     115 ],
            [ 2, -2,  0,  1,     107 ]
        ]
    
        # Reading periodic terms
        fD = 0
        fD2 = 0
        fM = 0
        fM2 = 0
        fMm = 0
        fMm2 = 0
        fF = 0
        fF2 = 0
        coeffs = 0
        coeffs2 = 0
        coeffc = 0
        f = 0
        f2 = 0
        sumL = 0
        sumR = 0
        sumB = 0
        x = 0
    
        while x < len(lat):
            fD = ld[x][0]
            fM = ld[x][1]
            fMm = ld[x][2]
            fF = ld[x][3]
            coeffs = ld[x][4]
            coeffc = ld[x][5]
            if fM == 1 or fM == -1:
                f = fE
            elif fM == 2 or fM == -2:
                f = fE2
            else:
                f = 1
    
            sumL += f * coeffs * mu.sind(fD * D + fM * Msm + fMm * Mmm + fF * F)
            sumR += f * coeffc * mu.cosd(fD * D + fM * Msm + fMm * Mmm + fF * F)
            fD2 = lat[x][0]
            fM2 = lat[x][1]
            fMm2 = lat[x][2]
            fF2 = lat[x][3]
            coeffs2 = lat[x][4]
            if fM2 == 1 or fM2 == -1:
                f2 = fE
            elif fM2 == 2 or fM2 == -2:
                f2 = fE2
            else:
                f2 = 1
    
            sumB += f2 * coeffs2 * mu.sind(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F)
            x += 1
    
    
        # Corrections
        sumL = sumL + 3958 * mu.sind(A1) + 1962 * mu.sind(Lmm - F) + 318 * mu.sind(A2)
        sumB = sumB - 2235 * mu.sind(Lmm) + 382 * mu.sind(A3) + 175 * mu.sind(A1 - F) + 175 * mu.sind(A1 + F) + 127 * mu.sind(Lmm - Mmm) - 115 * mu.sind(Lmm + Mmm)
    
        # Longitude of the moon
        lambdaMm = mu.norm_360_deg(Lmm + sumL / 1000000)
    
        # Latitude of the moon
        betaM = sumB / 1000000
    
        # Distance earth-moon
        dEM = 385000.56 + sumR / 1000
    
        # Apparent longitude of the moon
        LongTermAlmanac.lambdaMapp = lambdaMm + LongTermAlmanac.deltaPsi
    
        # Right ascension of the moon, apparent
        LongTermAlmanac.RAMoon = math.degrees(mu.norm2_pi_rad(math.atan2((mu.sind(LongTermAlmanac.lambdaMapp) * mu.cosd(LongTermAlmanac.eps) - mu.tand(betaM) * mu.sind(LongTermAlmanac.eps)), mu.cosd(LongTermAlmanac.lambdaMapp))))
    
        # Declination of the moon
        LongTermAlmanac.DECMoon = math.degrees(math.asin(mu.sind(betaM) * mu.cosd(LongTermAlmanac.eps) + mu.cosd(betaM) * mu.sind(LongTermAlmanac.eps) * mu.sind(LongTermAlmanac.lambdaMapp)))
    
        # GHA of the moon
        LongTermAlmanac.GHAMoon = mu.norm_360_deg(LongTermAlmanac.GHAAtrue - LongTermAlmanac.RAMoon)
    
        # Horizontal parallax of the moon
        LongTermAlmanac.HPMoon = math.degrees(3600 * math.asin(6378.14 / dEM))
    
        # Semi-diameter of the moon
        LongTermAlmanac.SDMoon = math.degrees(3600 * math.asin(1738 / dEM))
    
        # Geocentric angular distance between moon and sun
        LongTermAlmanac.LDist = math.degrees(math.acos(mu.sind(LongTermAlmanac.DECMoon) * mu.sind(LongTermAlmanac.DECSun) + mu.cosd(LongTermAlmanac.DECMoon) * mu.cosd(LongTermAlmanac.DECSun) * mu.cosd(LongTermAlmanac.RAMoon - LongTermAlmanac.RASun)))
    
        # Phase angle
        i = math.atan2(LongTermAlmanac.dES * mu.sind(LongTermAlmanac.LDist), (dEM - LongTermAlmanac.dES * mu.cosd(LongTermAlmanac.LDist)))
    
        # Illumination of the moon's disk
        k = 100 * (1 + math.cos(i)) / 2
        LongTermAlmanac.illumMoon = round(10 * k) / 10

        return

    # Ephemerides of Polaris
    @staticmethod
    def calculatePolaris():
        # Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
        RApol0 = 37.95293333
        DECpol0 = 89.26408889

        # Proper motion per year
        dRApol = 2.98155 / 3600
        dDECpol = -0.0152 / 3600

        # Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
        RApol1 = RApol0 + 100 * LongTermAlmanac.TE * dRApol
        DECpol1 = DECpol0 + 100 * LongTermAlmanac.TE * dDECpol

        # Mean obliquity of ecliptic at 2000.0 in degrees
        eps0_2000 = 23.439291111

        # Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
        lambdapol1 = math.atan2((mu.sind(RApol1) * mu.cosd(eps0_2000) + mu.tand(DECpol1) * mu.sind(eps0_2000)), mu.cosd(RApol1))
        betapol1 = math.asin(mu.sind(DECpol1) * mu.cosd(eps0_2000) - mu.cosd(DECpol1) * mu.sind(eps0_2000) * mu.sind(RApol1))

        # Precession
        eta = math.radians(47.0029 * LongTermAlmanac.TE - 0.03302 * LongTermAlmanac.TE2 + 0.00006 * LongTermAlmanac.TE3) / 3600
        PI0 = math.radians(174.876384 - (869.8089 * LongTermAlmanac.TE + 0.03536 * LongTermAlmanac.TE2) / 3600)
        p0 = math.radians(5029.0966 * LongTermAlmanac.TE + 1.11113 * LongTermAlmanac.TE2 - 0.0000006 * LongTermAlmanac.TE3) / 3600

        A1 = math.cos(eta) * math.cos(betapol1) * math.sin(PI0 - lambdapol1) - math.sin(eta) * math.sin(betapol1)
        B1 = math.cos(betapol1) * math.cos(PI0 - lambdapol1)
        C1 = math.cos(eta) * math.sin(betapol1) + math.sin(eta) * math.cos(betapol1) * math.sin(PI0 - lambdapol1)
        lambdapol2 = p0 + PI0 - math.atan2(A1, B1)
        betapol2 = math.asin(C1)

        # calculateNutation in longitude
        lambdapol2 +=  math.radians(LongTermAlmanac.deltaPsi)

        # aberration
        dlambdapol = (LongTermAlmanac.e * LongTermAlmanac.kappa * math.cos(LongTermAlmanac.pi0 - lambdapol2) - LongTermAlmanac.kappa * math.cos( math.radians(LongTermAlmanac.Lsun_true) - lambdapol2)) / math.cos(betapol2)
        dbetapol = -LongTermAlmanac.kappa * math.sin(betapol2) * (math.sin(math.radians(LongTermAlmanac.Lsun_true) - lambdapol2) - LongTermAlmanac.e * math.sin(LongTermAlmanac.pi0 - lambdapol2))

        lambdapol2 += dlambdapol
        betapol2 += dbetapol

        # Transformation back to equatorial coordinates in radians
        RApol2 = math.atan2((math.sin(lambdapol2) * mu.cosd(LongTermAlmanac.eps) - math.tan(betapol2) * mu.sind(LongTermAlmanac.eps)), math.cos(lambdapol2))
        DECpol2 = math.asin(math.sin(betapol2) * mu.cosd(LongTermAlmanac.eps) + math.cos(betapol2) * mu.sind(LongTermAlmanac.eps) * math.sin(lambdapol2))

        # Finals
        LongTermAlmanac.GHAPol = LongTermAlmanac.GHAAtrue - math.degrees(RApol2)
        LongTermAlmanac.GHAPol = mu.norm_360_deg(LongTermAlmanac.GHAPol)
        LongTermAlmanac.RAPol = math.degrees(RApol2)
        LongTermAlmanac.DECPol = math.degrees(DECpol2)

        return

    # Calculation of the phase of the Moon
    @staticmethod
    def calculateMoonPhase():
        x = LongTermAlmanac.lambdaMapp - LongTermAlmanac.lambdaSun
        x = mu.norm_360_deg(x)
        x = round(10 * x) / 10
        LongTermAlmanac.moonPhaseAngle = x
        if x == 0:
            LongTermAlmanac.moonPhase = " New"

        if 0 < x < 90:
            LongTermAlmanac.moonPhase = " +cre"

        if x == 90:
            LongTermAlmanac.moonPhase = " FQ"

        if 90 < x < 180:
            LongTermAlmanac.moonPhase = " +gib"

        if x == 180:
            LongTermAlmanac.moonPhase = " Full"

        if 180 < x < 270:
            LongTermAlmanac.moonPhase = " -gib"

        if x == 270:
            LongTermAlmanac.moonPhase = " LQ"

        if 270 < x < 360:
            LongTermAlmanac.moonPhase = " -cre"

        return

    DAYS = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"]

    # Day of the week
    @staticmethod
    def calculateWeekDay():
        LongTermAlmanac.JD0h += 1.5
        res = LongTermAlmanac.JD0h - 7 * math.floor(LongTermAlmanac.JD0h / 7)
        LongTermAlmanac.DoW = LongTermAlmanac.DAYS[int(res)]
