package astro.sample;

import calc.calculation.nauticalalmanacV2.ContextV2;
import calc.calculation.nauticalalmanacV2.Core;

public class CoreV2Test {

    public static void main(String... args) {
        ContextV2 context = new ContextV2();

        Core.julianDate(context, 2009, 4, 20, 0, 0, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
        Core.julianDate(context, 2009, 4, 20, 0, 10, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
        Core.julianDate(context, 2009, 4, 20, 0, 20, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
        Core.julianDate(context, 2009, 4, 20, 0, 30, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
        Core.julianDate(context, 2009, 4, 20, 0, 40, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
        Core.julianDate(context, 2009, 4, 20, 0, 50, 0f, 65.5);
        System.out.println("DayFraction:" + context.dayfraction);
    }
}
