(ns samples.two)

(def lampo1 [-20 -18 -30 -5 7 15 25 18 7 0 -15 -10])
(def lampo2 [-21 -11 -31 -5 8 16 35 18 1 2 -15 -20])

(defn -main
  [& args]
  (let [keskiarvot (map #(/ (reduce + %) (count %)) (map vector lampo1 lampo2))]
    (println keskiarvot)
    (let [positiiviset (filter pos? keskiarvot)]
      (println positiiviset)
      (let [posKeskiarvo (double (/ (reduce + positiiviset) (count positiiviset)))]
        (println posKeskiarvo)))))
