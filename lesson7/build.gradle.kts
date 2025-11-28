plugins {
    id("buildlogic.kotlin-library-conventions")
}
dependencies {
    testImplementation(kotlin("test"))
}

detekt {
    ignoreFailures = true  // Позволяет сборке продолжиться
    reports {
        html.enabled = true
        xml.enabled = false
        txt.enabled = false
    }
}

tasks.test {
    enabled = false
}