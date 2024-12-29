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

import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.nio.file.Path
import kotlin.io.path.absolutePathString

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.download)
}

val buildFolder: Path = layout.buildDirectory.asFile.get().toPath()
val opensslBinariesVersion = libs.versions.openssl.binaries.get()
val opensslBinariesFolder: Path = buildFolder.resolve("openssl").resolve(opensslBinariesVersion)
val downloadOpenSSLBinariesTask = tasks.create("downloadOpenSSLBinaries", Download::class.java) {
    src("https://gitlab.com/api/v4/projects/57407788/packages/generic/build/v$opensslBinariesVersion/build.zip")
    dest(opensslBinariesFolder.resolve("binaries.zip").toFile())
    overwrite(false)
    retries(10)
}

val extractOpenSSLBinariesTask = tasks.create("extractOpenSSLBinaries", Copy::class.java) {
    dependsOn(downloadOpenSSLBinariesTask)
    from(zipTree(opensslBinariesFolder.resolve("binaries.zip"))) {
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(2).toTypedArray())
        }
    }
    into(opensslBinariesFolder)
}

kotlin {
    listOf(
        linuxArm64(),
        linuxX64(),
        mingwX64(),
        macosArm64(),
        macosX64(),
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { target ->
        val targetFolder = opensslBinariesFolder.resolve(target.konanTarget.name)
        target.compilations.getByName("main") {
            cinterops {
                val openssl by creating {
                    packageName("${findProperty("project.group")}.openssl")
                    includeDirs(targetFolder.resolve("include"))
                    tasks.named(interopProcessingTaskName) {
                        dependsOn(extractOpenSSLBinariesTask)
                    }
                }
            }
        }
        target.compilerOptions {
            freeCompilerArgs.addAll("-include-binary", targetFolder.resolve("lib/libcrypto.a").absolutePathString())
            if (target.konanTarget == KonanTarget.MINGW_X64) {
                freeCompilerArgs.addAll(
                    "-include-binary", if (OperatingSystem.current().isWindows) {
                        Path.of(System.getProperty("user.home"))
                            .resolve(".konan/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32/lib")
                    } else {
                        Path.of("/usr/x86_64-w64-mingw32/lib")
                    }.resolve("libcrypt32.a").absolutePathString()
                )
            }
        }
    }
}
