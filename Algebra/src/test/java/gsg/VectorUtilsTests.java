package gsg;

import java.util.Arrays;
import java.util.List;

import static gsg.VectorUtils.*;

public class VectorUtilsTests {
    /**
     * This is for tests, not UnitTest
     *
     * @param args Not used.
     */
    public static void main(String... args) {
        VectorUtils.Vector2D one = new VectorUtils.Vector2D(1, 2);
        VectorUtils.Vector2D two = new VectorUtils.Vector2D(3, 4);

        double perimeter = perimeter(Arrays.asList(one, two));
        System.out.println("Perimeter: " + perimeter);
        VectorUtils.Vector2D vector = toPolar(one);
        System.out.println("To Polar:" + vector.toString());

        VectorUtils.Vector2D translation = new VectorUtils.Vector2D(5, 6);
        List<VectorUtils.Vector2D> translated = translate(translation, Arrays.asList(one, two));
        translated.forEach(System.out::println);

        VectorUtils.Vector2D backToBase = translate(new VectorUtils.Vector2D().x(-5).y(-6), translated.get(0));
        System.out.println("Back to One: " + backToBase);

        System.out.println(String.format("Rotating 90 degrees %s and %s", one, two));
        List<VectorUtils.Vector2D> rotated = rotate(Math.toRadians(90), Arrays.asList(one, two));
        rotated.forEach(v -> System.out.printf("Rotated: %s%n", v));

        VectorUtils.Vector2D rotateOne = rotate(Math.PI * 2, one);
        System.out.println("Rotated one: " + rotateOne);

        VectorUtils.Vector2D scaled = scale(4.56, one);
        System.out.println("Scaled: " + scaled);

        VectorUtils.GraphicRange graphicRange = findGraphicRange(one, two, translation, scaled, rotateOne);
        System.out.println(String.format("X in [%.03f, %.03f], Y in [%.03f, %.03f]",
                graphicRange.getMinX(), graphicRange.getMaxX(),
                graphicRange.getMinY(), graphicRange.getMaxY()));

        System.out.println(String.format("Dot product %s \u00d7 %s: %f", one, two, dot(one, two)));

        double angle = angleBetween(one, two);
        System.out.println(String.format("Angle between %s & %s: %f", one, two, angle));
        System.out.println(String.format("In degrees: %f\272", Math.toDegrees(angle)));

        double cross = cross(one, two);
        System.out.println(String.format("Cross product(2) %s \u00d7 %s: %f", one, two, cross));

        VectorUtils.Vector3D one3 = new VectorUtils.Vector3D(1, 2, 3);
        VectorUtils.Vector3D two3 = new VectorUtils.Vector3D(4, 5, 6);
        System.out.println(String.format("Cross product(3) %s \u00d7 %s >> %s", one3, two3, cross(one3, two3)));

        System.out.println(String.format("Unit(one): %s", unit(one)));

        double comp = component(one, two);
        System.out.println(String.format("component %s \u00d7 %s: %f", one, two, comp));

        VectorUtils.Vector3D v3 = new VectorUtils.Vector3D(0, -10, 0);
        VectorUtils.Vector3D rotatedV3 = rotate(v3, 0, 0, Math.toRadians(45));
        System.out.printf("V3 %s Rotated on 45Z : %s%n", v3.toString(), rotatedV3.toString());

        VectorUtils.Vector3D v3_prime = new VectorUtils.Vector3D(1, 2, 3);
        System.out.printf("V3 %s => length: %f%n", v3_prime.toString(), length(v3_prime));
    }
}
