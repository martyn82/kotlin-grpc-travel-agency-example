import com.google.protobuf.gradle.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val protoVersion: String by project
val protocVersion: String by project
val grpcVersion: String by project
val grpcKotlinStubVersion: String by project
val arrowVersion: String by project

plugins {
    idea
    java
    kotlin("jvm")
    id("com.google.protobuf")
}

dependencies {
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinStubVersion")
    api("com.google.protobuf:protobuf-kotlin:$protoVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")

    implementation(platform("io.arrow-kt:arrow-stack:$arrowVersion"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.grpc:grpc-all:$grpcVersion")

    testImplementation(kotlin("test"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protoVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$protocVersion"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

tasks.test {
    useJUnit()
}

tasks.create("cleanGenerated") {
    delete("${projectDir}/build/generated")
}

tasks.withType<KotlinCompile>() {
    dependsOn(":${project.name}:cleanGenerated")
    kotlinOptions.jvmTarget = "1.8"
}
