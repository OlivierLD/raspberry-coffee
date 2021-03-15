package astro.celestial

import astro.utils.MathUtils

object Moon {
  def compute(context: AstroContext): Unit = { // Mean longitude of the moon
    val Lmm = MathUtils.trunc(218.3164591 + 481267.88134236 * context.TE - 0.0013268 * context.TE2 + context.TE3 / 538841 - context.TE4 / 65194000)
    //Mean elongation of the moon
    val D = MathUtils.trunc(297.8502042 + 445267.1115168 * context.TE - 0.00163 * context.TE2 + context.TE3 / 545868 - context.TE4 / 113065000)
    // Mean anomaly of the sun
    val Msm = MathUtils.trunc(357.5291092 + 35999.0502909 * context.TE - 0.0001536 * context.TE2 + context.TE3 / 24490000)
    //Mean anomaly of the moon
    val Mmm = MathUtils.trunc(134.9634114 + 477198.8676313 * context.TE + 0.008997 * context.TE2 + context.TE3 / 69699 - context.TE4 / 14712000)
    //Mean distance of the moon from ascending node
    val F = MathUtils.trunc(93.2720993 + 483202.0175273 * context.TE - 0.0034029 * context.TE2 - context.TE3 / 3526000 + context.TE4 / 863310000)
    //Corrections
    val A1 = MathUtils.trunc(119.75 + 131.849 * context.TE)
    val A2 = MathUtils.trunc(53.09 + 479264.29 * context.TE)
    val A3 = MathUtils.trunc(313.45 + 481266.484 * context.TE)
    val fE = 1 - 0.002516 * context.TE - 0.0000074 * context.TE2
    val fE2 = fE * fE
    //Periodic terms for the moon:
    //Longitude and distance
    val ld = Array(
      Array[Double](0, 0, 1, 0, 6288774, -20905355), 
      Array[Double](2, 0, -1, 0, 1274027, -3699111), 
      Array[Double](2, 0, 0, 0, 658314, -2955968), 
      Array[Double](0, 0, 2, 0, 213618, -569925), 
      Array[Double](0, 1, 0, 0, -185116, 48888), 
      Array[Double](0, 0, 0, 2, -114332, -3149), 
      Array[Double](2, 0, -2, 0, 58793, 246158), 
      Array[Double](2, -1, -1, 0, 57066, -152138), 
      Array[Double](2, 0, 1, 0, 53322, -170733), 
      Array[Double](2, -1, 0, 0, 45758, -204586), 
      Array[Double](0, 1, -1, 0, -40923, -129620), 
      Array[Double](1, 0, 0, 0, -34720, 108743), 
      Array[Double](0, 1, 1, 0, -30383, 104755), 
      Array[Double](2, 0, 0, -2, 15327, 10321), 
      Array[Double](0, 0, 1, 2, -12528, 0), 
      Array[Double](0, 0, 1, -2, 10980, 79661), 
      Array[Double](4, 0, -1, 0, 10675, -34782), 
      Array[Double](0, 0, 3, 0, 10034, -23210), 
      Array[Double](4, 0, -2, 0, 8548, -21636), 
      Array[Double](2, 1, -1, 0, -7888, 24208), 
      Array[Double](2, 1, 0, 0, -6766, 30824), 
      Array[Double](1, 0, -1, 0, -5163, -8379), 
      Array[Double](1, 1, 0, 0, 4987, -16675), 
      Array[Double](2, -1, 1, 0, 4036, -12831), 
      Array[Double](2, 0, 2, 0, 3994, -10445), 
      Array[Double](4, 0, 0, 0, 3861, -11650), 
      Array[Double](2, 0, -3, 0, 3665, 14403), 
      Array[Double](0, 1, -2, 0, -2689, -7003), 
      Array[Double](2, 0, -1, 2, -2602, 0), 
      Array[Double](2, -1, -2, 0, 2390, 10056), 
      Array[Double](1, 0, 1, 0, -2348, 6322), 
      Array[Double](2, -2, 0, 0, 2236, -9884), 
      Array[Double](0, 1, 2, 0, -2120, 5751), 
      Array[Double](0, 2, 0, 0, -2069, 0), 
      Array[Double](2, -2, -1, 0, 2048, -4950), 
      Array[Double](2, 0, 1, -2, -1773, 4130), 
      Array[Double](2, 0, 0, 2, -1595, 0), 
      Array[Double](4, -1, -1, 0, 1215, -3958), 
      Array[Double](0, 0, 2, 2, -1110, 0), 
      Array[Double](3, 0, -1, 0, -892, 3258), 
      Array[Double](2, 1, 1, 0, -810, 2616), 
      Array[Double](4, -1, -2, 0, 759, -1897), 
      Array[Double](0, 2, -1, 0, -713, -2117), 
      Array[Double](2, 2, -1, 0, -700, 2354), 
      Array[Double](2, 1, -2, 0, 691, 0), 
      Array[Double](2, -1, 0, -2, 596, 0), 
      Array[Double](4, 0, 1, 0, 549, -1423), 
      Array[Double](0, 0, 4, 0, 537, -1117), 
      Array[Double](4, -1, 0, 0, 520, -1571), 
      Array[Double](1, 0, -2, 0, -487, -1739), 
      Array[Double](2, 1, 0, -2, -399, 0), 
      Array[Double](0, 0, 2, -2, -381, -4421), 
      Array[Double](1, 1, 1, 0, 351, 0), 
      Array[Double](3, 0, -2, 0, -340, 0), 
      Array[Double](4, 0, -3, 0, 330, 0), 
      Array[Double](2, -1, 2, 0, 327, 0), 
      Array[Double](0, 2, 1, 0, -323, 1165), 
      Array[Double](1, 1, -1, 0, 299, 0), 
      Array[Double](2, 0, 3, 0, 294, 0), 
      Array[Double](2, 0, -1, -2, 0, 8752)
    )
    val lat = Array(
      Array[Double](0, 0, 0, 1, 5128122), 
      Array[Double](0, 0, 1, 1, 280602), 
      Array[Double](0, 0, 1, -1, 277693), 
      Array[Double](2, 0, 0, -1, 173237), 
      Array[Double](2, 0, -1, 1, 55413), 
      Array[Double](2, 0, -1, -1, 46271), 
      Array[Double](2, 0, 0, 1, 32573), 
      Array[Double](0, 0, 2, 1, 17198), 
      Array[Double](2, 0, 1, -1, 9266), 
      Array[Double](0, 0, 2, -1, 8822), 
      Array[Double](2, -1, 0, -1, 8216), 
      Array[Double](2, 0, -2, -1, 4324), 
      Array[Double](2, 0, 1, 1, 4200), 
      Array[Double](2, 1, 0, -1, -3359), 
      Array[Double](2, -1, -1, 1, 2463), 
      Array[Double](2, -1, 0, 1, 2211), 
      Array[Double](2, -1, -1, -1, 2065), 
      Array[Double](0, 1, -1, -1, -1870), 
      Array[Double](4, 0, -1, -1, 1828), 
      Array[Double](0, 1, 0, 1, -1794), 
      Array[Double](0, 0, 0, 3, -1749), 
      Array[Double](0, 1, -1, 1, -1565), 
      Array[Double](1, 0, 0, 1, -1491), 
      Array[Double](0, 1, 1, 1, -1475), 
      Array[Double](0, 1, 1, -1, -1410), 
      Array[Double](0, 1, 0, -1, -1344), 
      Array[Double](1, 0, 0, -1, -1335), 
      Array[Double](0, 0, 3, 1, 1107), 
      Array[Double](4, 0, 0, -1, 1021), 
      Array[Double](4, 0, -1, 1, 833), 
      Array[Double](0, 0, 1, -3, 777), 
      Array[Double](4, 0, -2, 1, 671), 
      Array[Double](2, 0, 0, -3, 607), 
      Array[Double](2, 0, 2, -1, 596), 
      Array[Double](2, -1, 1, -1, 491), 
      Array[Double](2, 0, -2, 1, -451), 
      Array[Double](0, 0, 3, -1, 439), 
      Array[Double](2, 0, 2, 1, 422), 
      Array[Double](2, 0, -3, -1, 421), 
      Array[Double](2, 1, -1, 1, -366), 
      Array[Double](2, 1, 0, 1, -351), 
      Array[Double](4, 0, 0, 1, 331), 
      Array[Double](2, -1, 1, 1, 315), 
      Array[Double](2, -2, 0, -1, 302), 
      Array[Double](0, 0, 1, 3, -283), 
      Array[Double](2, 1, 1, -1, -229), 
      Array[Double](1, 1, 0, -1, 223), 
      Array[Double](1, 1, 0, 1, 223), 
      Array[Double](0, 1, -2, -1, -220), 
      Array[Double](2, 1, -1, -1, -220), 
      Array[Double](1, 0, 1, 1, -185), 
      Array[Double](2, -1, -2, -1, 181), 
      Array[Double](0, 1, 2, 1, -177), 
      Array[Double](4, 0, -2, -1, 176), 
      Array[Double](4, -1, -1, -1, 166), 
      Array[Double](1, 0, 1, -1, -164), 
      Array[Double](4, 0, 1, -1, 132), 
      Array[Double](1, 0, -1, -1, -119), 
      Array[Double](4, -1, 0, -1, 115), 
      Array[Double](2, -2, 0, 1, 107))
    //Reading periodic terms
    var fD = .0
    var fD2 = .0
    var fM = .0
    var fM2 = .0
    var fMm = .0
    var fMm2 = .0
    var fF = .0
    var fF2 = .0
    var coeffs = .0
    var coeffs2 = .0
    var coeffc = .0
    var f = .0
    var f2 = .0
    var sumL: Double = 0
    var sumR: Double = 0
    var sumB: Double = 0
    for (x <- ld.indices) {
      fD = ld(x)(0)
      fM = ld(x)(1)
      fMm = ld(x)(2)
      fF = ld(x)(3)
      coeffs = ld(x)(4)
      coeffc = ld(x)(5)
      if (fM == 1 || fM == -1) f = fE
      else if (fM == 2 || fM == -2) f = fE2
      else f = 1
      sumL += f * coeffs * MathUtils.sinD(fD * D + fM * Msm + fMm * Mmm + fF * F)
      sumR += f * coeffc * MathUtils.cosD(fD * D + fM * Msm + fMm * Mmm + fF * F)
      fD2 = lat(x)(0)
      fM2 = lat(x)(1)
      fMm2 = lat(x)(2)
      fF2 = lat(x)(3)
      coeffs2 = lat(x)(4)
      if (fM2 == 1 || fM2 == -1) f2 = fE
      else if (fM2 == 2 || fM2 == -2) f2 = fE2
      else f2 = 1
      sumB += f2 * coeffs2 * MathUtils.sinD(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F)
    }
    sumL = Math.round(sumL + 3958 * MathUtils.sinD(A1) + 1962 * MathUtils.sinD(Lmm - F) + 318 * MathUtils.sinD(A2))
    sumB = Math.round(sumB - 2235 * MathUtils.sinD(Lmm) + 382 * MathUtils.sinD(A3) + 175 * MathUtils.sinD(A1 - F) + 175 * MathUtils.sinD(A1 + F) + 127 * MathUtils.sinD(Lmm - Mmm) - 115 * MathUtils.sinD(Lmm + Mmm))
    //Longitude of the moon
    val lambdaMm = MathUtils.trunc(Lmm + sumL / 1000000D)
    //Latitude of the moon
    val betaM = sumB / 1000000D
    //Distance earth-moon
    val dEM = 385000.56 + sumR / 1000D
    //Apparent longitude of the moon
    context.lambdaMapp = lambdaMm + context.delta_psi
    //Right ascension of the moon, apparent
    context.RAmoon = Math.toDegrees(MathUtils.trunc2(Math.atan2(MathUtils.sinD(context.lambdaMapp) * MathUtils.cosD(context.eps) - MathUtils.tanD(betaM) * MathUtils.sinD(context.eps), MathUtils.cosD(context.lambdaMapp))))
    //Declination of the moon
    context.DECmoon = Math.toDegrees(Math.asin(MathUtils.sinD(betaM) * MathUtils.cosD(context.eps) + MathUtils.cosD(betaM) * MathUtils.sinD(context.eps) * MathUtils.sinD(context.lambdaMapp)))
    //GHA of the moon
    context.GHAmoon = MathUtils.trunc(context.GHAAtrue - context.RAmoon)
    //Horizontal parallax of the moon
    context.HPmoon = Math.toDegrees(3600D * Math.asin(6378.14 / dEM))
    //Semidiameter of the moon
    context.SDmoon = Math.toDegrees(3600D * Math.asin(1738 / dEM))
    //Geocentric angular distance between moon and sun
    context.LDist = Math.toDegrees(Math.acos(MathUtils.sinD(context.DECmoon) * MathUtils.sinD(context.DECsun) + MathUtils.cosD(context.DECmoon) * MathUtils.cosD(context.DECsun) * MathUtils.cosD(context.RAmoon - context.RAsun)))
    //Phase angle
    val i = Math.atan2(context.dES * MathUtils.sinD(context.LDist), dEM - context.dES * MathUtils.cosD(context.LDist))
    context.moonPhase = Math.toDegrees(i)
    //Illumination of the moon's disk
    val k = 100D * (1 + Math.cos(i)) / 2D
    context.k_moon = 10D * k.round / 10D
    context.moonEoT = 4 * context.GHAmoon + 720 - 1440 * context.dayfraction
    if (context.moonEoT > 20) context.moonEoT -= 1440
    if (context.moonEoT < -20) context.moonEoT += 1440
  }
}