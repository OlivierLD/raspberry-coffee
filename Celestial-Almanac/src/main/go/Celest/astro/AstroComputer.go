package astro

import (
  "fmt"
  "math"
)

var (
  DEBUG bool = false
)

type ComputedData struct {
  T,
  T2,
  T3,
  T4,
  T5,
  TE,
  TE2,
  TE3,
  TE4,
  TE5,
  Tau,
  Tau2,
  Tau3,
  Tau4,
  Tau5,
  DeltaT,
  Eps0,
  Eps,
  deltaPsi,
  deltaEps,
  Le,
  Be,
  Re,
  kappa,
  pi0,
  e,
  lambdaSun,
  RASun,
  DECSun,
  GHASun,
  SDSun,
  HPSun,
  EoT,
  fmtEoT,
  EoE,
  EoEout,
  Lsun_true,
  Lsun_prime,
  dES,
  dayFraction,
  GHAAmean,
  RAVenus,
  DECVenus,
  GHAVenus,
  SDVenus,
  HPVenus,
  RAMars,
  DECMars,
  GHAMars,
  SDMars,
  HPMars,
  RAJupiter,
  DECJupiter,
  GHAJupiter,
  SDJupiter,
  HPJupiter,
  RASaturn,
  DECSaturn,
  GHASaturn,
  SDSaturn,
  HPSaturn,
  RAMoon,
  DECMoon,
  GHAMoon,
  SDMoon,
  HPMoon,
  RAPol,
  DECPol,
  GHAPol,
  OoE,
  tOoE,
  LDist,
  JD0h,
  JD,
  JDE,
  lambdaMapp,
  GHAAtrue,
  MoonPhaseAngle,
  IllumMoon,
  IllumVenus,
  IllumMars,
  IllumJupiter,
  IllumSaturn float64
  MoonPhase, SidTa, SidTm, DoW string
}

func IsLeapYear(year int) bool {
  var ly = false
  if year%4 == 0 {
    ly = true
  }
  if year%100 == 0 {
    ly = false
  }
  if year%400 == 0 {
    ly = true
  }
  return ly
}

// string, dataBuffer

// Output Sidereal Time
func OutSideralTime(x float64) string {
  GMSTdecimal := x / 15
  GMSTh := math.Floor(GMSTdecimal)
  GMSTmdecimal := 60 * (GMSTdecimal - GMSTh)
  GMSTm := math.Floor(GMSTmdecimal)
  GMSTsdecimal := 60 * (GMSTmdecimal - GMSTm)
  GMSTs := math.Round(1000*GMSTsdecimal) / 1000
  data := fmt.Sprintf("%vh %vm %vs", GMSTh, GMSTm, GMSTs)
  return data
}

// Output Hour Angles
func OutHA(x float64) string {
  if DEBUG {
    fmt.Printf("Output HA for %f\n", x)
  }
  GHAdeg := math.Floor(x)
  GHAmin := math.Floor(60 * (x - GHAdeg))
  GHAsec := math.Round(3600 * ((x - GHAdeg) - (GHAmin / 60)))
  if GHAsec == 60 {
    GHAsec = 0
    GHAmin += 1
  }
  if GHAmin == 60 {
    GHAmin = 0
    GHAdeg += 1
  }
  data := fmt.Sprintf("%v° %v' %v\"", GHAdeg, GHAmin, GHAsec)
  return data
}

// Output Right ascension
func OutRA(x float64) string {
  if DEBUG {
    fmt.Printf("Output RA for %f\n", x)
  }
  t := x / 15
  RAh := math.Floor(t)
  RAmin := math.Floor(60 * (t - RAh))
  RAsec := math.Round(10*(3600*((t-RAh)-(RAmin/60)))) / 10
  if RAsec == 60 {
    RAsec = 0
    RAmin += 1
  }

  if RAmin == 60 {
    RAmin = 0
    RAh += 1
  }
  data := fmt.Sprintf("%vh %vm %vs", RAh, RAmin, RAsec)
  return data
}

func OutEoT(x float64) string {
  EoT := math.Abs(x)
  EOTmin := math.Floor(EoT)
  EOTsec := math.Round(600*(EoT-EOTmin)) / 10

  sign := ""
  if x < 0 {
    sign = "-"
  } else {
    sign = "+"
  }
  if EOTmin == 0 {
    return fmt.Sprintf("%s %vs", sign, EOTsec)
  } else {
    return fmt.Sprintf("%s %vm %vs", sign, EOTmin, EOTsec)
  }
}

func OutDec(x float64) string {
  DEC := math.Abs(x)
  DECdeg := math.Floor(DEC)
  DECmin := math.Floor(60 * (DEC - DECdeg))
  DECsec := math.Round(3600 * ((DEC - DECdeg) - (DECmin / 60)))
  if DECsec == 60 {
    DECsec = 0
    DECmin += 1
  }
  if DECmin == 60 {
    DECmin = 0
    DECdeg += 1
  }
  sign := ""
  if x < 0 {
    sign = "S"
  } else {
    sign = "N"
  }
  return fmt.Sprintf("%s %v°%v'%v\"", sign, DECdeg, DECmin, DECsec)
}

// Output SD and HP
func OutSdHp(x float64) string {
	x = math.Round(10 * x) / 10
	return fmt.Sprintf("%f\"", x)
}

// Output Obliquity of Ecliptic
func OutECL(x float64) string {
	ECLdeg := math.Floor(x)
	ECLmin := math.Floor(60 * (x - ECLdeg))
	ECLsec := math.Round(3600000 * ((x - ECLdeg) - (ECLmin / 60))) / 1000
	if ECLsec == 60 {
		ECLsec = 0
		ECLmin += 1
	}
	if ECLmin == 60 {
		ECLmin = 0
		ECLdeg += 1
	}
	return fmt.Sprintf("%v° %v' %f\"", ECLdeg, ECLmin, ECLsec)
}

// Placeholder for all computed data
var data = ComputedData{}

