import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":trip-api"))
    implementation(project(":car-rental-api"))
    implementation(project(":hotel-manager-api"))

    implementation(platform("io.arrow-kt:arrow-stack:1.0.1"))
    implementation("io.arrow-kt:arrow-core")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
