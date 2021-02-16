package utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DeltaTComputer {

    /**
     * TODO Enter the date as parameter
     * @param args
     */
    public static void main(String... args) {

        Calendar now = GregorianCalendar.getInstance();
        double deltaT = TimeUtil.getDeltaT(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
        // Duh
        System.out.println(String.format("=> %f", deltaT));
    }
}
