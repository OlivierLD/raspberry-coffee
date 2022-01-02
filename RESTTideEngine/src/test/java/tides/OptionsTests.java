package tides;

import calc.GeoPoint;
import com.google.gson.Gson;
import org.junit.Test;
import tiderest.RESTImplementation;

public class OptionsTests {

    @Test
    public void optionFormat() {
        RESTImplementation.PublishingOptions publishingOptions = new RESTImplementation.PublishingOptions();
        publishingOptions.setPosition(new GeoPoint(47.34, -3.12));
        publishingOptions.setTimeZone("Europe/Paris");
        publishingOptions.setNb(1);
        publishingOptions.setStartYear(2022);
        publishingOptions.setStartMonth(1);
        publishingOptions.setQuantity(RESTImplementation.Quantity.YEAR);
        publishingOptions.setStationName("Whatever");

        Gson gson = new Gson();
        String toJson = gson.toJson(publishingOptions);
        // Like {"startMonth":1,"startYear":2022,"nb":1,"quantity":"YEAR","position":{"latitude":47.34,"longitude":-3.12},"timeZone":"Europe/Paris","stationName":"Whatever"}
        System.out.println(toJson);
    }
}
