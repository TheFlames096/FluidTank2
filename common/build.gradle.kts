plugins {
    id("com.kotori316.common")
    alias(libs.plugins.architectury.plugin)
    alias(libs.plugins.architectury.loom)
}

architectury {
    common(project.property("enabled_platforms").toString().split(","))
}

loom {
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
}
