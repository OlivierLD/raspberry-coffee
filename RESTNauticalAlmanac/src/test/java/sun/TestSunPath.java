package sun;

import astrorest.RESTImplementation;
import nmea.parser.StringParsers;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSunPath {

    @Test
	public void rawSunPath() {
        try {
            String date = "2011-02-06T14:41:42.000Z";
            double lat = -10.761383333333333, lng = -156.24046666666666;
            long ld = StringParsers.durationToDate(date); // Returns epoch
            System.out.println(date + " => " + new Date(ld));

            Calendar refDate = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            refDate.setTimeInMillis(ld);

            System.out.println("From Calendar:" + date + " => " + refDate.getTime());

            RESTImplementation me = new RESTImplementation(null);
            List<RESTImplementation.BodyAt> sunPath = me.getSunDataForAllDay(lat, lng, 20, refDate);
            // See https://www.epochconverter.com/?source=searchbar&q=to+date
            // System.out.println("Yo!");
            assertTrue("Size should be 73", sunPath.size() == 73);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
	}
}
