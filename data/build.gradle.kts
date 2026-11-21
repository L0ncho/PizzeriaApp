import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

sourceSets {
    test {
        resources.srcDir(rootProject.file("app/src/main/assets"))
    }
}

dependencies {
    implementation(project(":util"))
    implementation(libs.kotlin.reflect)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
}
