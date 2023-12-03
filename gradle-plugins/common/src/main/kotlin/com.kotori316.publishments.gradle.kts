import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("java")
    id("scala")
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("maven-publish")
    id("signing")
    id("com.kotori316.plugin.cf")
    id("com.github.johnrengelman.shadow")
    id("com.matthewprenger.cursegradle")
    id("com.modrinth.minotaur")
}

val minecraftVersion = project.property("minecraft_version") as String
val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()

val remapJar: RemapJarTask by tasks.named("remapJar", RemapJarTask::class)

signing {
    sign(publishing.publications)
    sign(remapJar)
    // sign(tasks.named("sourcesJar").get())
    sign(tasks.named("shadowJar").get())
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks {
    val jksSignJar = register("jksSignJar") {
        //dependsOn(remapJar)
        onlyIf {
            project.hasProperty("jarSign.keyAlias") &&
                    project.hasProperty("jarSign.keyLocation") &&
                    project.hasProperty("jarSign.storePass")
        }
        doLast {
            ant.withGroovyBuilder {
                "signjar"(
                    "jar" to remapJar.map { it.archiveFile }.get(),
                    "alias" to project.findProperty("jarSign.keyAlias"),
                    "keystore" to project.findProperty("jarSign.keyLocation"),
                    "storepass" to project.findProperty("jarSign.storePass"),
                    "sigalg" to "Ed25519",
                    "digestalg" to "SHA-256",
                    "tsaurl" to "http://timestamp.digicert.com",
                )
            }
        }
    }
    remapJar {
        finalizedBy(jksSignJar)
    }
    withType(Sign::class) {
        onlyIf { hasGpgSignature }
    }
    withType(AbstractPublishToMaven::class) {
        if (hasGpgSignature) {
            dependsOn("signRemapJar")
        }
    }

    val baseName = project.findProperty("maven_base_name") as String

    register("registerVersion", CallVersionFunctionTask::class) {
        functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
        gameVersion = minecraftVersion
        platform = project.name
        modName = baseName
        changelog = cfChangelog()
        homepage = "https://modrinth.com/mod/large-fluid-tank"
        isDryRun = releaseDebug
    }
    register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
        gameVersion = minecraftVersion
        platform = project.name
        modName = baseName
        version = project.version.toString()
        failIfExists = !releaseDebug
    }
}

fun cfChangelog(): String {
    return rootProject
        .file(project.property("changelog_file") as String)
        .useLines {
            it.joinToString(System.lineSeparator())
                .split("---", limit = 2)[0]
                .lines()
                .filterNot { t -> t.startsWith("## ") }
                .joinToString(System.lineSeparator())
        }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get() + "-" + minecraftVersion
            from(components.getAt("java"))
        }
    }

    repositories {
        if (!System.getenv("CI").toBoolean() || !releaseDebug) {
            val user = project.findProperty("azureUserName") ?: System.getenv("AZURE_USER_NAME") ?: ""
            val pass = project.findProperty("azureToken") ?: System.getenv("AZURE_TOKEN") ?: "TOKEN"
            maven {
                name = "AzureRepository"
                url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
                credentials {
                    username = user.toString()
                    password = pass.toString()
                }
            }
        }
    }
}

fun mapPlatformToCamel(platform: String): String {
    return when (platform) {
        "forge" -> "Forge"
        "fabric" -> "Fabric"
        "neoforge" -> "NeoForge"
        else -> throw IllegalArgumentException("Unknown platform $platform")
    }
}

fun curseChangelog(): String {
    if (!ext.has("changelogHeader")) {
        return "NNC ${project.name}"
        // throw IllegalStateException("No changelogHeader for project(${project.name})")
    }
    val header = ext.get("changelogHeader").toString()
    val fromFile = rootProject
        .file(project.property("changelog_file") as String)
        .readText()
    return header + System.lineSeparator() + fromFile
}

fun curseProjectId(platform: String): String {
    return when (platform) {
        "forge" -> "291006"
        "fabric" -> "411564"
        "neoforge" -> "291006"
        else -> throw IllegalArgumentException("Unknown platform $platform")
    }
}

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = curseProjectId(project.name)
        changelogType = "markdown"
        changelog = curseChangelog()
        releaseType = "release"
        addGameVersion(minecraftVersion)
        addGameVersion(mapPlatformToCamel(project.name))
        mainArtifact(remapJar.archiveFile.get(), closureOf<CurseArtifact> {
            displayName = "${project.version}-${project.name}"
        })
        addArtifact(tasks.shadowJar.flatMap { it.archiveFile }.get())
        relations(closureOf<CurseRelation> {
            requiredDependency("scalable-cats-force")
        })
    })
    options(closureOf<Options> {
        curseGradleOptions.debug = releaseDebug
        curseGradleOptions.javaVersionAutoDetect = false
        curseGradleOptions.forgeGradleIntegration = false
    })
}

fun modrinthChangelog(): String {
    if (!ext.has("changelogHeader")) {
        throw IllegalStateException("No changelogHeader for project(${project.name})")
    }
    val header = ext.get("changelogHeader").toString()
    val fromFile = rootProject
        .file(project.property("changelog_file") as String)
        .readText()
    val shortFormat = fromFile.split("---", limit = 2)[0]
    return header + System.lineSeparator() + shortFormat
}

modrinth {
    token.set((project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String)
    projectId = "large-fluid-tank"
    versionType = "release"
    versionName = "${project.version}-${project.name}"
    uploadFile = remapJar
    additionalFiles = listOf(tasks.shadowJar)
    gameVersions = listOf(minecraftVersion)
    loaders = listOf(project.name)
    changelog = provider { modrinthChangelog() }
    debugMode = releaseDebug
    dependencies {
        required.project("scalable-cats-force")
    }
}

afterEvaluate {
    rootProject.tasks.named("githubRelease") {
        dependsOn(tasks.assemble)
    }
}

tasks.register("checkChangelog") {
    doLast {
        listOf(
            "cfChangelog" to cfChangelog(),
            "curseChangelog" to curseChangelog(),
            "modrinthChangelog" to modrinthChangelog(),
        ).forEach { pair ->
            println("*".repeat(10) + pair.first + "*".repeat(10))
            println(pair.second)
            println("*".repeat(30))
        }
    }
}
