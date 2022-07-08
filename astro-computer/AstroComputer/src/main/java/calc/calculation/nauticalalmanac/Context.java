package calc.calculation.nauticalalmanac;

public class Context {
	public final static double EPS0_2000 = 23.439291111; // Reference
	// Global Variables
	public static double T, T2, T3, T4, T5, TE, TE2, TE3, TE4, TE5, Tau, Tau2, Tau3, Tau4, Tau5, deltaT;
	public static double eps0, eps, delta_psi, delta_eps;
	public static double Le, Be, Re;
	public static double kappa, pi0, e;
	public static double lambda_sun, RAsun, DECsun, GHAsun, SDsun, HPsun, EoT;
	public static double RAvenus, DECvenus, GHAvenus, SDvenus, HPvenus;
	public static double RAmars, DECmars, GHAmars, SDmars, HPmars;
	public static double RAjupiter, DECjupiter, GHAjupiter, SDjupiter, HPjupiter;
	public static double RAsaturn, DECsaturn, GHAsaturn, SDsaturn, HPsaturn;
	public static double RAmoon, DECmoon, GHAmoon, SDmoon, HPmoon, moonEoT;
	public static double RApol, DECpol, GHApol; //, RApolaris, DECpolaris, GHApolaris;
	public static double OoE, tOoE, LDist, starMoonDist;

	public static double moonJupiterDist, moonVenusDist, moonMarsDist, moonSaturnDist;

	public static double moonPhase;

	public static double JD0h, JDE, JD;
	public static double lambda, beta, dES, lambdaMapp, dayfraction;

	public static double GHAAtrue, Lsun_mean, Lsun_true, k_moon, k_venus, k_mars, k_jupiter, k_saturn;

	public static double GHAstar, SHAstar, DECstar;
}
