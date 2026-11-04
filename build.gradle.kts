group = "beer.devs.rpgmoney"
version = "1.4.0"

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.6"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven("https://repo.papermc.io/repository/maven-public/")

    maven(url = "https://jitpack.io")
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    // Only public APIs are used; a server development bundle is unnecessary.
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("beer.devs:itemsadder-api:4.0.17") {
        isTransitive = false
    }
    implementation("commons-io:commons-io:2.18.0")

    implementation("de.tr7zw:item-nbt-api:2.16.1")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.mockito:mockito-core:5.15.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs = (listOf("-nowarn", "-Xlint:none"))
    options.isIncremental = true
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    archiveClassifier.set("") // Rimuove "-all"
    archiveBaseName.set("RPGMoney")
    archiveVersion.set("");

    relocate("de.tr7zw.changeme.nbtapi", "beer.devs.rpgmoney.shaded.nbtapi")
    relocate("org.apache.commons.io", "beer.devs.rpgmoney.shaded.commonsio")

    exclude("META-INF/**")

}

tasks.register<Copy>("copyToServer") {
    val serverPluginsDir = providers.gradleProperty("serverPluginsDir")
    onlyIf { serverPluginsDir.isPresent }
    from(tasks.shadowJar)
    into(serverPluginsDir.orElse(layout.buildDirectory.dir("server-plugins").map { it.asFile.absolutePath }))
    rename { "RPGMoney.jar" }
}
