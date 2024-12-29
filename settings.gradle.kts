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
    }
}


include(":krypton-openssl", ":krypton-core", ":krypton-keystore", ":krypton-asn1")
rootProject.name = "krypton"
