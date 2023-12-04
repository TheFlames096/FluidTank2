plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.minecraftforge.net/") }
    maven { url = uri("https://maven.architectury.dev/") }
    maven { url = uri("https://storage.googleapis.com/kotori316-maven-storage/maven/") }
}

dependencies {
    mapOf(
        "architectury-plugin" to libs.versions.plugin.architectury.get(),
        "dev.architectury.loom" to libs.versions.plugin.loom.get(),
        "com.kotori316.plugin.cf" to libs.versions.plugin.cf.get(),
        "com.github.johnrengelman.shadow" to libs.versions.plugin.shadow.get(),
        "me.modmuss50.mod-publish-plugin" to libs.versions.plugin.publish.all.get(),
    ).forEach { (name, version) ->
        implementation(group = name, name = "${name}.gradle.plugin", version = version)
    }
}
