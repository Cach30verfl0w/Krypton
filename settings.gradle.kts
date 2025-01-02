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


include(":krypton-openssl", ":krypton-core", ":krypton-asn1", ":krypton-x509")
rootProject.name = "krypton"
