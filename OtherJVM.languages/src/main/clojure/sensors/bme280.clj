;;
;; To run:
;; java -cp $CLOJURE_HOME/clojure-1.8.0.jar:../I2C.SPI/build/libs/I2C.SPI-1.0.jar clojure.main src/clojure/bme280.clj

(ns sensors.bme280
  (:import i2c.sensor.BME280))

(defn read-temperature [obj]
  (.readTemperature obj))

(defn -main
  "Read a BME280"
  []
  (let [bme280 (BME280.)]
    (println "Temperature:" (read-temperature bme280) "\272C")))

