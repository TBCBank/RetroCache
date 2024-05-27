import com.android.build.gradle.internal.scope.publishBuildArtifacts
import io.grpc.internal.SharedResourceHolder.release

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish")
}

group = "ge.tbcbank.retrocache"
version "1.0.2"

dependencies {
    val okhttp = "4.11.0"
    val retrofit = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit")
    implementation("com.squareup.okhttp3:okhttp:$okhttp")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.retrofit2:converter-gson:$retrofit")
    testImplementation("com.squareup.okhttp3:mockwebserver:$okhttp")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["java"])
                groupId = "com.github.TBCBank"
                artifactId = "RetroCache"
                version = "1.0.2"
            }
        }
    }
}