(ns systems.matrix)

;clojure.core.matrix

; The data for the sytem resolution
(def matrix [[12.0, 13.0, 14.0], [1.345, -654, 0.001], [23.09, 5.3, -12.34]])
(def coeffs [234, 98.87, 9.876])

(defn list-length [x]
  (if (= (first x) nil)
    0
    (+ 1 (list-length (rest x)))))

(defn minor "Calculate the minor of a matrix"
  [mat, row, col]
  ()
  0)

(defn transpose [mat]
  "Matrix transposition"
  (loop [matrix mat,
         transp [],
         i      0]
    (if (< i (count (nth matrix 0)))
      (recur matrix
        (conj transp
              (vec
                (reduce concat
                        (map #(conj [] (nth %1 i))
                             matrix))))
        (inc i))
      transp)))

(defn determinant
  "Calculate the determinant of a matrix"
  [mat]
  (
    (println "Calculating determinant matrix dim" (count mat))
    (let [res (int 0)]
      (let [dim (count mat)]
        (println "In the loop, looping on" dim "row(s)")
        (let [res (+ res dim)]
        (println "Res is now" res) )
        (for [row (range dim)]
          ((println "Row" (int row))
            (let [res (+ 1 res)])))
        ) res))
  )

; return value


(defn -main
  "Matrix Utilities"
  [& args] ; prms
; (try
    ((println "More soon about matrixes")
      (println "Matrix dim:" (count matrix))
      (println "Transposed" (transpose matrix))
      (let [min (minor matrix 0 0)] ; minor calculated
        ;(println (list-length '(this is a list)) "should be 4")
        (println "Det:" (determinant matrix))
       ;(println "DetMin:" (determinant min))
        (println "Done")))
;   (catch Exception ex (println (str "Caught this: " (.toString ex)))) ; Fix that
;   (finally (println "Finally"))))
  )
