package utils;

public class WeatherUtil {

	/**
	 * Compliant with http://www.dpcalc.org/
	 *
	 * @param hum in %
	 * @param temp in Celcius
	 * @return in Celcius
	 */
	public static double dewPointTemperature(double hum, double temp) {
		double dewPointTemp = 0d;
		double c1 = 6.10780;
		double c2 = (temp > 0) ? 17.08085 : 17.84362;
		double c3 = (temp > 0) ? 234.175 : 245.425;

		double pz = c1 * Math.exp((c2 * temp) / (c3 + temp));
		double pd = pz * (hum / 100d);

		dewPointTemp = (- Math.log(pd / c1) * c3) / (Math.log(pd / c1) - c2);

		return dewPointTemp;
	}

}
