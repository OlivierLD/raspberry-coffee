/**
 * Contains entities related to the NMEA data reading
 * and re-broadcasting.
 * <br>
 * Follows the Model-View-Controller (MVC) Pattern
 *
 * <ul>
 * <li>Model: {@link nmea.api.NMEAReader}</li>
 * <li>View: {@link nmea.api.NMEAClient}</li>
 * <li>Controller: {@link nmea.api.NMEAParser}</li>
 * </ul>
 * The client application only needs to create an {@link nmea.api.NMEAClient} to which it
 * passes an {@link nmea.api.NMEAReader} along with a list of {@link nmea.api.NMEAListener}.<br>
 * The {@link nmea.api.NMEAParser} is taken care of by the abstract {@link nmea.api.NMEAClient} class.
 * <br>
 * See the {@link nmea.api.NMEAClient} javadoc for further details.
 *
 * <ul>
 * <li>The {@link nmea.api.NMEAReader} reads the actual NMEA stream (Serial Port, Log File, TCP socket, etc).</li>
 * <li>The {@link nmea.api.NMEAParser} identifies the NMEA Sentences and isolates them.</li>
 * <li>The {@link nmea.api.NMEAClient} is notified when an NMEA sentence has been detected.</li>
 * </ul>
 */
package nmea.api;