(ns samples.three
    (:gen-class))

(defn Example []
  (try
    (aget (int-array [1 2 3]) 5)
    (catch Exception e (println (str "caught exception: " (.toString e))))
    (finally (println "This is our final block")))
  (println "Let's move on"))
(Example)
