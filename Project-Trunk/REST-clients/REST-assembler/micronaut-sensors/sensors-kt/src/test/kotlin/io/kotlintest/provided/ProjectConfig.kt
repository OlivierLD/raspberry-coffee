package io.kotlintest.provided

import io.kotlintest.AbstractProjectConfig
import io.micronaut.test.extensions.kotlintest.MicornautKotlinTestExtension

object ProjectConfig : AbstractProjectConfig() {
    override fun listeners() = listOf(MicornautKotlinTestExtension)
    override fun extensions() = listOf(MicornautKotlinTestExtension)
}
