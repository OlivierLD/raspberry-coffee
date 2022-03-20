package sun;

import astrorest.RESTImplementation;
import calc.calculation.AstroComputerV2;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
//            String date = "2011-02-06T14:41:42.000Z";
//            double lat = -10.761383333333333, lng = -156.24046666666666;

            String date = "2022-03-20T10:41:42.000Z";
            double lat = 47.661667, lng = -2.758167;

            long ld = StringParsers.durationToDate(date); // Returns epoch
            System.out.println(date + " => " + new Date(ld));

            Calendar refDate = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            refDate.setTimeInMillis(ld);

            System.out.println("From Calendar:" + date + " => " + refDate.getTime());

            RESTImplementation me = new RESTImplementation(null);

            // RESTImplementation.BodyDataForPos bodyData = me.getSunDataForDate(lat, lng, refDate);

            List<RESTImplementation.BodyAt> sunPath = me.getSunDataForAllDay(lat, lng, 20, refDate); // Sun Path?
            // See https://www.epochconverter.com/?source=searchbar&q=to+date
            // System.out.println("Yo!");
            ObjectMapper mapper = new ObjectMapper();
            // mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sunPath);
            System.out.println(json);
            // 73 = (3 * 24) + 1, where 3 = 60 / 20, 20 being the step.
            assertTrue("Size should be 73", sunPath.size() == 73);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
	}

    @Test
    public void rawSunData() {
        try {
//            String date = "2011-02-06T14:41:42.000Z";
//            double lat = -10.761383333333333, lng = -156.24046666666666;

            String date = "2022-03-20T10:41:42.000Z";
            double lat = 47.661667, lng = -2.758167;

            long ld = StringParsers.durationToDate(date); // Returns epoch
            System.out.println(date + " => " + new Date(ld));

            Calendar refDate = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
            refDate.setTimeInMillis(ld);

            System.out.println("From Calendar:" + date + " => " + refDate.getTime());

            RESTImplementation me = new RESTImplementation(null);

            RESTImplementation.BodyDataForPos bodyData = me.getSunDataForDate(lat, lng, refDate);

            boolean displayJson = true;
            if (displayJson) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(bodyData);
                System.out.println(json);
            }
            // Display some Sun Data
            Date riseDateTime = new Date(bodyData.getRiseTime());
            Date transitDateTime = new Date(bodyData.getSunTransitTime());
            Date setDateTime = new Date(bodyData.getSetTime());
            System.out.println("Sun Rise   :" + riseDateTime);
            System.out.println("Sun Transit:" + transitDateTime);
            System.out.println("Sun Set    :" + setDateTime);

//            assertTrue("Bad epoch", bodyData.getEpoch() == 1_297_003_302_000L);
//            assertTrue("Bad Decl", bodyData.getDecl() == -15.600935281704992);
//            assertTrue("Bad GHA", bodyData.getGha() == 36.91262094944125);
//            assertTrue("Bad EoT", bodyData.getEot() == -14.087231881717116);

            assertTrue("Bad epoch", bodyData.getEpoch() == 1_647_772_902_000L);
            assertTrue("Bad Decl", bodyData.getDecl() == -0.08003528415023496);
            assertTrue("Bad GHA", bodyData.getGha() == 338.55586541758504);
            assertTrue("Bad EoT", bodyData.getEot() == -7.382407987207898);
            // TODO More...

            // It went well

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
