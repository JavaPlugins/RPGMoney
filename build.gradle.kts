group = "beer.devs.rpgmoney"
version = "1.3.0"

plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.6"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven("https://repo.papermc.io/repository/maven-public/")

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "lonedev"
        url = uri("https://maven.devs.beer/")
    }
    maven(url = "https://jitpack.io")
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnlyApi("dev.lone:api-itemsadder:4.0.2-beta-release-11")
    compileOnlyApi("net.kyori:adventure-text-serializer-legacy:4.18.0")
    compileOnlyApi("net.kyori:adventure-platform-bukkit:4.3.4")
    compileOnlyApi("beer.devs:FastNbt-jar:1.4.2")
    compileOnlyApi("commons-io:commons-io:2.18.0")

    implementation("de.tr7zw:item-nbt-api:2.14.1")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
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

    exclude("META-INF/**")

    finalizedBy("copyToServer")
}

tasks.register<Copy>("copyToServer") {
    from(tasks.shadowJar)
    into("C:\\Progetti\\Minecraft\\TestServer\\1.21.4\\plugins")
    rename { "RPGMoney.jar" }
}