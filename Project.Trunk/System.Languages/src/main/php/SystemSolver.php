<?php
/**
 * CCI - Web Factory - PoC.
 *
 * @author    Sebastien Morel <s.morel@novactive.com>
 * @copyright 2018 CCI
 * @license   Proprietary
 */
declare(strict_types=1);

/**
 * Class MatrixSolver
 */
class SystemSolver {
    /**
     * @param SquareMatrix $m
     * @param array        $c Array of constants
     *
     * @return array Result coefficients
     *
     * Throws Exception when no solution (det = 0).
     */
    public static function solveSystem(SquareMatrix $m, array $c): array {
        $result = [];
        try {
            $inv = $m->invert();
            // Lines * Column
            for ($row = 0; $row < $m->getDimension(); $row++) {
                $result[$row] = 0.0;
                for ($col = 0; $col < $m->getDimension(); $col++) {
                    $result[$row] += ($inv->getElementAt($row, $col) * $c[$col]);
                }
            }
        } catch (Exception $ex) {
            throw $ex;
        }
        return $result;
    }
}
