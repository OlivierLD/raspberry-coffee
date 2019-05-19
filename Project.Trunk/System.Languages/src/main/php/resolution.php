<!DOCTYPE html>
<html lang="en">
<head>
	<title>System resolution</title>
	<link rel="shortcut icon" type="image/png" href="php.png">
</head>
<body style="font-family: 'Source Code Pro', 'Courier New', Helvetica, Geneva">
<h2>Solution</h2>

<?php

include __DIR__ . '/autoload.php';

try {
    $dim            = $_POST["dim"];
    $matrixElements = [];
    $coefficients   = [];

    for ($row = 0; $row < $dim; $row++) {
        for ($col = 0; $col < $dim; $col++) {
            $matrixElements[$row][$col] = $_POST["row-".$row."-col-".$col];
        }
    }

    for ($row = 0; $row < $dim; $row++) {
        $coefficients[$row] = $_POST["coeff-".$row];
    }

    $matrix = new SquareMatrix($dim, true);

    for ($row = 0; $row < $dim; $row++) {
        for ($col = 0; $col < $dim; $col++) {
            $matrix->setElementAt($row, $col, (float) $matrixElements[$row][$col]);
        }
    }

    echo "Matrix dim: {$matrix->getDimension()} <br />";
    $elements = $matrix->getElements();
    echo "<br/>";
    echo "Det: {$matrix->determinant()}<br />";
    echo "<hr/>Matrix<br />";
    echo $matrix;
    echo "<hr/>";
    $constants = [];
    for ($row = 0; $row < $dim; $row++) {
        $constants[$row] = $coefficients[$row];
    }
    echo "Constants: ", implode(", ", $constants);
    echo "<hr/>";

    try {
        $coeffs = SystemSolver::solveSystem($matrix, $constants);

        $coeffNames = [
            'A', 'B', 'C', 'D', 'E', 'F', 'G', "H", 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        ];

        // Results
        echo "System Solution:<br/>";
        $count = count($coeffs);
        for ($i = 0; $i < $count; $i++) {
            echo $coeffNames[$i], " : ", $coeffs[$i], "<br/>";
        }
    } catch (Exception $ex) {
        echo "System resolution: ", $ex->getMessage(), "\n";
    }
} catch (Exception $ex) {
    echo 'Caught exception: ', $ex->getMessage(), "\n";
}
?>

<hr/>
<span style="font-style: italic">&copy; OlivSoft, 2018</span>
</body>
</html>
