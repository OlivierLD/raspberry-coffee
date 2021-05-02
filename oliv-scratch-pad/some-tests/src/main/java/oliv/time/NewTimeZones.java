package oliv.time;

//import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

/**
 * Uses modern classes:
 * See java.time.Instant
 *     java.time.OffsetDateTime
 *     java.time.ZonedDateTime
 *     java.time.LocalDateTime
 *     java.time.LocalDate
 *     java.time.LocalTime
 *     java.time.OffsetTime
 */
public class NewTimeZones {

    public static void main(String... args) {

//        final Instant now = Instant.now();
        ZoneId zoneId = ZoneId.systemDefault() ;
//        ZonedDateTime zdtHome = now.atZone(zoneId);
        OffsetDateTime offsetDateTime = OffsetDateTime.now(zoneId);
        System.out.printf("Now, here: %s (UTC%s)\n", offsetDateTime.toString(), offsetDateTime.getOffset().toString());

        ZoneId.getAvailableZoneIds()
                .forEach(tzId -> {
                    try {
                        ZoneId zone = ZoneId.of(tzId);
//                    ZonedDateTime zdt = now.atZone(zone);
                        OffsetDateTime _offsetDateTime = OffsetDateTime.now(zone);
                        System.out.printf("At %s, it is %s (UTC %s)\n", tzId, _offsetDateTime.toString(), _offsetDateTime.getOffset().toString());
                    } catch (ZoneRulesException zre) {
                        System.err.println(zre.toString());
                    }
                });
    }
}
