package gsg;

import java.util.Arrays;
import java.util.List;

import static gsg.VectorUtils.Vector2D;
import static gsg.VectorUtils.angleBetween;
import static gsg.VectorUtils.component;
import static gsg.VectorUtils.cross;
import static gsg.VectorUtils.dot;
import static gsg.VectorUtils.findGraphicRange;
import static gsg.VectorUtils.length;
import static gsg.VectorUtils.multiply;
import static gsg.VectorUtils.perimeter;
import static gsg.VectorUtils.rotate;
import static gsg.VectorUtils.scale;
import static gsg.VectorUtils.toPolar;
import static gsg.VectorUtils.translate;
import static gsg.VectorUtils.unit;

public class VectorUtilsTests {
    /**
     * This is just for tests, not unit-tests
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

        System.out.println(String.format("Dot (2D) product (%s).(%s) : %f", one, two, dot(one, two)));

        VectorUtils.Vector2D oneV2 = new Vector2D(-1, 3);
        VectorUtils.Vector2D twoV2 = new Vector2D(3, 1);
        System.out.println(String.format("Dot (2D) product of 2 square ones (%s).(%s) = %f",
                oneV2,
                multiply(twoV2, 3.3),
                dot(oneV2, multiply(twoV2, 3.3))));

        double angle = angleBetween(one, two);
        System.out.println(String.format("Angle between %s & %s: %f", one, two, angle));
        System.out.println(String.format("In degrees: %f\272", Math.toDegrees(angle)));

        double cross = cross(one, two);
        System.out.println(String.format("Cross product(2) %s \u00d7 %s: %f", one, two, cross));

        VectorUtils.Vector3D one3 = new VectorUtils.Vector3D(1, 2, 3);
        VectorUtils.Vector3D two3 = new VectorUtils.Vector3D(4, 5, 6);
        System.out.println(String.format("Cross product(3) %s \u00d7 %s >> %s", one3, two3, cross(one3, two3)));
        System.out.println(String.format("Dot (3D) product %s \u00d7 %s: %f", one3, two3, dot(one3, two3)));
        System.out.println(String.format("Angle (3D) between %s and %s: %f rad (%f\272)", one3, two3, angleBetween(one3, two3), Math.toDegrees(angleBetween(one3, two3))));

        System.out.println(String.format("Unit(one): %s", unit(one)));

        double comp = component(one, two);
        System.out.println(String.format("component %s \u00d7 %s: %f", one, two, comp));

        VectorUtils.Vector3D v3 = new VectorUtils.Vector3D(0, -10, 0);
        VectorUtils.Vector3D rotatedV3 = rotate(v3, 0, 0, Math.toRadians(45));
        System.out.printf("V3 %s Rotated on 45Z : %s%n", v3.toString(), rotatedV3.toString());

        VectorUtils.Vector3D v3_prime = new VectorUtils.Vector3D(1, 2, 3);
        System.out.printf("V3 %s => length: %f%n", v3_prime.toString(), length(v3_prime));

        // For Getting Started with NLP
        Vector2D query = new Vector2D().x(1).y(1);
        Vector2D doc1 = new Vector2D().x(3).y(5);
        double cosine = dot(query, doc1) / (length(query) * length(doc1));
        System.out.printf("Cosine: %.016f\n", cosine);

        Vector2D query90 = new Vector2D().x(-1).y(1);
        System.out.printf("Orthogonal dot product: %f\n", dot(query, query90));

        one =  new Vector2D().x(1).y(1);
        two =  new Vector2D().x(1.1).y(0.9);
        System.out.printf("Convergent dot product: %f\n", dot(one, two));
        two =  new Vector2D().x(-1.1).y(0.0);
        System.out.printf("Divergent dot product: %f\n", dot(one, two));
    }
}
