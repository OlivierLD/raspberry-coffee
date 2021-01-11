## Linear Algebra

Basic linear algebra ([الجبر](https://en.wikipedia.org/wiki/Algebra)), and statistical functions. Work in progress.
- Square matrix
- System resolution
- Least squares method (regression)
- Derivative
- Standard deviation and others
- etc.

---

## Java Graphics (Generic Swing Graphics, GSG)

This module also includes a _Swing based Java_ library for graphics (2 & 3D), that can be used in pure Swing applications,
as well as in Jupyter Notebooks (IJava). 

- Jupyter examples provided in the `jupyter` folder.
- Swing examples provided in the `gsg.examples` package, in the `test` folder. 

Try this:
```
$ ../gradlew runSample
```

#### Some utility methods
##### `drawBox`, `drawArrow`
Just provide the (spatial, absolute) vertices of the box to draw, same for the arrow.
Rotation and space-to-screen operations are taken care of.
```java
Consumer<Graphics2D> afterDrawer = g2d -> {
    // Draw a box
    g2d.setColor(Color.BLUE);
    // Dotted lines for the cube
    g2d.setStroke(new BasicStroke(1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            1.0f,
            new float[] { 2f, 0f, 2f },
            2f));
    double[][] boxVertex = {
            {0.0, 0.0, 1.5},
            {0.0, 2.0, 1.5},
            {2.0, 2.0, 1.5},
            {2.0, 0.0, 1.5},
            {0.0, 0.0, 0.0},
            {0.0, 2.0, 0.0},
            {2.0, 2.0, 0.0},
            {2.0, 0.0, 0.0}
    };
    box3D.drawBox.accept(g2d, boxVertex);
    // An arrow
    g2d.setColor(Color.RED);
    g2d.setStroke(new BasicStroke(2));
    box3D.drawArrow(g2d,
            new double[] {0.0, 0.0, 0.0},
            new double[] {2.0, 2.0, 1.5});
};
box3D.setAfterDrawer(afterDrawer);
```
![Drawing Box](./docimg/box.101.png)

##### `drawSurroundingBox`, `drawSegment`
```java
Consumer<Graphics2D> afterDrawer = g2d -> {
    // Define vertices
    double[] spatialPointOne = new double[] { 1d, 1d, 1d};
    double[] spatialPointTwo = new double[] { 2d, 3d, -1.5d};
    double[] spatialPointThree = new double[] { -2.5d, -2d, -1d};

    // Draw surrounding boxes
    g2d.setColor(Color.BLUE);
    // Dotted lines for the cube
    g2d.setStroke(new BasicStroke(1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            1.0f,
            new float[] { 2f, 0f, 2f },
            2f));
    box3D.drawSurroundingBox(g2d, spatialPointOne, spatialPointTwo);
    // A segment
    g2d.setStroke(new BasicStroke(2));
    box3D.drawSegment(g2d, spatialPointOne, spatialPointTwo);

    g2d.setColor(Color.RED);
    // Dotted lines for the cube
    g2d.setStroke(new BasicStroke(1,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            1.0f,
            new float[] { 2f, 0f, 2f },
            2f));
    box3D.drawSurroundingBox(g2d, spatialPointTwo, spatialPointThree);
    // A segment
    g2d.setStroke(new BasicStroke(2));
    box3D.drawSegment(g2d, spatialPointTwo, spatialPointThree);
};
box3D.setAfterDrawer(afterDrawer);
```
![Drawing Box](./docimg/box.102.png)

---
