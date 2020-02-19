package sensors.kt

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("sensors.kt")
                .mainClass(Application.javaClass)
                .start()
    }
}