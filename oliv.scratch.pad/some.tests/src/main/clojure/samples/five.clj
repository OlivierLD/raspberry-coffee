(let [dim (count [1, 2, 3, 4])]
  (println "will loop" dim " time(s)")
  (def acc 0)
  (for [row (range dim)]
    (if (< row dim)
      (var-set acc (+ acc 1))
      (println "Row" (str row) (str acc))))
  (println "Acc" (str acc)))

(println "----------------------")

(let [dim (count [1, 2, 3, 4])]
  (println "will loop" dim " time(s)")
  (def acc 0)
  (for [row (range dim)]
    (if (< row dim)
      (inc acc)
      (println "Row" (str row) (str acc))))
  (println "Acc" (str acc)))

(println "----------------------")
