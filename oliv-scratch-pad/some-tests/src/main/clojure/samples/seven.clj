(defmacro for-loop [[sym init check change :as params] & steps]
  `(loop [~sym ~init value# nil]
    (if ~check
      (let [new-value# (do ~@steps)]
        (recur ~change new-value#))
      value#)))

(println "Running for-loop")
(for-loop [i 0 (< i 10) (inc i)]
          (println "> " i))

(println "Running for loop")
(for [i (range 10)]
  (inc i))

; ---------
(defn some-stuff
  "Does stuff"
  [idx]
  (println (inc idx)))

(println "Doing stuff")
(some-stuff 10)

; -----------
(println "Running for-loop")
(for-loop [i 0 (< i 10) (inc i)]
          (some-stuff i))

(println "Running for loop")
(for [i (range 10)]
  (some-stuff i))
