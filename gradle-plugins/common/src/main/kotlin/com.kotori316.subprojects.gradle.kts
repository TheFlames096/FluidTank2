import net.fabricmc.loom.task.RemapJarTask
import org.gradle.jvm.tasks.Jar

plugins {
    java
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
}

configurations {
    val common = create("common")
    create("shadowCommon") // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
    compileClasspath { extendsFrom(common) }
    if (project.name != "forge" || System.getenv("RUN_JUNIT") != null) {
        runtimeClasspath { extendsFrom(common) }
    }
    testCompileClasspath { extendsFrom(compileClasspath.get()) }
    testRuntimeClasspath { extendsFrom(runtimeClasspath.get()) }
}

tasks {
    jar {
        archiveClassifier = "dev-only-platform"
    }
    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations.getAt("shadowCommon"))
        archiveClassifier = "dev"
    }
    named("remapJar", RemapJarTask::class) {
        val shadowJarProvider = provider { project }.flatMap { p -> p.tasks.shadowJar }
        inputFile = shadowJarProvider.flatMap { j -> j.archiveFile }
        dependsOn(shadowJarProvider)
        archiveClassifier = null
    }
    named("sourcesJar", Jar::class) {
        val commonSources = provider { project(":common") }.flatMap { p -> p.tasks.named("sourcesJar", Jar::class) }
        dependsOn(commonSources)
        from(commonSources.flatMap { it.archiveFile }.map { zipTree(it) })
    }
}

dependencies {

}

components.named("java", AdhocComponentWithVariants::class) {
    withVariantsFromConfiguration(configurations.shadowRuntimeElements.get()) {
        skip()
    }
}

afterEvaluate {
    tasks.findByName("gameTestClasses")?.let { c ->
        tasks.findByName("runClient")?.dependsOn(c)
        tasks.findByName("runGameTest")?.dependsOn(c)
    }
    tasks.findByName("runGameClasses")?.let { c ->
        tasks.findByName("runClient")?.dependsOn(c)
        tasks.findByName("runServer")?.dependsOn(c)
    }
    tasks.findByName("genDataClasses")?.let { c ->
        tasks.findByName("runData")?.dependsOn(c)
    }
    tasks.withType(ProcessResources::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        val version = project.version.toString()
        inputs.property("version", version)
        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
        filesMatching("META-INF/mods.toml") {
            expand("version" to version)
        }
    }
}

