(ns clojure-learning.core)

(defn list-length [x] (
                        if (= (first x) nil)
                           0
                           (+ 1 (list-length (rest x)))))

(defn -main
  "My first Clojure program"
  []
  (println (list-length '(this is a list)) "should be 4"))

