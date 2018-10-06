<?php
/**
 * Author: Olivier LeDiouris
 * Date: 10/5/18
 * Time: 9:13 AM
 */

function console_log($data){
    echo '<script>';
    echo 'console.log('. json_encode( $data ) .')';
    echo '</script>';
}

class SquareMatrix {
    private $dimension = 0;
    private $matrixElements = array();

    function SquareMatrix($dim, $init) {
        $this->dimension = $dim;
        if ($init) {
            $this->matrixElements = array();
            for ($row = 0; $row < $this->dimension; $row++) {
                $this->matrixElements[$row] = array();
                for ($col = 0; $col < $this->dimension; $col++) {
                    $this->matrixElements[$row][$col] = 0;
                }
            }
        }
    }

    function getDim() {
        return $this->dimension;
    }

    function getElements() {
        return $this->matrixElements;
    }

    function setElementAt($row, $col, $val) {
        $this->matrixElements[$row][$col] = $val;
    }

    function getElementAt($row, $col) {
        return $this->matrixElements[$row][$col];
    }
}

class MatrixUtil {

    const DEBUG = false;

    public static function printMatrix($m) {
        $elements = $m->getElements();
        $elem = "";
        $elem .= "<table>";
        for ($row=0; $row<count($elements); $row++) {
            $elem .= "<tr>";
            $elem .= "<td>|</td>";
            for ($col=0; $col<count($elements[$row]); $col++) {
                $elem .= "<td>";
                $elem .= $elements[$row][$col];
                $elem .= "</td>";
            }
            $elem .= "<td>|</td>";
            $elem .= "</tr>";
        }
        $elem .= "</table>";
        return $elem;
    }

    public static function minor($m, $row, $col) {
        $small = new SquareMatrix($m->getDim() - 1, true);
        for ($c=0; $c<$m->getDim(); $c++) {
            if ($c != $col) {
                for ($r=0; $r<$m->getDim(); $r++) {
                    if ($r != $row) {
                        $small->setElementAt((($r < $row) ? $r : ($r - 1)), (($c < $col) ? $c : ($c - 1)), $m->getElementAt($r, $c));
                    }
                }
            }
        }
        return $small;
    }

    public static function determinant($m) {
		$v = 0.0;

		if ($m->getDim() == 1) {
			$v = $m->getElementAt(0, 0);
		} else {
			// C : column in Major
			for ($C = 0; $C < $m->getDim(); $C++) { // Walk thru first line
				// Minor's determinant
				$minDet = MatrixUtil::determinant(MatrixUtil::minor($m, 0, $C));
				$v += ($m->getElementAt(0, $C) * $minDet * ((-1.0) ** ($C + 1 + 1))); // line C, column 1
			}
		}
		if (self::DEBUG) {
    	echo "Determinant of ", MatrixUtil::printMatrix($m), " is ", $v, "<br/>";
    }

		return $v;
	}

	public static function comatrix($m) {
        $co = new SquareMatrix($m->getDim(), true);
        for ($r = 0; $r < $m->getDim(); $r++) {
            for ($c = 0; $c < $m->getDim(); $c++) {
                $co->setElementAt($r, $c, MatrixUtil::determinant(MatrixUtil::minor($m, $r, $c)) * ((-1) ** ($r + $c + 2)));  // r+c+2 = (r+1) + (c+1)...
            }
		}
		if (self::DEBUG) {
            echo "Comatrix:", MatrixUtil::printMatrix($co);
        }
		return $co;
    }

    public static function transposed($m) {
		$t = new SquareMatrix($m->getDim(), true);
		// Replace line with columns.
		for ($r = 0; $r < $m->getDim(); $r++) {
			for ($c = 0; $c < $m->getDim(); $c++) {
				$t->setElementAt($r, $c, $m->getElementAt($c, $r));
			}
		}
		if (self::DEBUG) {
			echo "Transposed:", MatrixUtil::printMatrix($t);
		}
		return $t;
	}

	public static function multiply($m, $n) {
		$res = new SquareMatrix($m->getDim(), false);
		for ($r = 0; $r < $m->getDim(); $r++) {
			for ($c = 0; $c < $m->getDim(); $c++) {
				$res->setElementAt($r, $c, $m->getElementAt($r, $c) * $n);
			}
		}
		return $res;
	}

	public static function equals($a, $b) {
		if ($a->getDim() != $b->getDim()) {
			return false;
		}
		for ($r=0; $r<$a->getDim(); $r++) {
			for ($c=0; $c<$a->getDim(); $c++) {
				if ($a->getElementAt($r, $c) != $b->getElementAt($r, $c)) {
					return false;
				}
			}
		}
		return true;
	}

	public static function invert($m) {
		return MatrixUtil::multiply(MatrixUtil::transposed(MatrixUtil::comatrix($m)), (1.0 / MatrixUtil::determinant($m)));
	}

	public static function solveSystem($m, $c) {
    console_log("Solving:");
      console_log($m);
      console_log($c);
		$result = array();

		$inv = MatrixUtil::invert($m);

		// Print inverted Matrix
		if (self::DEBUG) {
			echo "Inverted:", MatrixUtil::printMatrix($inv);
		}

		// Lines * Column
		for ($row = 0; $row < $m->getDim(); $row++) {
			$result[$row] = 0.0;
			for ($col = 0; $col < $m->getDim(); $col++) {
				$result[$row] += ($inv->getElementAt($row, $col) * $c[$col]);
			}
		}
		return $result;
	}
}

?>
