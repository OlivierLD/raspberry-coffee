/**
 * In this example, `val` denotes a declaration of a read-only local variable,
 * that is assigned an pattern matching expression.
 * See http://kotlinlang.org/docs/reference/control-flow.html#when-expression
 */

fun main(args: Array<String>) {
    val language = if (args.size == 0) "EN" else args[0]
    val name     = if (args.size < 2) "" else (" " + args[1])
    println(when (language) {
        "EN" -> "Hello${name}!"
        "FR" -> "Salut${name}!"
        "ES" -> "\u00A1Hola${name}!"
        "IT" -> "Ciao${name}!"
        else -> "Sorry${name}, I can't greet you in $language yet"
    })
}
