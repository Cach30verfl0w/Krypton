pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}


include(":krypton-openssl", ":krypton-core", ":krypton-keystore", ":krypton-asn1")
rootProject.name = "krypton"
