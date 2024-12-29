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

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val Project.kotlin: KotlinMultiplatformExtension get() = the()

fun Project.configureAllTargets(projectJvmTarget: String) = with(kotlin) {
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    jvmToolchain(projectJvmTarget.toInt())
    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.valueOf("JVM_$projectJvmTarget"))
        }
    }
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.valueOf("JVM_$projectJvmTarget"))
        }
    }
}

fun Project.configureOpenSSL() = with(kotlin) {
    sourceSets.all {
        compilerOptions {
            optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    val opensslMain = sourceSets.create("opensslMain").also { it.dependsOn(sourceSets.commonMain.get()) }
    sourceSets.getByName("linuxX64Main").dependsOn(opensslMain)
    sourceSets.getByName("linuxArm64Main").dependsOn(opensslMain)
    sourceSets.getByName("mingwX64Main").dependsOn(opensslMain)
    sourceSets.getByName("macosX64Main").dependsOn(opensslMain)
    sourceSets.getByName("macosArm64Main").dependsOn(opensslMain)
    sourceSets.getByName("iosX64Main").dependsOn(opensslMain)
    sourceSets.getByName("iosArm64Main").dependsOn(opensslMain)
    sourceSets.getByName("iosSimulatorArm64Main").dependsOn(opensslMain)

    opensslMain.dependencies {
        api(project(":krypton-openssl"))
    }
}

fun Project.configureJvmAndAndroid() = with(kotlin) {
    val jvmAndAndroidMain = sourceSets.create("jvmAndAndroidMain").also { it.dependsOn(sourceSets.commonMain.get()) }
    sourceSets.getByName("androidMain").dependsOn(jvmAndAndroidMain)
    sourceSets.getByName("jvmMain").dependsOn(jvmAndAndroidMain)
}

fun Project.configureTests(
    kotestProvider: Provider<ExternalModuleDependencyBundle>,
    junitProvider: Provider<MinimalExternalModuleDependency>
) = with(kotlin) {
    sourceSets.getByName("commonTest").dependencies {
        implementation(kotestProvider)
    }
    sourceSets.findByName("jvmTest")?.dependencies {
        implementation(junitProvider)
    }
}