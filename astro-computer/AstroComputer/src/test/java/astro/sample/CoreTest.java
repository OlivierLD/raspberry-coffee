package astro.sample;

import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;

public class CoreTest {

    public static void main(String... args) {
        Core.julianDate(2009, 4, 20, 0, 0, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
        Core.julianDate(2009, 4, 20, 0, 10, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
        Core.julianDate(2009, 4, 20, 0, 20, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
        Core.julianDate(2009, 4, 20, 0, 30, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
        Core.julianDate(2009, 4, 20, 0, 40, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
        Core.julianDate(2009, 4, 20, 0, 50, 0f, 65.5);
        System.out.println("DayFraction:" + Context.dayfraction);
    }

}
