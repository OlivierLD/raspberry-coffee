<!DOCTYPE html>
<html lang="en">
<head>

</head>
<body style="font-family: 'Source Code Pro', 'Courier New', Helvetica, Geneva">
<h4>Input System and Constants</h4>
<?php
$dim = $_POST["dim"];
echo "Dim={$dim}";
?>
<form method="post" action="resolution.php" target="">

    <?php

    $varNames = [
        'A', 'B', 'C', 'D', 'E', 'F', 'G', "H", 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    ];

    echo "<input type='hidden' name='dim' value='{$dim}'>";
    ?>
    <table>
        <?php

        for ($col = 0; $col < $dim; $col++) {
            echo "<th>", $varNames[$col], "</th>";
        }
        echo "<th>&nbsp;</th>";
        echo "<th>const</td>";

        for ($row = 0; $row < $dim; $row++) {
            echo "<tr>";
            for ($col = 0; $col < $dim; $col++) {
                echo "<td><input type='number' step='0.000001' name='row-".$row."-col-".$col."' placeholder='", ($row.
                                                                                                                 "x".
                                                                                                                 $col), "' style=\"width: 80px; text-align: right;\"/></td>";
            }
            echo "<td>&nbsp;:&nbsp;</td>";
            echo "<td><input type='number' step='0.000001' name='coeff-".$row.
                 "' placeholder='", $row, "' style=\"width: 80px; text-align: right;\"/></td>";
            echo "</tr>";
        }
        ?>
    </table>

    <input type="submit" value="Solve it!"/>

</form>

</body>
</html>
