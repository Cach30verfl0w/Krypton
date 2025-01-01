import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

/*
 * Copyright 2024 Cedric Hammes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotest)
}

var projectJvmTarget = libs.versions.jvmTarget.get()
configureAllTargets(projectJvmTarget)

kotlin {
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    iosArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.core)
            api(libs.kotlinx.io.core)
            api(libs.kotlinx.datetime)
            api(libs.bignum)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.bundles.kotest)
        }
        jvmTest.dependencies {
            implementation(libs.kotest.junit.runner)
        }
    }
}

android {
    namespace = "${findProperty("project.group")}.asn1"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.compileSdk.get().toInt()
    }

    compileOptions {
        val androidJvmTarget = JavaVersion.valueOf("VERSION_$projectJvmTarget")
        sourceCompatibility = androidJvmTarget
        targetCompatibility = androidJvmTarget
    }
}
