(ns example.invokejava
  (:import test.clojure.TestObject))

;; This is how we call methods on java objects

(defn first-name [obj]
  (.getFirstName obj))

(defn last-name [obj]
  (.getName obj))

(defn age [obj]
  (.getAge obj))

(defn set-first-name [obj name]
  (.setFirstName obj name))

(defn set-last-name [obj name]
  (.setName obj name))

(defn set-age [obj age]
  (.setAge obj age))

(defn -main
  "Test, communication with Java"
  []
  (let [testObj (TestObject. "Joe" "Shmow" 25)]
    (println (first-name testObj))
    (println (last-name testObj))
    (println (age testObj))))


;; To execute in the REPL:
;(let [testObj (TestObject. "Joe" "Shmow" 25)]
;  (println (first-name testObj))
;  (println (last-name testObj))
;  (println (age testObj)))
;
;(println "-------------------")
;
;(let [testObj (new TestObject "Roger" "Rabbit" 26)]  ;; Equivalent to the above
;  (println (first-name testObj))
;  (println (last-name testObj))
;  (println (age testObj)))
;
;(println "-------------------")
;
;(let [testObj (TestObject.)]
;  (set-first-name testObj "Luke")
;  (set-last-name testObj "Skywalker")
;  (set-age testObj 24)
;  (println (first-name testObj))
;  (println (last-name testObj))
;  (println (age testObj)))
;
;(println "-------------------")

