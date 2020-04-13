#!/usr/bin/env python3

from long_term_almanac import LongTermAlmanac as lta

DELTA_T = 69.2201
# 2020-MAR-28 16:50:20 UTC
lta.calculate(2020, 3, 28, 16, 50, 20, DELTA_T)

# Display results
print("----------------------------------------------")
print("Calculations done for 2020-Mar-28 16:50:20 UTC")
print("----------------------------------------------")

print("Sideral Time: {}".format(lta.SidTm))

# Sun
fmtGHASun = lta.outHA(lta.GHASun)
fmtRASun = lta.outRA(lta.RASun)
fmtDECSun = lta.outDec(lta.DECSun)
fmtSDSun = lta.outSdHp(lta.SDSun)
fmtHPSun = lta.outSdHp(lta.HPSun)

print("Sun: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHASun, fmtRASun, fmtDECSun, fmtSDSun, fmtHPSun))

# Venus
fmtGHAVenus = lta.outHA(lta.GHAVenus)
fmtRAVenus = lta.outRA(lta.RAVenus)
fmtDECVenus = lta.outDec(lta.DECVenus)
fmtSDVenus = lta.outSdHp(lta.SDVenus)
fmtHPVenus = lta.outSdHp(lta.HPVenus)

print("Venus: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAVenus, fmtRAVenus, fmtDECVenus, fmtSDVenus, fmtHPVenus))

# Mars
fmtGHAMars = lta.outHA(lta.GHAMars)
fmtRAMars = lta.outRA(lta.RAMars)
fmtDECMars = lta.outDec(lta.DECMars)
fmtSDMars = lta.outSdHp(lta.SDMars)
fmtHPMars = lta.outSdHp(lta.HPMars)

print("Mars: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAMars, fmtRAMars, fmtDECMars, fmtSDMars, fmtHPMars))

# Jupiter
fmtGHAJupiter = lta.outHA(lta.GHAJupiter)
fmtRAJupiter = lta.outRA(lta.RAJupiter)
fmtDECJupiter = lta.outDec(lta.DECJupiter)
fmtSDJupiter = lta.outSdHp(lta.SDJupiter)
fmtHPJupiter = lta.outSdHp(lta.HPJupiter)

print("Jupiter: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAJupiter, fmtRAJupiter, fmtDECJupiter, fmtSDJupiter, fmtHPJupiter))

# Saturn
fmtGHASaturn = lta.outHA(lta.GHASaturn)
fmtRASaturn = lta.outRA(lta.RASaturn)
fmtDECSaturn = lta.outDec(lta.DECSaturn)
fmtSDSaturn = lta.outSdHp(lta.SDSaturn)
fmtHPSaturn = lta.outSdHp(lta.HPSaturn)

print("Saturn: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHASaturn, fmtRASaturn, fmtDECSaturn, fmtSDSaturn, fmtHPSaturn))

# Moon
fmtGHAMoon = lta.outHA(lta.GHAMoon)
fmtRAMoon = lta.outRA(lta.RAMoon)
fmtDECMoon = lta.outDec(lta.DECMoon)
fmtSDMoon = lta.outSdHp(lta.SDMoon)
fmtHPMoon = lta.outSdHp(lta.HPMoon)

print("Moon: GHA {}, RA {}, DEC {}, sd {}, hp {}".format(fmtGHAMoon, fmtRAMoon, fmtDECMoon, fmtSDMoon, fmtHPMoon))
print("\tMoon phase {} -> {}".format(lta.moonPhaseAngle, lta.moonPhase))

# Polaris
fmtGHAPolaris = lta.outHA(lta.GHAPol)
fmtRAPolaris = lta.outRA(lta.RAPol)
fmtDECPolaris = lta.outDec(lta.DECPol)

print("Polaris: GHA {}, RA {}, DEC {}".format(fmtGHAPolaris, fmtRAPolaris, fmtDECPolaris))

# Obliquity of Ecliptic
OoE = lta.outECL(lta.eps0)
tOoE = lta.outECL(lta.eps)

print("Ecliptic: obliquity {}, true {}".format(OoE, tOoE))

# Equation of time
fmtEoT = lta.outEoT(lta.EoT)
print("Equation of time {}".format(fmtEoT))

# Lunar Distance of Sun
fmtLDist = lta.outHA(lta.LDist)
print("Lunar Distance: {}".format(fmtLDist))

print("Day of Week: {}".format(lta.DoW))
