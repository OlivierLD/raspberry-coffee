package celestial.almanach;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import calc.calculation.AstroComputer;
import utils.TimeUtil;


@Controller("/astro")
public class AstroController {

    @Value("${values.deltaT}")     // From application.yml
    private double defaultDeltaT;

    @Get("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getCelestialData(@Header(name = "year", defaultValue = "") String year,
                                                @Header(name = "month", defaultValue = "") String month, // Jan: 1, Dec: 12
                                                @Header(name = "day", defaultValue = "") String day,
                                                @Header(name = "hour", defaultValue = "") String hour,
                                                @Header(name = "minute", defaultValue = "") String minute,
                                                @Header(name = "second", defaultValue = "") String second,
                                                @Header(name = "deltaT", defaultValue = "") String deltaT) {

        if (!deltaT.isEmpty()) {
            AstroComputer.setDeltaT(Double.parseDouble(deltaT));
        } else {
            AstroComputer.setDeltaT(defaultDeltaT);
        }

        // Recalculate deltaT
        if (!year.isEmpty() && !month.isEmpty()) {
            double calculatedDeltaT = TimeUtil.getDeltaT(Integer.parseInt(year), Integer.parseInt(month));
            System.out.printf("Overriding Delta T with %f\n", calculatedDeltaT);
            AstroComputer.setDeltaT(calculatedDeltaT);
        }

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")); // Now
        if (!year.isEmpty() && !month.isEmpty() && !day.isEmpty() &&
            !hour.isEmpty() && !minute.isEmpty() && !second.isEmpty()) { // No param: current date. Otherwise, all prm are required
            date.set(Calendar.YEAR, Integer.parseInt(year));
            date.set(Calendar.MONTH, Integer.parseInt(month) - 1); // March = 2!
            date.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour)); // and not just Calendar.HOUR !!!!
            date.set(Calendar.MINUTE, Integer.parseInt(minute));
            date.set(Calendar.SECOND, Integer.parseInt(second));
        }

        // All calculations here
        AstroComputer.calculate(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
                date.get(Calendar.MINUTE),
                date.get(Calendar.SECOND));

        Map<String, Object> allCalculatedData = AstroComputer.getAllCalculatedData();

        return allCalculatedData; // "{ \"data\": 23.45 }";
    }
}