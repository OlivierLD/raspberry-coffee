(ns samples.four)

(defn factorial [n]
  (if (= n 1)
    1
    (* (factorial (dec n)) n)
    )
  )

(defn -main
  [& args]
  (println(factorial 5))
  )
