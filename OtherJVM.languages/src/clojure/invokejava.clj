(ns example
  (:import test.clojure.TestObject))

;; This is how we call methods on java objects

(defn first-name [obj]
  (.getFirstname obj))

(defn last-name [obj]
  (.getName obj))

(defn age [obj]
  (.getAge obj))

(defn set-first-name [obj name]
  (.setFirstname obj name))

(defn set-last-name [obj name]
  (.setName obj name))

(defn set-age [obj age]
  (.setAge obj age))

;; In the REPL:
(let [testObj (TestObject. "Joe" "Shmow" 25)]
  (println (first-name testObj))
  (println (first-name testObj))
  (println (last-name testObj))
  (println (age testObj)))

(let [testObj (new TestObject "Roger" "Rabbit" 26)]  ;; Equivalent to the above
  (println (first-name testObj))
  (println (first-name testObj))
  (println (last-name testObj))
  (println (age testObj)))

(let [testObj (TestObject.)]
  (println (first-name testObj))
  (set-first-name testObj "Luke")
  (println (first-name testObj))
  (set-last-name testObj "Skywalker")
  (set-age testObj 24)
  (println (last-name testObj))
  (println (age testObj)))