/**
 * Input data conversion and reworking
 * All data are UTC data (except deltaT)
 **
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
func calculateJulianDate(year int, month int, day int, hour int, minute int, second int, delta_t float64) {

  data.dayFraction = (float64(hour) + float64(minute) / 60.0 + float64(second) / 3600.0) / 24.0
  if (data.dayFraction < 0 || data.dayFraction > 1) {
    fmt.Println("Time out of range! Restart calculation.")
    return
  }
  data.DeltaT = delta_t

  // Calculating Julian date, century, and millennium

  // Julian date (UT1)
  if (month <= 2) {
    year -= 1
    month += 12
  }
  A := math.Floor(float64(year) / 100)
  B := 2 - A + math.Floor(A / 4)
  data.JD0h = math.Floor(365.25 * float64(year + 4716)) + math.Floor(30.6001 * float64(month + 1)) + float64(day) + B - 1524.5

  data.JD = data.JD0h + data.dayFraction

  // Julian centuries (UT1) from 2000 January 0.5
  data.T = (data.JD - 2451545) / 36525
  data.T2 = data.T * data.T
  data.T3 = data.T * data.T2
  data.T4 = data.T * data.T3
  data.T5 = data.T * data.T4

  // Julian ephemeris date (TDT)
  data.JDE = data.JD + data.DeltaT / 86400

  // Julian centuries (TDT) from 2000 January 0.5
  data.TE = (data.JDE - 2451545) / 36525
  data.TE2 = data.TE * data.TE
  data.TE3 = data.TE * data.TE2
  data.TE4 = data.TE * data.TE3
  data.TE5 = data.TE * data.TE4

  // Julian millenniums (TDT) from 2000 January 0.5
  data.Tau = 0.1 * data.TE
  data.Tau2 = data.Tau * data.Tau
  data.Tau3 = data.Tau * data.Tau2
  data.Tau4 = data.Tau * data.Tau3
  data.Tau5 = data.Tau * data.Tau4

  if (DEBUG) {
    fmt.Printf("DayFraction: %f, JD0h: %f, JD: %f\n", data.dayFraction, data.JD0h, data.JD)
  }
}

// Nutation, obliquity of the ecliptic
func calculateNutation() {
  // IAU 1980 calculateNutation theory:

  // Mean anomaly of the Moon
  Mm := 134.962981389 + 198.867398056 * data.TE + Norm360Deg(477000 * data.TE) + 0.008697222222 * data.TE2 + data.TE3 / 56250

  // Mean anomaly of the Sun
  M := 357.527723333 + 359.05034 * data.TE + Norm360Deg(35640 * data.TE) - 0.0001602777778 * data.TE2 - data.TE3 / 300000

  // Mean distance of the Moon from ascending node
  F := 93.271910277 + 82.017538055 * data.TE + Norm360Deg(483120 * data.TE) - 0.0036825 * data.TE2 + data.TE3 / 327272.7273

  // Mean elongation of the Moon
  D := 297.850363055 + 307.11148 * data.TE + Norm360Deg(444960 * data.TE) - 0.001914166667 * data.TE2 + data.TE3 / 189473.6842

  // Longitude of the ascending node of the Moon
  omega := 125.044522222 - 134.136260833 * data.TE - Norm360Deg(1800 * data.TE) + 0.002070833333 * data.TE2 + data.TE3 / 450000

  // Periodic terms for nutation
  const nutRows int = 106
  const nutColumns int = 9
  // Why a var and not a const ?????
  var nut = [nutRows][nutColumns] float64 {  // [][]
    {  0,  0,  0,  0,  1, -171996, -174.2,  92025,  8.9 },
    {  0,  0,  2, -2,  2,  -13187,   -1.6,   5736, -3.1 },
    {  0,  0,  2,  0,  2,   -2274,   -0.2,    977, -0.5 },
    {  0,  0,  0,  0,  2,    2062,    0.2,   -895,  0.5 },
    {  0, -1,  0,  0,  0,   -1426,    3.4,     54, -0.1 },
    {  1,  0,  0,  0,  0,     712,    0.1,     -7,  0.0 },
    {  0,  1,  2, -2,  2,    -517,    1.2,    224, -0.6 },
    {  0,  0,  2,  0,  1,    -386,   -0.4,    200,  0.0 },
    {  1,  0,  2,  0,  2,    -301,    0.0,    129, -0.1 },
    {  0, -1,  2, -2,  2,     217,   -0.5,    -95,  0.3 },
    { -1,  0,  0,  2,  0,     158,    0.0,     -1,  0.0 },
    {  0,  0,  2, -2,  1,     129,    0.1,    -70,  0.0 },
    { -1,  0,  2,  0,  2,     123,    0.0,    -53,  0.0 },
    {  1,  0,  0,  0,  1,      63,    0.1,    -33,  0.0 },
    {  0,  0,  0,  2,  0,      63,    0.0,     -2,  0.0 },
    { -1,  0,  2,  2,  2,     -59,    0.0,     26,  0.0 },
    { -1,  0,  0,  0,  1,     -58,   -0.1,     32,  0.0 },
    {  1,  0,  2,  0,  1,     -51,    0.0,     27,  0.0 },
    { -2,  0,  0,  2,  0,     -48,    0.0,      1,  0.0 },
    { -2,  0,  2,  0,  1,      46,    0.0,    -24,  0.0 },
    {  0,  0,  2,  2,  2,     -38,    0.0,     16,  0.0 },
    {  2,  0,  2,  0,  2,     -31,    0.0,     13,  0.0 },
    {  2,  0,  0,  0,  0,      29,    0.0,     -1,  0.0 },
    {  1,  0,  2, -2,  2,      29,    0.0,    -12,  0.0 },
    {  0,  0,  2,  0,  0,      26,    0.0,     -1,  0.0 },
    {  0,  0,  2, -2,  0,     -22,    0.0,      0,  0.0 },
    { -1,  0,  2,  0,  1,      21,    0.0,    -10,  0.0 },
    {  0,  2,  0,  0,  0,      17,   -0.1,      0,  0.0 },
    {  0,  2,  2, -2,  2,     -16,    0.1,      7,  0.0 },
    { -1,  0,  0,  2,  1,      16,    0.0,     -8,  0.0 },
    {  0,  1,  0,  0,  1,     -15,    0.0,      9,  0.0 },
    {  1,  0,  0, -2,  1,     -13,    0.0,      7,  0.0 },
    {  0, -1,  0,  0,  1,     -12,    0.0,      6,  0.0 },
    {  2,  0, -2,  0,  0,      11,    0.0,      0,  0.0 },
    { -1,  0,  2,  2,  1,     -10,    0.0,      5,  0.0 },
    {  1,  0,  2,  2,  2,      -8,    0.0,      3,  0.0 },
    {  0, -1,  2,  0,  2,      -7,    0.0,      3,  0.0 },
    {  0,  0,  2,  2,  1,      -7,    0.0,      3,  0.0 },
    {  1,  1,  0, -2,  0,      -7,    0.0,      0,  0.0 },
    {  0,  1,  2,  0,  2,       7,    0.0,     -3,  0.0 },
    { -2,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 },
    {  0,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 },
    {  2,  0,  2, -2,  2,       6,    0.0,     -3,  0.0 },
    {  1,  0,  0,  2,  0,       6,    0.0,      0,  0.0 },
    {  1,  0,  2, -2,  1,       6,    0.0,     -3,  0.0 },
    {  0,  0,  0, -2,  1,      -5,    0.0,      3,  0.0 },
    {  0, -1,  2, -2,  1,      -5,    0.0,      3,  0.0 },
    {  2,  0,  2,  0,  1,      -5,    0.0,      3,  0.0 },
    {  1, -1,  0,  0,  0,       5,    0.0,      0,  0.0 },
    {  1,  0,  0, -1,  0,      -4,    0.0,      0,  0.0 },
    {  0,  0,  0,  1,  0,      -4,    0.0,      0,  0.0 },
    {  0,  1,  0, -2,  0,      -4,    0.0,      0,  0.0 },
    {  1,  0, -2,  0,  0,       4,    0.0,      0,  0.0 },
    {  2,  0,  0, -2,  1,       4,    0.0,     -2,  0.0 },
    {  0,  1,  2, -2,  1,       4,    0.0,     -2,  0.0 },
    {  1,  1,  0,  0,  0,      -3,    0.0,      0,  0.0 },
    {  1, -1,  0, -1,  0,      -3,    0.0,      0,  0.0 },
    { -1, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 },
    {  0, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 },
    {  1, -1,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    {  3,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    { -2,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 },
    {  1,  0,  2,  0,  0,       3,    0.0,      0,  0.0 },
    { -1,  0,  2,  4,  2,      -2,    0.0,      1,  0.0 },
    {  1,  0,  0,  0,  2,      -2,    0.0,      1,  0.0 },
    { -1,  0,  2, -2,  1,      -2,    0.0,      1,  0.0 },
    {  0, -2,  2, -2,  1,      -2,    0.0,      1,  0.0 },
    { -2,  0,  0,  0,  1,      -2,    0.0,      1,  0.0 },
    {  2,  0,  0,  0,  1,       2,    0.0,     -1,  0.0 },
    {  3,  0,  0,  0,  0,       2,    0.0,      0,  0.0 },
    {  1,  1,  2,  0,  2,       2,    0.0,     -1,  0.0 },
    {  0,  0,  2,  1,  2,       2,    0.0,     -1,  0.0 },
    {  1,  0,  0,  2,  1,      -1,    0.0,      0,  0.0 },
    {  1,  0,  2,  2,  1,      -1,    0.0,      1,  0.0 },
    {  1,  1,  0, -2,  1,      -1,    0.0,      0,  0.0 },
    {  0,  1,  0,  2,  0,      -1,    0.0,      0,  0.0 },
    {  0,  1,  2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  0,  1, -2,  2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0, -2,  2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0, -2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0,  2, -2,  0,      -1,    0.0,      0,  0.0 },
    {  1,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 },
    {  2,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 },
    {  0,  0,  2,  4,  2,      -1,    0.0,      0,  0.0 },
    {  0,  0,  2, -1,  2,      -1,    0.0,      0,  0.0 },
    { -2,  0,  2,  4,  2,      -1,    0.0,      1,  0.0 },
    {  2,  0,  2,  2,  2,      -1,    0.0,      0,  0.0 },
    {  0, -1,  2,  0,  1,      -1,    0.0,      0,  0.0 },
    {  0,  0, -2,  0,  1,      -1,    0.0,      0,  0.0 },
    {  0,  0,  4, -2,  2,       1,    0.0,      0,  0.0 },
    {  0,  1,  0,  0,  2,       1,    0.0,      0,  0.0 },
    {  1,  1,  2, -2,  2,       1,    0.0,     -1,  0.0 },
    {  3,  0,  2, -2,  2,       1,    0.0,      0,  0.0 },
    { -2,  0,  2,  2,  2,       1,    0.0,     -1,  0.0 },
    { -1,  0,  0,  0,  2,       1,    0.0,     -1,  0.0 },
    {  0,  0, -2,  2,  1,       1,    0.0,      0,  0.0 },
    {  0,  1,  2,  0,  1,       1,    0.0,      0,  0.0 },
    { -1,  0,  4,  0,  2,       1,    0.0,      0,  0.0 },
    {  2,  1,  0, -2,  0,       1,    0.0,      0,  0.0 },
    {  2,  0,  0,  2,  0,       1,    0.0,      0,  0.0 },
    {  2,  0,  2, -2,  1,       1,    0.0,     -1,  0.0 },
    {  2,  0, -2,  0,  1,       1,    0.0,      0,  0.0 },
    {  1, -1,  0, -2,  0,       1,    0.0,      0,  0.0 },
    { -1,  0,  0,  1,  1,       1,    0.0,      0,  0.0 },
    { -1, -1,  0,  2,  1,       1,    0.0,      0,  0.0 },
    {  0,  1,  0,  1,  0,       1,    0.0,      0,  0.0 }}

  // Reading periodic terms
  var fMm float64
  var fM float64
  var fF float64
  var fD float64
  var f_omega float64
  var dp float64 = 0
  var de float64 = 0
  x := 0

  for ok := true; ok; ok = x < nutRows {
    fMm = nut[x][0]
    fM = nut[x][1]
    fF = nut[x][2]
    fD = nut[x][3]
    f_omega = nut[x][4]
    dp += (nut[x][5] + data.TE * nut[x][6]) * SinD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)
    de += (nut[x][7] + data.TE * nut[x][8]) * CosD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega)
    x++
  }

  // Corrections (Herring, 1987)
  const corrRows int = 4
  const corrColumns int = 9
  var corr = [corrRows][corrColumns] float64 {
    { 0, 0, 0, 0, 1,-725, 417, 213, 224 },
    { 0, 1, 0, 0, 0, 523,  61, 208, -24 },
    { 0, 0, 2,-2, 2, 102,-118, -41, -47 },
    { 0, 0, 2, 0, 2, -81,   0,  32,   0 }}
  x = 0

  for ok := true; ok; ok = x < corrRows {
    fMm = corr[x][0]
    fM = corr[x][1]
    fF = corr[x][2]
    fD = corr[x][3]
    f_omega = corr[x][4]
    dp += 0.1 * (corr[x][5] * SinD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][6] * CosD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega))
    de += 0.1 * (corr[x][7] * CosD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][8] * SinD(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega))
    x++
  }


  // calculateNutation in longitude
  data.deltaPsi = dp / 36000000

  // calculateNutation in obliquity
  data.deltaEps = de / 36000000

  // Mean obliquity of the ecliptic
  data.Eps0 = (84381.448 - 46.815 * data.TE - 0.00059 * data.TE2 + 0.001813 * data.TE3) / 3600

  // True obliquity of the ecliptic
  data.Eps = data.Eps0 + data.deltaEps
}

func calculateAberration() {
  data.kappa = ToRadians(20.49552 / 3600)
  data.pi0 = ToRadians(102.93735 + 1.71953 * data.TE + 0.00046 * data.TE2)
  data.e = 0.016708617 - 0.000042037 * data.TE - 0.0000001236 * data.TE2
}

// GHA Aries, GAST, GMST, equation of the equinoxes
func calculateAries() {
	// Mean GHA Aries
	data.GHAAmean = Norm360Deg(280.46061837 + 360.98564736629 * (data.JD - 2451545) + 0.000387933 * data.T2 - data.T3 / 38710000)

	// GMST
	data.SidTm = OutSideralTime(data.GHAAmean)

	// True GHA Aries
	data.GHAAtrue = Norm360Deg(data.GHAAmean + data.deltaPsi * CosD(data.Eps))

	// GAST
  data.SidTa = OutSideralTime(data.GHAAtrue)

	// Equation of the equinoxes
	data.EoE = 240 * data.deltaPsi * CosD(data.Eps)
	data.EoEout = math.Round(1000 * data.EoE) / 1000
	// EoEout = " " + EoEout + "s";
}

// Calculations for the Sun
func calculateSun() {
	// Mean longitude of the Sun
	// Lsun_mean := Norm360Deg(280.4664567 + 360007.6982779 * data.Tau + 0.03032028 * data.Tau2 + data.Tau3 / 49931 - data.Tau4 / 15299 - data.Tau5 / 1988000)

	// Heliocentric longitude of the Earth
  data.Le = LEarth(data.Tau)

	// Geocentric longitude of the Sun
	data.Lsun_true = Norm360Deg(data.Le + 180 - 0.000025)

	// Heliocentric latitude of Earth
	data.Be = BEarth(data.Tau)

	// Geocentric latitude of the Sun
	beta := Norm360Deg(- data.Be)

	// Corrections
	data.Lsun_prime = Norm360Deg(data.Le + 180 - 1.397 * data.TE - 0.00031 * data.TE2)

	beta = beta + 0.000011 * (CosD(data.Lsun_prime) - SinD(data.Lsun_prime))

	// Distance Earth-Sun
	data.Re = REarth(data.Tau)
	data.dES = 149597870.691 * data.Re

	// Apparent longitude of the Sun
	data.lambdaSun = Norm360Deg(data.Lsun_true + data.deltaPsi - 0.005691611 / data.Re)

	// Right ascension of the Sun, apparent
	data.RASun = ToDegrees(Norm2PiRad(math.Atan2((SinD(data.lambdaSun) * CosD(data.Eps) - TanD(beta) * SinD(data.Eps)), CosD(data.lambdaSun))))

	// Declination of the Sun, apparent
	data.DECSun = ToDegrees(math.Asin(SinD(beta) * CosD(data.Eps) + CosD(beta) * SinD(data.Eps) * SinD(data.lambdaSun)))

	// GHA of the Sun
	data.GHASun = Norm360Deg(data.GHAAtrue - data.RASun)

	// Semi-diameter of the Sun
	data.SDSun = 959.63 / data.Re

	//Horizontal parallax of the Sun
	data.HPSun = 8.794 / data.Re

	// Equation of time
	// EoT = 4*(Lsun_mean-0.0057183-0.0008-RASun+deltaPsi*cosd(eps));
	data.EoT = 4 * data.GHASun + 720 - 1440 * data.dayFraction
	if data.EoT > 20 {
		data.EoT -= 1440
	}
	if data.EoT < -20 {
		data.EoT += 1440
	}
}

// Calculations for Venus
func calculateVenus() {
	// Heliocentric spherical coordinates
	L := LVenus(data.Tau)
	B := BVenus(data.Tau)
	R := RVenus(data.Tau)

	// Rectangular coordinates
	x := R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y := R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z := R * SinD(B) - data.Re * SinD(data.Be)

	// Geocentric spherical coordinates
	lambda := math.Atan2(y, x)
	beta := math.Atan(z / math.Sqrt(x * x + y * y))

	// Distance from Earth / light time
	d := math.Sqrt(x * x + y * y + z * z)
	lt := 0.0057755183 * d

	// Time correction
	Tau_corr := (data.JDE - lt - 2451545) / 365250

	// Coordinates corrected for light time
	L = LVenus(Tau_corr)
	B = BVenus(Tau_corr)
	R = RVenus(Tau_corr)
	x = R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y = R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z = R * SinD(B) - data.Re * SinD(data.Be)

	lambda = math.Atan2(y, x)
	beta = math.Atan(z / math.Sqrt(x * x + y * y))

	// aberration
	dlambda := (data.e * data.kappa * math.Cos(data.pi0 - lambda) - data.kappa * math.Cos(ToRadians(data.Lsun_true) - lambda)) / math.Cos(beta)
	dbeta := -data.kappa * math.Sin(beta) * (math.Sin(ToRadians(data.Lsun_true) - lambda) - data.e * math.Sin(data.pi0 - lambda))

	lambda += dlambda
	beta += dbeta

	// FK5
	lambda_prime := lambda - ToRadians(1.397) * data.TE - ToRadians(0.00031) * data.TE2

	dlambda = ToRadians(-0.09033) / 3600 + ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) + math.Sin(lambda_prime)) * math.Tan(beta)
	dbeta = ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) - math.Sin(lambda_prime))

	lambda += dlambda
	beta += dbeta

	// calculateNutation in longitude
	lambda += ToRadians(data.deltaPsi)

	// Right ascension, apparent
	data.RAVenus = ToDegrees(Norm2PiRad(math.Atan2((math.Sin(lambda) * CosD(data.Eps) - math.Tan(beta) * SinD(data.Eps)), math.Cos(lambda))))

	// Declination of Venus, apparent
	data.DECVenus = ToDegrees(math.Asin(math.Sin(beta) * CosD(data.Eps) + math.Cos(beta) * SinD(data.Eps) * math.Sin(lambda)))

	// GHA of Venus
	data.GHAVenus = Norm360Deg(data.GHAAtrue - data.RAVenus)

	// Semi-diameter of Venus (including cloud layer)
	data.SDVenus = 8.41 / d

	// Horizontal parallax of Venus
	data.HPVenus = 8.794 / d

	// Illumination of the planet's disk
	k := 100 * (1 + ((R - data.Re * CosD(B) * CosD(L - data.Le)) / d)) / 2
	data.IllumVenus = math.Round(10 * k) / 10
}

// Calculations for Mars
func calculateMars() {
	// Heliocentric coordinates
	L := LMars(data.Tau)
	B := BMars(data.Tau)
	R := RMars(data.Tau)

	// Rectangular coordinates
	x := R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y := R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z := R * SinD(B) - data.Re * SinD(data.Be)

	// Geocentric coordinates
	lambda := math.Atan2(y, x)
	beta := math.Atan(z / math.Sqrt(x * x + y * y))

	// Distance from earth / light time
	d := math.Sqrt(x * x + y * y + z * z)
	lt := 0.0057755183 * d

	// Time correction
	Tau_corr := (data.JDE - lt - 2451545) / 365250

	// Coordinates corrected for light time
	L = LMars(Tau_corr)
	B = BMars(Tau_corr)
	R = RMars(Tau_corr)
	x = R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y = R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z = R * SinD(B) - data.Re * SinD(data.Be)

	lambda = math.Atan2(y, x)
	beta = math.Atan(z / math.Sqrt(x * x + y * y))

	// aberration
	dlambda := (data.e * data.kappa * math.Cos(data.pi0 - lambda) - data.kappa * math.Cos(ToRadians(data.Lsun_true) - lambda)) / math.Cos(beta)
	dbeta := -data.kappa * math.Sin(beta) * (math.Sin(ToRadians(data.Lsun_true) - lambda) - data.e * math.Sin(data.pi0 - lambda))

	lambda += dlambda
	beta += dbeta

	// FK5
	lambda_prime := lambda - ToRadians(1.397) * data.TE -  ToRadians(0.00031) * data.TE2

	dlambda =  ToRadians(-0.09033) / 3600 +  ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) + math.Sin(lambda_prime)) * math.Tan(beta)
	dbeta =  ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) - math.Sin(lambda_prime))

	lambda += dlambda
	beta += dbeta

	// calculateNutation in longitude
	lambda +=  ToRadians(data.deltaPsi)

	// Right ascension, apparent
	data.RAMars = ToDegrees(Norm2PiRad(math.Atan2((math.Sin(lambda) * CosD(data.Eps) - math.Tan(beta) * SinD(data.Eps)), math.Cos(lambda))))

	// Declination of Mars, apparent
	data.DECMars = ToDegrees(math.Asin(math.Sin(beta) * CosD(data.Eps) + math.Cos(beta) * SinD(data.Eps) * math.Sin(lambda)))

	//GHA of Mars
	data.GHAMars = Norm360Deg(data.GHAAtrue - data.RAMars)

	// Semi-diameter of Mars
	data.SDMars = 4.68 / d

	// Horizontal parallax of Mars
	data.HPMars = 8.794 / d

	// Illumination of the planet's disk
	k := 100 * (1 + ((R - data.Re * CosD(B) * CosD(L - data.Le)) / d)) / 2
	data.IllumMars = math.Round(10 * k) / 10
}

// Calculations for Jupiter
func calculateJupiter() {
	// Heliocentric coordinates
	L := LJupiter(data.Tau)
	B := BJupiter(data.Tau)
	R := RJupiter(data.Tau)

	// Rectangular coordinates
	x := R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y := R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z := R * SinD(B) - data.Re * SinD(data.Be)

	// Geocentric coordinates
	lambda := math.Atan2(y, x)
	beta := math.Atan(z / math.Sqrt(x * x + y * y))

	// Distance from earth / light time
	d := math.Sqrt(x * x + y * y + z * z)
	lt := 0.0057755183 * d

	// Time correction
	Tau_corr := (data.JDE - lt - 2451545) / 365250

	// Coordinates corrected for light time
	L = LJupiter(Tau_corr)
	B = BJupiter(Tau_corr)
	R = RJupiter(Tau_corr)
	x = R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y = R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z = R * SinD(B) - data.Re * SinD(data.Be)

	lambda = math.Atan2(y, x)
	beta = math.Atan(z / math.Sqrt(x * x + y * y))

	// aberration
	dlambda := (data.e * data.kappa * math.Cos(data.pi0 - lambda) - data.kappa * math.Cos(ToRadians(data.Lsun_true) - lambda)) / math.Cos(beta)
	dbeta := -data.kappa * math.Sin(beta) * (math.Sin(ToRadians(data.Lsun_true) - lambda) - data.e * math.Sin(data.pi0 - lambda))

	lambda += dlambda
	beta += dbeta

	// FK5
	lambda_prime := lambda - ToRadians(1.397) * data.TE - ToRadians(0.00031) * data.TE2

	dlambda = ToRadians(-0.09033) / 3600 + ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) + math.Sin(lambda_prime)) * math.Tan(beta)
	dbeta = ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) - math.Sin(lambda_prime))

	lambda += dlambda
	beta += dbeta

	// calculateNutation in longitude
	lambda += ToRadians(data.deltaPsi)

	// Right ascension, apparent
	data.RAJupiter = ToDegrees(Norm2PiRad(math.Atan2((math.Sin(lambda) * CosD(data.Eps) - math.Tan(beta) * SinD(data.Eps)), math.Cos(lambda))))

	// Declination of Jupiter, apparent
	data.DECJupiter = ToDegrees(math.Asin(math.Sin(beta) * CosD(data.Eps) + math.Cos(beta) * SinD(data.Eps) * math.Sin(lambda)))

	// GHA of Jupiter
	data.GHAJupiter = Norm360Deg(data.GHAAtrue - data.RAJupiter)

	// Semi-diameter of Jupiter (equatorial)
	data.SDJupiter = 98.44 / d

	// Horizontal parallax of Jupiter
	data.HPJupiter = 8.794 / d

	// Illumination of the planet's disk
	k := 100 * (1 + ((R - data.Re * CosD(B) * CosD(L - data.Le)) / d)) / 2
	data.IllumJupiter = math.Round(10 * k) / 10
}

// Calculations for Saturn
func calculateSaturn() {
	// Heliocentric coordinates
	L := LSaturn(data.Tau)
	B := BSaturn(data.Tau)
	R := RSaturn(data.Tau)

	// Rectangular coordinates
	x := R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y := R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z := R * SinD(B) - data.Re * SinD(data.Be)

	// Geocentric coordinates
	lambda := math.Atan2(y, x)
	beta := math.Atan(z / math.Sqrt(x * x + y * y))

	// Distance from earth / light time
	d := math.Sqrt(x * x + y * y + z * z)
	lt := 0.0057755183 * d

	// Time correction
	Tau_corr := (data.JDE - lt - 2451545) / 365250

	// Coordinates corrected for light time
	L = LSaturn(Tau_corr)
	B = BSaturn(Tau_corr)
	R = RSaturn(Tau_corr)
	x = R * CosD(B) * CosD(L) - data.Re * CosD(data.Be) * CosD(data.Le)
	y = R * CosD(B) * SinD(L) - data.Re * CosD(data.Be) * SinD(data.Le)
	z = R * SinD(B) - data.Re * SinD(data.Be)

	lambda = math.Atan2(y, x)
	beta = math.Atan(z / math.Sqrt(x * x + y * y))

	// aberration
	dlambda := (data.e * data.kappa * math.Cos(data.pi0 - lambda) - data.kappa * math.Cos(ToRadians(data.Lsun_true) - lambda)) / math.Cos(beta)
	dbeta := -data.kappa * math.Sin(beta) * (math.Sin(ToRadians(data.Lsun_true) - lambda) - data.e * math.Sin(data.pi0 - lambda))

	lambda += dlambda
	beta += dbeta

	// FK5
	lambda_prime := lambda - ToRadians(1.397) * data.TE - ToRadians(0.00031) * data.TE2
	dlambda = ToRadians(-0.09033) / 3600 + ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) + math.Sin(lambda_prime)) * math.Tan(beta)
	dbeta = ToRadians(0.03916) / 3600 * (math.Cos(lambda_prime) - math.Sin(lambda_prime))

	lambda += dlambda
	beta += dbeta

	// calculateNutation in longitude
	lambda += ToRadians(data.deltaPsi)

	// Right ascension, apparent
	data.RASaturn = ToDegrees(Norm2PiRad(math.Atan2((math.Sin(lambda) * CosD(data.Eps) - math.Tan(beta) * SinD(data.Eps)), math.Cos(lambda))))

	// Declination of Saturn, apparent
	data.DECSaturn = ToDegrees(math.Asin(math.Sin(beta) * CosD(data.Eps) + math.Cos(beta) * SinD(data.Eps) * math.Sin(lambda)))

	// GHA of Saturn
	data.GHASaturn = Norm360Deg(data.GHAAtrue - data.RASaturn)

	// Semi-diameter of Saturn (equatorial)
	data.SDSaturn = 82.73 / d

	// Horizontal parallax of Saturn
	data.HPSaturn = 8.794 / d

	// Illumination of the planet's disk
	k := 100 * (1 + ((R - data.Re * CosD(B) * CosD(L - data.Le)) / d)) / 2
	data.IllumSaturn = math.Round(10 * k) / 10
}

// Calculations for the moon
func calculateMoon() {
	// Mean longitude of the moon
	Lmm := Norm360Deg(218.3164591 + 481267.88134236 * data.TE - 0.0013268 * data.TE2 + data.TE3 / 538841 - data.TE4 / 65194000)

	// Mean elongation of the moon
	D := Norm360Deg(297.8502042 + 445267.1115168 * data.TE - 0.00163 * data.TE2 + data.TE3 / 545868 - data.TE4 / 113065000)

	// Mean anomaly of the sun
	Msm := Norm360Deg(357.5291092 + 35999.0502909 * data.TE - 0.0001536 * data.TE2 + data.TE3 / 24490000)

	// Mean anomaly of the moon
	Mmm := Norm360Deg(134.9634114 + 477198.8676313 * data.TE + 0.008997 * data.TE2 + data.TE3 / 69699 - data.TE4 / 14712000)

	// Mean distance of the moon from ascending node
	F := Norm360Deg(93.2720993 + 483202.0175273 * data.TE - 0.0034029 * data.TE2 - data.TE3 / 3526000 + data.TE4 / 863310000)

	// Corrections
	A1 := Norm360Deg(119.75 + 131.849 * data.TE)
	A2 := Norm360Deg(53.09 + 479264.29 * data.TE)
	A3 := Norm360Deg(313.45 + 481266.484 * data.TE)
	fE := 1 - 0.002516 * data.TE - 0.0000074 * data.TE2
	fE2 := fE * fE

	// Periodic terms for the moon:

	// Longitude and distance
  // Periodic terms for nutation
	const ldRows int  = 60
  const ldColumns int = 6
	var ld = [ldRows][ldColumns] float64 {
		{ 0,  0,  1,  0, 6288774, -20905355 },
		{ 2,  0, -1,  0, 1274027,  -3699111 },
		{ 2,  0,  0,  0,  658314,  -2955968 },
		{ 0,  0,  2,  0,  213618,   -569925 },
		{ 0,  1,  0,  0, -185116,     48888 },
		{ 0,  0,  0,  2, -114332,     -3149 },
		{ 2,  0, -2,  0,   58793,    246158 },
		{ 2, -1, -1,  0,   57066,   -152138 },
		{ 2,  0,  1,  0,   53322,   -170733 },
		{ 2, -1,  0,  0,   45758,   -204586 },
		{ 0,  1, -1,  0,  -40923,   -129620 },
		{ 1,  0,  0,  0,  -34720,    108743 },
		{ 0,  1,  1,  0,  -30383,    104755 },
		{ 2,  0,  0, -2,   15327,     10321 },
		{ 0,  0,  1,  2,  -12528,         0 },
		{ 0,  0,  1, -2,   10980,     79661 },
		{ 4,  0, -1,  0,   10675,    -34782 },
		{ 0,  0,  3,  0,   10034,    -23210 },
		{ 4,  0, -2,  0,    8548,    -21636 },
		{ 2,  1, -1,  0,   -7888,     24208 },
		{ 2,  1,  0,  0,   -6766,     30824 },
		{ 1,  0, -1,  0,   -5163,     -8379 },
		{ 1,  1,  0,  0,    4987,    -16675 },
		{ 2, -1,  1,  0,    4036,    -12831 },
		{ 2,  0,  2,  0,    3994,    -10445 },
		{ 4,  0,  0,  0,    3861,    -11650 },
		{ 2,  0, -3,  0,    3665,     14403 },
		{ 0,  1, -2,  0,   -2689,     -7003 },
		{ 2,  0, -1,  2,   -2602,         0 },
		{ 2, -1, -2,  0,    2390,     10056 },
		{ 1,  0,  1,  0,   -2348,      6322 },
		{ 2, -2,  0,  0,    2236,     -9884 },
		{ 0,  1,  2,  0,   -2120,      5751 },
		{ 0,  2,  0,  0,   -2069,         0 },
		{ 2, -2, -1,  0,    2048,     -4950 },
		{ 2,  0,  1, -2,   -1773,      4130 },
		{ 2,  0,  0,  2,   -1595,         0 },
		{ 4, -1, -1,  0,    1215,     -3958 },
		{ 0,  0,  2,  2,   -1110,         0 },
		{ 3,  0, -1,  0,    -892,      3258 },
		{ 2,  1,  1,  0,    -810,      2616 },
		{ 4, -1, -2,  0,     759,     -1897 },
		{ 0,  2, -1,  0,    -713,     -2117 },
		{ 2,  2, -1,  0,    -700,      2354 },
		{ 2,  1, -2,  0,     691,         0 },
		{ 2, -1,  0, -2,     596,         0 },
		{ 4,  0,  1,  0,     549,     -1423 },
		{ 0,  0,  4,  0,     537,     -1117 },
		{ 4, -1,  0,  0,     520,     -1571 },
		{ 1,  0, -2,  0,    -487,     -1739 },
		{ 2,  1,  0, -2,    -399,         0 },
		{ 0,  0,  2, -2,    -381,     -4421 },
		{ 1,  1,  1,  0,     351,         0 },
		{ 3,  0, -2,  0,    -340,         0 },
		{ 4,  0, -3,  0,     330,         0 },
		{ 2, -1,  2,  0,     327,         0 },
		{ 0,  2,  1,  0,    -323,      1165 },
		{ 1,  1, -1,  0,     299,         0 },
		{ 2,  0,  3,  0,     294,         0 },
		{ 2,  0, -1, -2,       0,      8752 }}

	const latRows int = 60
  const latColumns int = 5
	var lat = [latRows][latColumns] float64 {
		{ 0,  0,  0,  1, 5128122 },
		{ 0,  0,  1,  1,  280602 },
		{ 0,  0,  1, -1,  277693 },
		{ 2,  0,  0, -1,  173237 },
		{ 2,  0, -1,  1,   55413 },
		{ 2,  0, -1, -1,   46271 },
		{ 2,  0,  0,  1,   32573 },
		{ 0,  0,  2,  1,   17198 },
		{ 2,  0,  1, -1,    9266 },
		{ 0,  0,  2, -1,    8822 },
		{ 2, -1,  0, -1,    8216 },
		{ 2,  0, -2, -1,    4324 },
		{ 2,  0,  1,  1,    4200 },
		{ 2,  1,  0, -1,   -3359 },
		{ 2, -1, -1,  1,    2463 },
		{ 2, -1,  0,  1,    2211 },
		{ 2, -1, -1, -1,    2065 },
		{ 0,  1, -1, -1,   -1870 },
		{ 4,  0, -1, -1,    1828 },
		{ 0,  1,  0,  1,   -1794 },
		{ 0,  0,  0,  3,   -1749 },
		{ 0,  1, -1,  1,   -1565 },
		{ 1,  0,  0,  1,   -1491 },
		{ 0,  1,  1,  1,   -1475 },
		{ 0,  1,  1, -1,   -1410 },
		{ 0,  1,  0, -1,   -1344 },
		{ 1,  0,  0, -1,   -1335 },
		{ 0,  0,  3,  1,    1107 },
		{ 4,  0,  0, -1,    1021 },
		{ 4,  0, -1,  1,     833 },
		{ 0,  0,  1, -3,     777 },
		{ 4,  0, -2,  1,     671 },
		{ 2,  0,  0, -3,     607 },
		{ 2,  0,  2, -1,     596 },
		{ 2, -1,  1, -1,     491 },
		{ 2,  0, -2,  1,    -451 },
		{ 0,  0,  3, -1,     439 },
		{ 2,  0,  2,  1,     422 },
		{ 2,  0, -3, -1,     421 },
		{ 2,  1, -1,  1,    -366 },
		{ 2,  1,  0,  1,    -351 },
		{ 4,  0,  0,  1,     331 },
		{ 2, -1,  1,  1,     315 },
		{ 2, -2,  0, -1,     302 },
		{ 0,  0,  1,  3,    -283 },
		{ 2,  1,  1, -1,    -229 },
		{ 1,  1,  0, -1,     223 },
		{ 1,  1,  0,  1,     223 },
		{ 0,  1, -2, -1,    -220 },
		{ 2,  1, -1, -1,    -220 },
		{ 1,  0,  1,  1,    -185 },
		{ 2, -1, -2, -1,     181 },
		{ 0,  1,  2,  1,    -177 },
		{ 4,  0, -2, -1,     176 },
		{ 4, -1, -1, -1,     166 },
		{ 1,  0,  1, -1,    -164 },
		{ 4,  0,  1, -1,     132 },
		{ 1,  0, -1, -1,    -119 },
		{ 4, -1,  0, -1,     115 },
		{ 2, -2,  0,  1,     107 }}

	// Reading periodic terms
	var fD, fD2, fM, fM2, fMm, fMm2, fF, fF2, coeffs, coeffs2, coeffc, f, f2, sumL, sumR, sumB float64
  sumL = 0
  sumR = 0
  sumB = 0
	x := 0

  for ok := true; ok; ok = x < latRows {
		fD = ld[x][0]
		fM = ld[x][1]
		fMm = ld[x][2]
		fF = ld[x][3]
		coeffs = ld[x][4]
		coeffc = ld[x][5]
		if fM == 1 || fM == -1 {
			f = fE
		} else if fM == 2 || fM == -2 {
			f = fE2
		} else {
			f = 1
		}
		sumL += f * coeffs * SinD(fD * D + fM * Msm + fMm * Mmm + fF * F)
		sumR += f * coeffc * CosD(fD * D + fM * Msm + fMm * Mmm + fF * F)
		fD2 = lat[x][0]
		fM2 = lat[x][1]
		fMm2 = lat[x][2]
		fF2 = lat[x][3]
		coeffs2 = lat[x][4]
		if fM2 == 1 || fM2 == -1 {
			f2 = fE
		} else if fM2 == 2 || fM2 == -2 {
			f2 = fE2
		} else {
			f2 = 1
		}
		sumB += f2 * coeffs2 * SinD(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F)
		x++
	}

	// Corrections
	sumL = sumL + 3958 * SinD(A1) + 1962 * SinD(Lmm - F) + 318 * SinD(A2)
	sumB = sumB - 2235 * SinD(Lmm) + 382 * SinD(A3) + 175 * SinD(A1 - F) + 175 * SinD(A1 + F) + 127 * SinD(Lmm - Mmm) - 115 * SinD(Lmm + Mmm)

	// Longitude of the moon
	lambdaMm := Norm360Deg(Lmm + sumL / 1000000)

	// Latitude of the moon
	betaM := sumB / 1000000

	// Distance earth-moon
	dEM := 385000.56 + sumR / 1000

	// Apparent longitude of the moon
	data.lambdaMapp = lambdaMm + data.deltaPsi

	// Right ascension of the moon, apparent
	data.RAMoon = ToDegrees(Norm2PiRad(math.Atan2((SinD(data.lambdaMapp) * CosD(data.Eps) - TanD(betaM) * SinD(data.Eps)), CosD(data.lambdaMapp))))

	// Declination of the moon
	data.DECMoon = ToDegrees(math.Asin(SinD(betaM) * CosD(data.Eps) + CosD(betaM) * SinD(data.Eps) * SinD(data.lambdaMapp)))

	// GHA of the moon
	data.GHAMoon = Norm360Deg(data.GHAAtrue - data.RAMoon)

	// Horizontal parallax of the moon
	data.HPMoon = ToDegrees(3600 * math.Asin(6378.14 / dEM))

	// Semi-diameter of the moon
	data.SDMoon = ToDegrees(3600 * math.Asin(1738 / dEM))

	// Geocentric angular distance between moon and sun
	data.LDist = ToDegrees(math.Acos(SinD(data.DECMoon) * SinD(data.DECSun) + CosD(data.DECMoon) * CosD(data.DECSun) * CosD(data.RAMoon - data.RASun)))

	//Phase angle
	i := math.Atan2(data.dES * SinD(data.LDist), (dEM - data.dES * CosD(data.LDist)))

	//Illumination of the moon's disk
	k := 100 * (1 + math.Cos(i)) / 2
	data.IllumMoon = math.Round(10 * k) / 10
}

// Ephemerides of Polaris
func calculatePolaris() {
	// Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
	RApol0 := 37.95293333
	DECpol0 := 89.26408889

	// Proper motion per year
	dRApol := 2.98155 / 3600
	dDECpol := -0.0152 / 3600

	// Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
	RApol1 := RApol0 + 100 * data.TE * dRApol
	DECpol1 := DECpol0 + 100 * data.TE * dDECpol

	// Mean obliquity of ecliptic at 2000.0 in degrees
	eps0_2000 := 23.439291111

	// Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
	lambdapol1 := math.Atan2((SinD(RApol1) * CosD(eps0_2000) + TanD(DECpol1) * SinD(eps0_2000)), CosD(RApol1))
	betapol1 := math.Asin(SinD(DECpol1) * CosD(eps0_2000) - CosD(DECpol1) * SinD(eps0_2000) * SinD(RApol1))

	// Precession
	eta := ToRadians(47.0029 * data.TE - 0.03302 * data.TE2 + 0.00006 * data.TE3) / 3600
	PI0 := ToRadians(174.876384 - (869.8089 * data.TE + 0.03536 * data.TE2) / 3600)
	p0 := ToRadians(5029.0966 * data.TE + 1.11113 * data.TE2 - 0.0000006 * data.TE3) / 3600

	A1 := math.Cos(eta) * math.Cos(betapol1) * math.Sin(PI0 - lambdapol1) - math.Sin(eta) * math.Sin(betapol1)
	B1 := math.Cos(betapol1) * math.Cos(PI0 - lambdapol1)
	C1 := math.Cos(eta) * math.Sin(betapol1) + math.Sin(eta) * math.Cos(betapol1) * math.Sin(PI0 - lambdapol1)
	lambdapol2 := p0 + PI0 - math.Atan2(A1, B1)
	betapol2 := math.Asin(C1)

	// calculateNutation in longitude
	lambdapol2 += ToRadians(data.deltaPsi)

	// aberration
	dlambdapol := (data.e * data.kappa * math.Cos(data.pi0 - lambdapol2) - data.kappa * math.Cos(ToRadians(data.Lsun_true) - lambdapol2)) / math.Cos(betapol2)
	dbetapol := -data.kappa * math.Sin(betapol2) * (math.Sin(ToRadians(data.Lsun_true) - lambdapol2) - data.e * math.Sin(data.pi0 - lambdapol2))

	lambdapol2 += dlambdapol
	betapol2 += dbetapol

	// Transformation back to equatorial coordinates in radians
	RApol2 := math.Atan2((math.Sin(lambdapol2) * CosD(data.Eps) - math.Tan(betapol2) * SinD(data.Eps)), math.Cos(lambdapol2))
	DECpol2 := math.Asin(math.Sin(betapol2) * CosD(data.Eps) + math.Cos(betapol2) * SinD(data.Eps) * math.Sin(lambdapol2))

	// Finals
	data.GHAPol = data.GHAAtrue - ToDegrees(RApol2)
	data.GHAPol = Norm360Deg(data.GHAPol)
	data.RAPol = ToDegrees(RApol2)
	data.DECPol = ToDegrees(DECpol2)
}

// Calculation of the phase of the Moon
func calculateMoonPhase() {
	x := data.lambdaMapp - data.lambdaSun
	x = Norm360Deg(x)
	x = math.Round(10 * x) / 10
	data.MoonPhaseAngle = x
	if x == 0 {
		data.MoonPhase = " New"
	}
	if x > 0 && x < 90 {
		data.MoonPhase = " +cre"
	}
	if x == 90 {
		data.MoonPhase = " FQ"
	}
	if x > 90 && x < 180 {
		data.MoonPhase = " +gib"
	}
	if x == 180 {
		data.MoonPhase = " Full"
	}
	if x > 180 && x < 270 {
		data.MoonPhase = " -gib"
	}
	if x == 270 {
		data.MoonPhase = " LQ"
	}
	if x > 270 && x < 360 {
		data.MoonPhase = " -cre"
	}
}

func calculateWeekDay() {
  data.JD0h += 1.5
  res := int(data.JD0h - 7 * math.Floor(data.JD0h / 7))
  // data.DoW = (char *) calloc(4, sizeof(char));
  switch res {
    case 0:
      data.DoW = "SUN"
    case 1:
      data.DoW = "MON"
    case 2:
      data.DoW = "TUE"
    case 3:
      data.DoW = "WED"
    case 4:
      data.DoW = "THU"
    case 5:
      data.DoW = "FRI"
    case 6:
      data.DoW = "SAT"
  }
}


/**
 * Main function
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
func Calculate(year int, month int, day int, hour int, minute int, second int, delta_t float64) ComputedData {
  calculateJulianDate(year, month, day, hour, minute, second, delta_t)
  calculateNutation()
  calculateAberration()
  calculateAries()
  calculateSun()
  calculateVenus()
  calculateMars()
  calculateJupiter()
  calculateSaturn()
  calculateMoon()
  calculatePolaris()
  calculateMoonPhase()
  calculateWeekDay()

  return data
}


/**
 * Y parameter (not year) for deltaT computing.
 *
 * @param year
 * @param month in [1..12]
 * @return
 */
