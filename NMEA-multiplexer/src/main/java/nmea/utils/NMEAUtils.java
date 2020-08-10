package nmea.utils;

import context.ApplicationContext;
import context.NMEADataCache;
import nmea.mux.context.Context;
import nmea.parser.Angle180;
import nmea.parser.Angle180EW;
import nmea.parser.Angle180LR;
import nmea.parser.Angle360;
import nmea.parser.HDG;
import nmea.parser.OverGround;
import nmea.parser.RMC;
import nmea.parser.Speed;
import nmea.parser.StringParsers;
import nmea.parser.TrueWindDirection;
import nmea.parser.TrueWindSpeed;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class NMEAUtils {
	public final static int ALL_IN_HEXA = 0;
	public final static int CR_NL = 1;

	public static String translateEscape(String str, int option) {
		String s = null;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (option == CR_NL) {
				if (str.charAt(i) == (char) 0x0A) // [NL], \n, [LF]
					sb.append("[LF]");
				else if (str.charAt(i) == (char) 0x0D) // [CR], \r
					sb.append("[CR]");
				else
					sb.append(str.charAt(i));
			} else {
				String c = Integer.toHexString((int) str.charAt(i) & 0xFF).toUpperCase();
				sb.append(StringUtils.lpad(c, 2, "0") + " ");
			}
		}
		return sb.toString();
	}

	public static long longitudeToTime(double longitude) {
		long offset = (long) (longitude * 3_600_000L / 15L);
		return offset;
	}

	/*
	 * Calculated Data
	 * <p>
	 * TWS, TWA, TWD
	 * HDG, true
	 * CSP, CDR
	 * Leeway
	 */
	public static void computeAndSendValuesToCache(NMEADataCache cache) {
		computeAndSendValuesToCache(cache, false);
	}

	@SuppressWarnings("unchecked")
	public static void computeAndSendValuesToCache(NMEADataCache cache, boolean isHDTPresent) {
		double heading = 0d;
		if (!isHDTPresent) {
			double hdc = 0d;
			double dec = 0d;
			//  System.out.println("========================");
			try {
				hdc = ((Angle360) cache.get(NMEADataCache.HDG_COMPASS)).getValue() + ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
			} catch (Exception ex) {
			}
			//  System.out.println("HDG Compass:" + hdc);
			try {
				dec = ((Angle180EW) cache.get(NMEADataCache.DECLINATION)).getValue();
			} catch (Exception ex) {
			}
			if (dec == -Double.MAX_VALUE) {
				dec = ((Angle180EW) cache.get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
			}
			//  System.out.println("Declination:" + dec);

			@SuppressWarnings("unchecked")
			double dev = getDeviation(heading, (List<double[]>)cache.get(NMEADataCache.DEVIATION_DATA));
			cache.put(NMEADataCache.DEVIATION, new Angle180EW(dev));

			heading = hdc + dev; // Magnetic
			cache.put(NMEADataCache.HDG_MAG, new Angle360(heading));
			//  System.out.println("HDG Mag: " + heading);

			double w = dec + dev;
			cache.put(NMEADataCache.VARIATION, new Angle180EW(w));
			heading = hdc + w; // true
			cache.put(NMEADataCache.HDG_TRUE, new Angle360(heading));
			//  System.out.println("HDG True:" + heading);
			//  System.out.println("==========================");
		} else
			try {
				heading = ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue() + ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
			} catch (Exception ex) {
				// Absorb
			}

		double twa = 0d,
						tws = 0d;
		int twd = 0;

		double sog = 0d,
					 cog = 0d,
					 aws = -1d;
		int awa = 0;
		try {
			sog = ((Speed) cache.get(NMEADataCache.SOG)).getValue();
		} catch (Exception ex) {
		}
		try {
			cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
		} catch (Exception ex) {
		}
		try {
			aws = ((Speed) cache.get(NMEADataCache.AWS)).getValue() * ((Double) cache.get(NMEADataCache.AWS_FACTOR)).doubleValue();
		} catch (Exception ex) {
		}
		try {
			awa = (int) (((Angle180) cache.get(NMEADataCache.AWA)).getValue() + ((Double) cache.get(NMEADataCache.AWA_OFFSET)).doubleValue());
		} catch (Exception ex) {
		}

		double awsCoeff = 1d;
		try {
			awsCoeff = ((Double) cache.get(NMEADataCache.AWS_FACTOR)).doubleValue();
		} catch (Exception ex) {
		}
		double awaOffset = 0d;
		try {
			awaOffset = ((Double) cache.get(NMEADataCache.AWA_OFFSET)).doubleValue();
		} catch (Exception ex) {
		}
		double bspCoeff = 1d;
		try {
			bspCoeff = ((Double) cache.get(NMEADataCache.BSP_FACTOR)).doubleValue();
		} catch (Exception ex) {
		}
		double hdgOffset = 0d;
		try {
			hdgOffset = ((Double) cache.get(NMEADataCache.HDG_OFFSET)).doubleValue();
		} catch (Exception ex) {
		}

		if (aws != -Double.MAX_VALUE) {
			//    System.out.println("Using the GOOD method");
			double[] tw = calculateTWwithGPS(
					aws,
					awsCoeff,
					awa,
					awaOffset,
					heading,
					hdgOffset,
					sog,
					cog);
			twa = tw[0];
			tws = tw[1];
			twd = (int) tw[2];
			cache.put(NMEADataCache.TWA, new Angle180(twa));
			cache.put(NMEADataCache.TWS, new TrueWindSpeed(tws));
			cache.put(NMEADataCache.TWD, new TrueWindDirection(twd));
		}
//  else
//    System.out.println(" NO AW !!!");
//  System.out.println("AWS:" + aws + ", TWS:" + tws + ", AWA:" + awa + ", TWA:" + twa);

		double bsp = 0d;
		double maxLeeway = 0d;
		try {
			maxLeeway = ((Double) cache.get(NMEADataCache.MAX_LEEWAY)).doubleValue();
		} catch (Exception ex) {
		}
		double leeway = getLeeway(awa, maxLeeway);
		cache.put(NMEADataCache.LEEWAY, new Angle180LR(leeway));
		double cmg = heading + leeway;
		cache.put(NMEADataCache.CMG, new Angle360(cmg));

		try {
			bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
		} catch (Exception ex) {
		}
		double[] cr = calculateCurrent(bsp,
				bspCoeff,
				heading,
				hdgOffset,
				leeway,
				sog,
				cog);
		cache.put(NMEADataCache.CDR, new Angle360(cr[0]));
		cache.put(NMEADataCache.CSP, new Speed(cr[1]));
	}

	public static double[] calculateTWwithGPS(double aws, double awsCoeff,
	                                          double awa, double awaOffset,
	                                          double hdg, double hdgOffset,
	                                          double sog,
	                                          double cog) {
		double twa = 0d, tws = -1d, twd = 0d;
		try {
			// Warning, the MHU is carried by the boat, that has the HDG...
			// Only if the boat is moving (ie SOG > 0)
			double diffCogHdg = 0;
			if (sog > 0d) {
				diffCogHdg = (cog - (hdg + hdgOffset));
				while (diffCogHdg < 0) diffCogHdg += 360;
				if (diffCogHdg > 180) {
//        System.out.println("- diffCogHdg > 180:" + Double.toString(diffCogHdg));
					diffCogHdg -= 360;
				}
			}
			double awaOnCOG = (awa + awaOffset) - diffCogHdg;
			double d = ((aws * awsCoeff) * Math.cos(Math.toRadians(awaOnCOG))) - (sog);
			double h = ((aws * awsCoeff) * Math.sin(Math.toRadians(awaOnCOG)));
			tws = Math.sqrt((d * d) + (h * h));
			double twaOnCOG = Math.toDegrees(Math.acos(d / tws));
			if (Double.compare(Double.NaN, twaOnCOG) == 0) {
				twaOnCOG = 0d;
			}
			if (Math.abs(awaOnCOG) > 180 || awaOnCOG < 0) {
				twaOnCOG = 360 - twaOnCOG;
			}
			if (sog > 0) {
				twd = (int) (cog) + (int) twaOnCOG;
			} else {
				twd = (int) (hdg) + (int) twaOnCOG;
			}
			while (twd > 360) twd -= 360;
			while (twd < 0) twd += 360;

			twa = twaOnCOG + diffCogHdg;
			if (twa > 180) {
				twa -= 360;
			}
			//    System.out.println("DiffCOG-HDG:" + diffCogHdg + ", AWA on COG:" + awaOnCOG + ", TWAonCOG:" + twaOnCOG);
		} catch (Exception oops) {
			oops.printStackTrace();
		}
		return new double[]{twa, tws, twd};
	}

	public static double[] calculateCurrent(double bsp, double bspCoeff,
	                                        double hdg, double hdgOffset,
	                                        double leeway,
	                                        double sog, double cog) {
		double cdr = 0d, csp = 0d;

		//  double rvX = ((bsp * bspCoeff) * Math.sin(Math.toRadians(hdg + hdgOffset)));
		//  double rvY = -((bsp * bspCoeff) * Math.cos(Math.toRadians(hdg + hdgOffset)));

		double rsX = ((bsp * bspCoeff) * Math.sin(Math.toRadians((hdg + hdgOffset) + leeway)));
		double rsY = -((bsp * bspCoeff) * Math.cos(Math.toRadians((hdg + hdgOffset) + leeway)));

		double rfX = (sog * Math.sin(Math.toRadians(cog)));
		double rfY = -(sog * Math.cos(Math.toRadians(cog)));
		double a = (rsX - rfX);
		double b = (rfY - rsY);
		csp = Math.sqrt((a * a) + (b * b));
		cdr = getDir((float) a, (float) b);

		return new double[]{cdr, csp};
	}

	public static double getDir(float x, float y) {
		double dir = 0.0D;
		if (y != 0) {
			dir = Math.toDegrees(Math.atan((double) x / (double) y));
		}
		if (x <= 0 || y <= 0) {
			if (x > 0 && y < 0) {
				dir += 180D;
			} else if (x < 0 && y > 0) {
				dir += 360D;
			} else if (x < 0 && y < 0) {
				dir += 180D;
			} else if (x == 0) {
				if (y > 0) {
					dir = 0.0D;
				} else {
					dir = 180D;
				}
			} else if (y == 0) {
				if (x > 0) {
					dir = 90D;
				} else {
					dir = 270D;
				}
			}
		}
		dir += 180D;
		while (dir >= 360D) {
			dir -= 360D;
		}
		return dir;
	}

	public static double getLeeway(double awa, double maxLeeway) {
		double _awa = awa;
		if (_awa < 0) {
			_awa += 360;
		}
		double leeway = 0D;
		if (_awa < 90 || _awa > 270) {
			double leewayAngle = maxLeeway * Math.cos(Math.toRadians(awa));
			if (_awa < 90) {
				leewayAngle = -leewayAngle;
			}
			leeway = leewayAngle;
		}
//  System.out.println("For AWA:" + awa + ", leeway:" + leeway);
		return leeway;
	}

	public static Map<Double, Double> loadDeviationHashtable(InputStream is) {
		Map<Double, Double> data = null;
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			data = loadDeviationHashtable(br);
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return data;
	}

	public static Map<Double, Double> loadDeviationHashtable(String deviationFileName) {
		Map<Double, Double> data = null;
		try {
			FileReader fr = new FileReader(deviationFileName);
			BufferedReader br = new BufferedReader(fr);
			data = loadDeviationHashtable(br);
			br.close();
			fr.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("Deviation curve data file [" + deviationFileName + "] does not exist.\n" +
							"Please change your preferences accordingly.\n" +
							"Using default [zero-deviation.csv] instead.");
			try {
				FileReader fr = new FileReader("zero-deviation.csv");
				BufferedReader br = new BufferedReader(fr);
				data = loadDeviationHashtable(br);
				br.close();
				fr.close();
			} catch (FileNotFoundException fnfe2) {
				System.err.println("Installation problem: file [zero-deviation.csv] not found.");
				System.err.println("Initializing the deviation map to all zeros");
				data = new HashMap<>();
				for (int i=0; i<=360; i+=10) {
					data.put((double)i, 0d);
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return data;
	}

	public static Map<Double, Double> loadDeviationHashtable(BufferedReader br) {
		Map<Double, Double> data = new Hashtable<Double, Double>();

		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] sa = line.split(",");
				double cm = Double.parseDouble(sa[0]);
				double d = Double.parseDouble(sa[1]);
				data.put(cm, d);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return data;
	}

	public static List<double[]> loadDeviationCurve(Map<Double, Double> data) {
		List<double[]> ret = null;

		try {
			Set<Double> set = data.keySet();
			List<Double> list = new ArrayList<>(set.size());
			for (Double d : set)
				list.add(d);
			Collections.sort(list);

			ret = new ArrayList<double[]>(list.size());
			for (Double d : list) {
				double deviation = data.get(d);
				double cm = d.doubleValue();
				ret.add(new double[]{cm, deviation});
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public static List<double[]> loadDeviationCurve(String deviationFileName) {
		List<double[]> ret = null;
		try {
			Map<Double, Double> data = loadDeviationHashtable(deviationFileName);
			ret = loadDeviationCurve(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public static Hashtable<Double, Double> loadDeviationCurve(List<double[]> data) {
		Hashtable<Double, Double> ret = new Hashtable<Double, Double>(data.size());
		try {
			for (double[] da : data) {
				ret.put(da[0], da[1]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	public static double getDeviation(double cc, List<double[]> deviationAL) {
		double deviation = 0d;
		if (deviationAL != null) {
			double prevCm = 0d, prevDev = 0;
			for (double[] dd : deviationAL) {
				if (dd[0] == cc) {
					deviation = dd[1];
					break;
				} else if (cc > prevCm && cc < dd[0]) {
					// Extrapolate
					double factor = (cc - prevCm) / (dd[0] - prevCm);
					deviation = prevDev + ((dd[1] - prevDev) * factor);
					break;
				}
				prevCm = dd[0];
				prevDev = dd[1];
			}
		}
//  System.out.println("d for " + cc + "=" + deviation);
		return deviation;
	}

	public static List<double[]> getDataForDeviation(String dataFileName) {
		List<double[]> ret = null;

		try {
			BufferedReader br = new BufferedReader(new FileReader(dataFileName));
	    /*
       * We need:
       *
       * HDG (possible mag decl), HDM, or VHW for Heading
       * RMC for COG, SOG, TimeStamp, and Mag Decl.
       * GLL for TimeStamp
       * VTG for COG & SOG
       */
			HashMap<String, Integer> counter = new HashMap<String, Integer>(4);
			counter.put("HDG", 0);
			counter.put("HDM", 0);
			counter.put("VHW", 0);
			counter.put("RMC", 0);
			counter.put("GLL", 0);
			counter.put("VTG", 0);

			String line = "";
			boolean keepLooping = true;
			while (keepLooping) {
				line = br.readLine();
				if (line == null)
					keepLooping = false;
				else {
					if (line.startsWith("$") && line.length() > 7) // then let's try
					{
						String key = line.substring(3, 6);
						if ("HDG".equals(key) ||
										"HDM".equals(key) ||
										"VHW".equals(key) ||
										"RMC".equals(key) ||
										"GLL".equals(key) ||
										"VTG".equals(key))
							counter.put(key, counter.get(key).intValue() + 1);
					}
				}
			}
			br.close();
			System.out.println("We have:");
			Set<String> keys = counter.keySet();
			for (String k : keys)
				System.out.println(k + " " + counter.get(k).intValue());
			if (counter.get("RMC").intValue() == 0 &&
							counter.get("GLL").intValue() == 0 &&
							counter.get("VTG").intValue() == 0) {
				System.err.println("No RMC, GLL, or VTG!");
			} else if (counter.get("HDG").intValue() == 0 &&
							counter.get("HDM").intValue() == 0 &&
							counter.get("VHW").intValue() == 0) {
				System.err.println("No HDM, HDG or VHW!");
			} else { // Proceed
				System.out.println("Proceeding...");
				// Ideal: RMC + HDG
				if (counter.get("RMC").intValue() > 0 &&
								(counter.get("HDG").intValue() > 0 || counter.get("HDM").intValue() > 0)) {
					System.out.println("RMC + HDG/HDM, Ideal.");
					ret = new ArrayList<double[]>(counter.get("RMC").intValue());
					// Is there a Declination?
					double decl = -Double.MAX_VALUE;
					double hdg = 0d; // (cc - D) when available
					double cog = -Double.MAX_VALUE;
					try {
						br = new BufferedReader(new FileReader(dataFileName));
						keepLooping = true;
						while (keepLooping) {
							line = br.readLine();
							if (line == null) {
								keepLooping = false;
							} else {
								if (line.startsWith("$") && line.length() > 7) { // then let's try
									String key = line.substring(3, 6);
									if ("HDG".equals(key)) {
										try {
											HDG val = StringParsers.parseHDG(line);
											if (val.getDeviation() != -Double.MAX_VALUE ||
															val.getVariation() != -Double.MAX_VALUE) {
												decl = Math.max(val.getDeviation(), val.getVariation());
											}
											hdg = val.getHeading();
											if (decl != -Double.MAX_VALUE) {
												hdg += decl;
											} else {
												hdg += ((Angle180EW) ApplicationContext.getInstance().getDataCache().get(NMEADataCache.DEFAULT_DECLINATION)).getValue();
											}
											// Write data here
											if (cog != -Double.MAX_VALUE) {
												ret.add(new double[]{hdg, cog});
											}
										} catch (Exception ex) {
										}
									} else if ("HDM".equals(key) && counter.get("HDG").intValue() == 0) {
										try {
											double hdm = StringParsers.parseHDM(line);
											if (decl != -Double.MAX_VALUE) {
												hdg = hdm + decl;
											} else {
												hdg = hdm;
											}
											// Write data here
											if (cog != -Double.MAX_VALUE) {
												ret.add(new double[]{hdg, cog});
											}
										} catch (Exception ex) {
										}
									} else if ("RMC".equals(key)) {
										try {
											RMC rmc = StringParsers.parseRMC(line);
											if (rmc.getDeclination() != -Double.MAX_VALUE) {
												decl = rmc.getDeclination();
											}
											cog = rmc.getCog();
										} catch (Exception ex) {
										}
									}
								}
							}
						}
						br.close();
						if (decl == -Double.MAX_VALUE) {
							System.out.println("No declination found.");
						} else {
							System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else if (counter.get("VTG").intValue() > 0 &&
								counter.get("GLL").intValue() > 0 &&
								(counter.get("HDM").intValue() > 0 || counter.get("HDG").intValue() > 0)) {
					ret = new ArrayList<double[]>(counter.get("GLL").intValue());
					System.out.println("VTG, GLL, (HDG or HDM), good enough");
					// Is there a Declination?
					double decl = -Double.MAX_VALUE;
					double hdg = 0d; // (cc - D) when available
					double cog = -Double.MAX_VALUE;
					try {
						br = new BufferedReader(new FileReader(dataFileName));
						keepLooping = true;
						while (keepLooping) {
							line = br.readLine();
							if (line == null) {
								keepLooping = false;
							} else {
								if (line.startsWith("$") && line.length() > 7) { // then let's try
									String key = line.substring(3, 6);
									if ("HDG".equals(key)) {
										try {
											HDG val = StringParsers.parseHDG(line);
											if (val.getDeviation() != -Double.MAX_VALUE ||
															val.getVariation() != -Double.MAX_VALUE) {
												decl = Math.max(val.getDeviation(), val.getVariation());
											}
											hdg = val.getHeading();
										} catch (Exception ex) {
										}
									} else if (counter.get("HDM").intValue() == 0 && "HDG".equals(key)) {
										hdg = StringParsers.parseHDM(line);
									} else if ("GLL".equals(key)) {
										// Just for the rhythm. Write data here
										if (cog != -Double.MAX_VALUE) {
											double delta = cog - hdg;
//                    System.out.println("HDG:" + hdg + "\272, W:" + delta + "\272");
											ret.add(new double[]{hdg, cog});
										}
									} else if ("VTG".equals(key)) {
										OverGround og = StringParsers.parseVTG(line);
										try {
											cog = og.getCourse();
										} catch (Exception ex) {
										}
										if (og == null) {
											System.out.println("Null for VTG [" + line + "]");
										}
									}
								}
							}
						}
						br.close();
						if (decl == -Double.MAX_VALUE) {
							System.out.println("No declination found.");
						} else {
							System.out.println("Declination is :" + new Angle180EW(decl).toFormattedString());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					System.out.println("Later...");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	private static String generateCacheAge(String devicePrefix, long age) {
		String std = devicePrefix + "STD,"; // StarTeD
		std += Long.toString(age);
		// Checksum
		int cs = StringParsers.calculateCheckSum(std);
		std += ("*" + StringUtils.lpad(Integer.toString(cs, 16).toUpperCase(), 2, "0"));
		return "$" + std;
	}

	public static void calculateVMGs(NMEADataCache cache) {
		double vmg = 0d;
		try {
			if (cache.get(NMEADataCache.SOG) != null &&
					cache.get(NMEADataCache.COG) != null &&
					cache.get(NMEADataCache.TWD) != null) {
				double sog = (((Speed) cache.get(NMEADataCache.SOG)).getValue());
				double cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
				double twd = (((Angle360) cache.get(NMEADataCache.TWD)).getValue());
				double twa = twd - cog;
				if (sog > 0) { // Try with GPS Data first
					vmg = sog * Math.cos(Math.toRadians(twa));
				} else {
					try {
						twa = ((Angle180) cache.get(NMEADataCache.TWA)).getValue();
						double bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
						if (bsp > 0) {
							vmg = bsp * Math.cos(Math.toRadians(twa));
						}
					} catch (Exception e) {
						vmg = 0;
					}
				}
				cache.put(NMEADataCache.VMG_ON_WIND, vmg);

				if (cache.get(NMEADataCache.TO_WP) != null && !cache.get(NMEADataCache.TO_WP).toString().trim().isEmpty()) {
					double b2wp = ((Angle360) cache.get(NMEADataCache.B2WP)).getValue();
					sog = (((Speed) cache.get(NMEADataCache.SOG)).getValue());
					cog = ((Angle360) cache.get(NMEADataCache.COG)).getValue();
					if (sog > 0) {
						double angle = b2wp - cog;
						vmg = sog * Math.cos(Math.toRadians(angle));
					} else {
						double angle = b2wp - ((Angle360) cache.get(NMEADataCache.HDG_TRUE)).getValue();
						double bsp = ((Speed) cache.get(NMEADataCache.BSP)).getValue();
						vmg = bsp * Math.cos(Math.toRadians(angle));
					}
					cache.put(NMEADataCache.VMG_ON_WP, vmg);
				}
			}
		} catch (Exception ex) {
			Context.getInstance().getLogger().log(Level.WARNING, ex.getMessage(), ex);
		}
	}

	public static void main(String... args) {
		String data = "Akeu CoucouA*FG\r\n";
		System.out.println(translateEscape(data, ALL_IN_HEXA));
		System.out.println(translateEscape(data, CR_NL));
	}
}
