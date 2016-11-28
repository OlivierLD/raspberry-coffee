package nmea.api;

/**
 * Anything that can happen during the NMEA Stream reading
 * 
 * @version 1.0
 * @author Olivier Le Diouris
 */
public class NMEAException extends Exception 
{
  public NMEAException()
  {
    super();
  }
  public NMEAException(String s)
  {
    super(s);
  }
}