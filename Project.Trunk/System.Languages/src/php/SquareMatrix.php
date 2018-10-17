<?php
declare(strict_types=1);

/**
 * Class SquareMatrix
 */
class SquareMatrix {
    /**
     * @var int
     */
    private $dimension = 0;

    /**
     * @var array
     */
    private $elements = [];

    /**
     * @var bool
     */
    private $debugMode = false;

    /**
     * SquareMatrix constructor.
     *
     * @param int $dim greater than 1
     * @param bool $init true means initialize all elements to zero.
     *
     * Throws exception if dim < 1.
     */
    public function __construct(int $dim, bool $init = true) {
        if ($dim < 1) {
            throw new Exception('Dimension must be at least 1');
        }
        $this->dimension = $dim;
        if (true === $init) {
            $this->init();
        }
    }

    public function setDebugMode(bool $mode) {
        $this->debugMode = $mode;
    }

    public function isDebugMode(): bool {
        return $this->debugMode === true;
    }

    public function init() {
        $this->elements = [];
        for ($row = 0; $row < $this->dimension; $row++) {
            $this->elements[$row] = [];
            for ($col = 0; $col < $this->dimension; $col++) {
                $this->elements[$row][$col] = 0;
            }
        }
    }

    public function getDimension(): int {
        return $this->dimension;
    }

    public function getElements(): array {
        return $this->elements;
    }

    public function setElementAt(int $row, int $col, float $val) {
        $this->elements[$row][$col] = $val;
    }

    public function getElementAt(int $row, int $col): float {
        return $this->elements[$row][$col];
    }

    /**
     * Minor
     *
     * @param int $row The row to exclude (zero based)
     * @param int $col The column to exclude (zero based)
     *
     * @return self The required minor, for (row, col).
     */
    public function minor(int $row, int $col): self {
        $small = new static($this->dimension - 1);
        for ($c = 0; $c < $this->dimension; $c++) {
            if ($c != $col) {
                for ($r = 0; $r < $this->dimension; $r++) {
                    if ($r != $row) {
                        $small->setElementAt(
                            (($r < $row) ? $r : ($r - 1)),
                            (($c < $col) ? $c : ($c - 1)),
                            $this->getElementAt($r, $c)
                        );
                    }
                }
            }
        }
        return $small;
    }

    /**
     * Determinant of a SquareMatrix. Recursive method.
     *
     * @return float The value of the Determinant
     */
    public function determinant(): float {
        $v = 0.0;
        if ($this->dimension === 1) {
            $v = $this->getElementAt(0, 0);
        } else {
            // C : column in Major
            for ($C = 0; $C < $this->dimension; $C++) { // Walk thru first line
                // Minor's determinant
                $minDet = $this->minor(0, $C)->determinant();
                $v += ($this->getElementAt(0, $C) * $minDet * ((-1.0) ** ($C + 1 + 1))); // line C, column 1
            }
        }
        if ($this->isDebugMode()) {
            echo "Determinant of {$this} is {$v}<br/>";
        }
        return $v;
    }

    /**
     * CoMatrix of a SquareMatrix.
     * Each term is replaced with the determinant of its minor.
     *
     * @return self The CoMatrix
     */
    public function coMatrix(): self {
        $co = new static($this->dimension);
        for ($r = 0; $r < $this->dimension; $r++) {
            for ($c = 0; $c < $this->dimension; $c++) {
                $co->setElementAt(
                    $r,
                    $c,
                    $this->minor($r, $c)->determinant() * ((-1) ** ($r + $c + 2))
                );  // r+c+2 = (r+1) + (c+1)...
            }
        }
        if ($this->isDebugMode()) {
            echo "CoMatrix {$co}<br />";
        }
        return $co;
    }

    /**
     * Transpose a SquareMatrix.
     * Flip columns and rows
     *
     * @return self The transposed one.
     */
    public function transposed(): self {
        $t = new static($this->dimension);
        // Replace line with columns.
        for ($r = 0; $r < $this->dimension; $r++) {
            for ($c = 0; $c < $this->dimension; $c++) {
                $t->setElementAt($r, $c, $this->getElementAt($c, $r));
            }
        }
        if ($this->isDebugMode()) {
            echo "Transposed: {$t}<br />";
        }
        return $t;
    }

    /**
     * Multiply a SquareMatrix by a number
     *
     * @param float $n Number
     *
     * @return self
     */
    public function multiply(float $n): self {
        $res = new static($this->dimension, false);
        for ($r = 0; $r < $this->dimension; $r++) {
            for ($c = 0; $c < $this->dimension; $c++) {
                $res->setElementAt($r, $c, $this->getElementAt($r, $c) * $n);
            }
        }
        if ($this->isDebugMode()) {
            echo "Multiplied by {$n}: {$res}<br />";
        }
        return $res;
    }

    /**
     * Matrix equality
     *
     * @param self $a
     * @param self $b
     *
     * @return bool true if equal, false if not
     */
    public static function equals(self $a, self $b): bool {
        if ($a->getDimension() != $b->getDimension()) {
            return false;
        }
        for ($r = 0; $r < $a->getDimension(); $r++) {
            for ($c = 0; $c < $a->getDimension(); $c++) {
                if ($a->getElementAt($r, $c) != $b->getElementAt($r, $c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Matrix inversion.
     * The inverse of a Matrix is the Transposed of the CoMatrix, multiplied by the inverse of its determinant (that
     * needs NOT to be null).
     *
     * @return self the inverted one.
     *
     * Throws Exception if det = 0
     */
    public function invert(): self {
        $det = $this->determinant();
        if ($det === 0) {
            throw new Exception('No solution, determinant = 0.');
        }

        $inv = $this->coMatrix()->transposed()->multiply(1.0 / $det);
        if ($this->isDebugMode()) {
            echo "Inverted: {$inv}<br />";
        }

        return $inv;
    }

    public function toHTML(): string
    {
        //@todo: the __toString should not return HTML.. but for now that is fine
        return (string)$this;
    }

    public function __toString() {
        $elem = "";
        $elem .= "<table>";

        $rows = count($this->elements);
        for ($row = 0; $row < $rows; $row++) {
            $elem .= "<tr>";
            $elem .= "<td>|</td>";
            $cols = count($this->elements[$row]);
            for ($col = 0; $col < $cols; $col++) {
                $elem .= "<td>";
                $elem .= $this->elements[$row][$col];
                $elem .= "</td>";
            }
            $elem .= "<td>|</td>";
            $elem .= "</tr>";
        }
        $elem .= "</table>";

        return $elem;
    }
}
