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
}

var projectJvmTarget = libs.versions.jvmTarget.get()
configureAllTargets(projectJvmTarget)
configureJvmAndAndroid()
configureOpenSSL()

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":krypton-core"))
            api(project(":krypton-asn1"))
        }
    }
}

android {
    namespace = "${findProperty("project.group")}.keystore"
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
