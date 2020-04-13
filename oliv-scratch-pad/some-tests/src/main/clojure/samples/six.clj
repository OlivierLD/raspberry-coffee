(for [i (range 10)]
  (inc i))

`(+ 10 (* 3 2))

; ~ unquotes an expression within a quote
`(+ 10 ~(* 3 2))