func getY(year int, month int) float64 {
	if year < -1999 || year > 3000 {
		fmt.Println("Year must be in [-1999, 3000]")
		return float64(0)
	} else {
		// fprintf(stdout, "Returning y=%f\n", (year + ((month - 0.5) / 12.0)));
		return (float64(year) + ((float64(month) - 0.5) / 12.0))
	}
}

/**
 * Since the usual (the ones I used to used) on-line resources are not always available, obsolete,
 * or expecting some serious revamping, here is a method to calculate deltaT out of thin air.
 *
 * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
 * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
 *               https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
 *
 * @param year from -1999 to +3000
 * @param month in [1..12], NOT in [0..11] <b>&lt; Important!!</b>
 * @return
 */
func CalculateDeltaT(year int, month int) float64 {
	if year < -1999 || year > 3000 {
		fmt.Println("Year must be in [-1999, 3000]")
	}
	if month < 1 || month > 12 {
		fmt.Println("Month must be in [1, 12]")
	}

	var deltaT float64
	y := getY(year, month)

	if year < -500 {
		u := (y - 1820.0) / 100.0
		deltaT = -20.0 + (32.0 * (u * u))
	} else if year < 500 {
		u := y / 100.0
		deltaT = 10583.6 + (-1014.41 * u) + (33.78311 * math.Pow(u, 2)) + (-5.952053 * math.Pow(u, 3)) + 
    (-0.1798452 * math.Pow(u, 4)) + (0.022174192 * math.Pow(u, 5)) + (0.0090316521 * math.Pow(u, 6))
	} else if year < 1600 {
		u := (y - 1000.0) / 100.0
		deltaT = 1574.2 + (-556.01 * u) + (71.23472 * math.Pow(u, 2)) + (0.319781 * math.Pow(u, 3)) + 
    (-0.8503463 * math.Pow(u, 4)) + (-0.005050998 * math.Pow(u, 5)) + (0.0083572073 * math.Pow(u, 6))
	} else if year < 1700 {
		t := y - 1600.0
		deltaT = 120 + (-0.9808 * t) + (-0.01532 * math.Pow(t, 2)) + (math.Pow(t, 3) / 7129)
	} else if year < 1800 {
	  t := y - 1700.0
		deltaT = 8.83 + (0.1603 * t) + (-0.0059285 * math.Pow(t, 2)) + (0.00013336 * math.Pow(t, 3)) + (math.Pow(t, 4) / -1174000)
	} else if year < 1860 {
		t := y - 1800.0
		deltaT = 13.72 + (-0.332447 * t) + (0.0068612 * math.Pow(t, 2)) + (0.0041116 * math.Pow(t, 3)) + 
    (-0.00037436 * math.Pow(t, 4)) + (0.0000121272 * math.Pow(t, 5)) + (-0.0000001699 * math.Pow(t, 6)) + (0.000000000875 * math.Pow(t, 7))
	} else if (year < 1900) {
		t := y - 1860.0
		deltaT = 7.62 + (0.5737 * t) + (-0.251754 * math.Pow(t, 2)) + (0.01680668 * math.Pow(t, 3)) + 
    (-0.0004473624 * math.Pow(t, 4)) + (math.Pow(t, 5) / 233174)
	} else if year < 1920 {
		t := y - 1900
		deltaT = -2.79 + (1.494119 * t) + (-0.0598939 * math.Pow(t, 2)) + (0.0061966 * math.Pow(t, 3)) + (-0.000197 * math.Pow(t, 4))
	} else if year < 1941 {
		t := y - 1920
		deltaT = 21.20 + (0.84493 * t) + (-0.076100 * math.Pow(t, 2)) + (0.0020936 * math.Pow(t, 3))
	} else if year < 1961 {
		t := y - 1950
		deltaT = 29.07 + (0.407 * t) + (math.Pow(t, 2) / -233) + (math.Pow(t, 3) / 2547)
	} else if year < 1986 {
		t := y - 1975
		deltaT = 45.45 + (1.067 * t) + (math.Pow(t, 2) / -260) + (math.Pow(t, 3) / -718)
	} else if year < 2005 {
		t := y - 2000
		deltaT = 63.86 + (0.3345 * t) + (-0.060374 * math.Pow(t, 2)) + (0.0017275 * math.Pow(t, 3)) + 
    (0.000651814 * math.Pow(t, 4)) + (0.00002373599 * math.Pow(t, 5))
	} else if year < 2050 {
 		t := y - 2000
		deltaT = 62.92 + (0.32217 * t) + (0.005589 * math.Pow(t, 2))
	} else if year < 2150 {
		deltaT = -20 + (32 * math.Pow((y - 1820) / 100, 2)) + (-0.5628 * (2150 - y))
	} else {
		u := (y - 1820) / 100
		deltaT = -20 + (32 * math.Pow(u, 2))
	}

	return deltaT
}
