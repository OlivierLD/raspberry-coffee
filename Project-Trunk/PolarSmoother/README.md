### Elaborating your own polars

![Polar](./polar.jpg)

Polars are those curves predicting the speed of the boat (Boat SPeed **BSP** or Speed Through Water **STW**) for given true wind speed (TWS) and true wind angle (TWA). Without the polars of your boat, there is no way to compute any routing, it is as simple as that.

The tool presented here can help you elaborate your own polars. It's written in Java, using Swing. It needs to run in a graphical environment.

You can run it from `gradle`:
```
 $ ../../gradlew runSmoother
```
or after a `$ ../../gradlew shadowJar`, run the script named `smoother.sh`
```
 $./smoother.sh
```
You can create new files or use already existing ones, change smoothing parameters, drag points directly on the graph (right pane), etc.

Menu bar will give you access to the various feature, as well as contextual popup menus in the left pane.

The files the tool rely on (`*.polar-data` and `*.polar-coeff`) are XML files, they can be edited as any text document. But the graphical interface
provided here is usually easier to deal with.

| Polars for all TWS | Polars for TWS = 20 knots |
|:------------------:|:-------------------------:|
|![All TWS](./docimg/screenshot.01.png)|![TWS=20](./docimg/screenshot.02.png)|

They are called polars, because they are represented around a pole (the center of the true wind angles), each point being defined by its angle and its radius. Another representation commonly used for curves is the Cartesian way, with abscissa (x) and ordinates (y). This representation is also available in this tool.
For a given TWS and TWA, the boat speed is represented by the distance between the center (the pole) and the curve.
Polars elaboration is the process that will smooth the logged points (provided by the designer of the boat, or by some logging) so you can come up with a bunch of coefficients used to calculate the boat speed for any true wind speed and any true wind angle at any time.
Notice on the snapshot above that the polars can – and often will – be made out of different sections, typically one for upwind, and one for downwind. But anyway, there is no limit in the number of sections used for the polars of a given boat.
But be warned, this is a long and uneasy process.

MaxSea has its own polar format, the polar tool is aware of it, and can digest those data.

The polars used later are stored as XML files containing the coefficients of the polynomials that can be used to represent the polars.
For a given TWS, the STW is given for any TWA by a polynomial function like


> STW = f<small><sup>TWS</sup></small>(TWA)</span>

where f<small><sup>TWS</sup></small> is a polynomial function of degree `n`, like
> f<small><sup>TWS</sup></small>(TWA) = (Coeff<small><sub>0</sub></small> * TWA<small><sup>0</sup></small>) + (Coeff<small><sub>1</sub></small> * TWA<small><sup>1</sup></small>) + (Coeff<small><sub>2</sub></small> * TWA<small><sup>2</sup></small>) + ... + (Coeff<small><sub>n</sub></small> * TWA<small><sup>n</sup></small>)

also written

![f(TWA)](./docimg/ftwa.gif)


<!--
|  |    |  |
|--:|:--:|:--|
|                                      |   <small>n</small>     |                                                    |
|f<small><sup>TWS</sup></small>(TWA) = | &Sigma; | Coeff<small><sub>i</sub></small> * TWA<small><sup>i</sup></small> |
|                                      |  <small>i=0</small>    |                                                    |
-->
Those coefficients can be obtained from raw data, by using the least squares method.

Those coefficients can themselves be smoothed across the true wind speed, with the same least square method, to obtain a function that would compute the coefficients for any TWS.

This way, a boat speed can be obtained for any true wind speed and true wind angle.

Some boats can have very different behaviors depending on their point of sail. This is why we have here the possibility to have several sections in the polars. Like one for upwind, and one for downwind, when the kite is up. You can have as many sections as you want.

The smoothing is done section by section, the parameters of the smoothing are set at this level, they are accessible with a right-click on the section in the tree in the left pane (**Edit Node**).

![Edit Node](./docimg/screenshot.03.png)

Notice the two degrees, one for the polar curves, and another one used to smooth the coefficients of the curves.
The sections are sorted in the tree by name.
It is your responsibility to name them so they're displayed in a significant order.

Two kinds of files are managed by this tool.

The files that contain the data to smooth, namely the boat speed for given tuples (TWS, TWA), have an extension like `.polar-data`.

The files that contained the final coefficients, needed for the routing, have an extension like `.polar-coeff`.

Those two sorts of files are XML files, and can be read and edited in any text editor. Each of them will be validated against an XML Schema. This means that if you have modified those files with an editor, if they're not valid (i.e. screwed up during the modification), you will have an error message when re-opening them using this tool.

To produce the `polar-coeff` file, ultimately used to calculate the routing, you modify the points and parameters of the smoothing until you are happy with the representation, and you use the menu `File > Generate Polar Coeff...`

You will be prompted to give this file a name, and then you will need to mention this file when computing the routing.

Ultimately, you will end up with a 3D smoothed polar surface, you will be able to find the Boat Speed for any wind speed, at any angle,
as illustrated by the 3D view in the tool.

![3D View](./docimg/screenshot.04.png)

👉 This tool also understands the format used by MaxSea, the files with a `.pol` extension. The can be read (`File > Import from MaxSea...`), or produced (`File > Export to MaxSea...`).

Those files are text files, looking like this:

|TWA|2|3|4|5|6|7|8|9|10|11|...|
|---|-|-|-|-|-|-|-|-|--|--|---|
|45|2.0|3.0|3.0|4.0|5.0|6.0|6.0|9.0|12.0|13.0|...|
|50|2.0|3.0|4.0|5.0|6.0|7.0|7.0|10.0|13.0|14.0|...|
|55|2.0|3.0|4.0|5.0|6.0|7.0|8.0|11.0|13.0|14.0|...|
|...|
