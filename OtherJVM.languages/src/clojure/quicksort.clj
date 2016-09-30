(ns com.asoftwareguy.example.quicksort)

(defn quicksort [[pivot & xs]]
  (when pivot
        (let [smaller #(< % pivot)]
          (lazy-cat
            (quicksort (filter smaller xs))
            [pivot]
            (quicksort (remove smaller xs))
            )
          )
        )
  )
