<!DOCTYPE html>
<html lang="en">
<head>
	<link rel="shortcut icon" type="image/png" href="php.png">
</head>
<body style="font-family: 'Source Code Pro', 'Courier New', Helvetica, Geneva">
<h1>Matrix Utils tests</h1>
<h2>System Resolution</h2>

<?php

include __DIR__ . '/autoload.php';

$matrix = new SquareMatrix(3, true);
$matrix->setElementAt(0, 0, 12);
$matrix->setElementAt(0, 1, 13);
$matrix->setElementAt(0, 2, 14);

$matrix->setElementAt(1, 0, 1.345);
$matrix->setElementAt(1, 1, -654);
$matrix->setElementAt(1, 2, 0.001);

$matrix->setElementAt(2, 0, 23.09);
$matrix->setElementAt(2, 1, 5.3);
$matrix->setElementAt(2, 2, -12.34);

echo "Matrix dim: {$matrix->getDimension()} <br />";
$elements = $matrix->getElements();
echo "Det: {$matrix->determinant()} <br />";
echo "<hr/> == Matrix == <br />{$matrix}<br />";
$constants = [234, 98.87, 9.876];
$coeffs    = SystemSolver::solveSystem($matrix, $constants);
echo "<hr />System Resolution:<br/>";
$count = count($coeffs);

for ($i = 0; $i < $count; $i++) {
    echo "Coeff, deg ", ($count - 1 - $i), " : ", $coeffs[$i], "<br/>";
}

?>

</body>
</html>
