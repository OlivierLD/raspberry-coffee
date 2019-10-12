## Magnetometer calibration
Use the `csv` file, open it as a spreadsheet (I use LibreOffice).

Select columns `magX` and `magY`, and insert chart.

![MagX-MagY](./magX-magY.png)

The calibration parameters should re-center the circle on `[0, 0]` and make the figure round instead of oval.

- Create new cells, `X offset`, `X coeff`, `Y offset`, `Y coeff`
- Then create new columns, `new MagX` as `=$Q$3*(J2 + $Q$2)`, and `new MagY` as `=$Q$5*(K2 + $Q$4)`, drag each column down to the bottom of the table.
- Then adjust the offsets and coeffs until you reach the expected result

![Adjusted](./Adjusted.png)

See on the figure above, the circle has a (almost) constant radius of ~40, centered on `[0, 0]`.
The parameters to remember are on the top left.
