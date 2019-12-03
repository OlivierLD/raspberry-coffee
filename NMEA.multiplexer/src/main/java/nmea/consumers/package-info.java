/**
 * Consumers (in this package) are input channels.<br/>
 * They're made of two parts: a client, and a reader.
 * <p>
 * <ul>
 * <li>Client classes must extends {@link nmea.api.NMEAClient}, the {@link nmea.api.NMEAClient#dataDetectedEvent(nmea.api.NMEAEvent)} method talks to the Multiplexer.</li>
 * <li>Reader classes must extends {@link nmea.api.NMEAReader}, it takes care of getting the actual data.</li>
 * </ul>
 * </p>
 * A List of {@link nmea.api.NMEAClient} is created in {@link nmea.mux.GenericNMEAMultiplexer}, each of its elements is created
 * in {@link nmea.mux.MuxInitializer}, where a corresponding {@link nmea.api.NMEAReader} is created and assigned to it.
 */
package nmea.consumers;
