package calc.calculation.nauticalalmanacV2;

public class ContextV2 {
	public final static double EPS0_2000 = 23.439291111; // Reference
	// Global Variables
	public double T, T2, T3, T4, T5, TE, TE2, TE3, TE4, TE5, Tau, Tau2, Tau3, Tau4, Tau5, deltaT;
	public double eps0, eps, delta_psi, delta_eps;
	public double Le, Be, Re;
	public double kappa, pi0, e;
	public double lambda_sun, RAsun, DECsun, GHAsun, SDsun, HPsun, EoT;
	public double RAvenus, DECvenus, GHAvenus, SDvenus, HPvenus;
	public double RAmars, DECmars, GHAmars, SDmars, HPmars;
	public double RAjupiter, DECjupiter, GHAjupiter, SDjupiter, HPjupiter;
	public double RAsaturn, DECsaturn, GHAsaturn, SDsaturn, HPsaturn;
	public double RAmoon, DECmoon, GHAmoon, SDmoon, HPmoon, moonEoT;
	public double RApol, DECpol, GHApol; //, RApolaris, DECpolaris, GHApolaris;
	public double OoE, tOoE, LDist, starMoonDist;

	public double moonJupiterDist, moonVenusDist, moonMarsDist, moonSaturnDist;

	public double moonPhase;

	public double JD0h, JDE, JD;
	public double lambda, beta, dES, lambdaMapp, dayfraction;

	public double GHAAtrue, Lsun_mean, Lsun_true, k_moon, k_venus, k_mars, k_jupiter, k_saturn;

	public double GHAstar, SHAstar, DECstar;
}
