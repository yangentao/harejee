import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.kotlin.dsl.compileOnly
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:2.0.0")
    }
}
plugins {
    kotlin("jvm") version "2.1.20"
    signing
    `java-library`
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

group = "io.github.yangentao"

version = "1.0.10"
val artifactName = "harejee"
val githubLib = "harejee"
val descLib = "Java/Kotlin web."


repositories {
    mavenLocal()
    mavenCentral()
//    maven("https://app800.cn/maven/repository/public/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib"))

    compileOnlyApi("jakarta.servlet:jakarta.servlet-api:5.0.0")
    compileOnly("jakarta.websocket:jakarta.websocket-api:2.1.0")
    compileOnly("jakarta.websocket:jakarta.websocket-client-api:2.1.0")

    api("io.github.yangentao:hare:[1.0.9,)")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}
java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(project.group.toString(), artifactName, project.version.toString())
    pom {
        configPom(this, artifactName, descLib, githubLib)
    }
}
afterEvaluate {
    publishing {
        repositories {
            mavenLocal()
            maven {
                name = "App800"
                url = uri("https://app800.cn/maven/repository/public/")
                credentials {
                    username = providers.gradleProperty("ARCHIVA_USERNAME").get()
                    password = providers.gradleProperty("ARCHIVA_PASSWORD").get()
                }
            }
        }
    }
}



fun configPom(pom: MavenPom, artifactName: String, descLib: String, githubLib: String = artifactName) {
    pom.apply {
        name.set(artifactName)
        description.set(descLib)
        inceptionYear.set("2025")
        url.set("https://github.com/yangentao/$githubLib/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("yangentao")
                name.set("Yang Entao")
                url.set("https://github.com/yangentao/")
            }
        }
        scm {
            url.set("https://github.com/yangentao/$githubLib/")
            connection.set("scm:git:git://github.com/yangentao/$githubLib.git")
            developerConnection.set("scm:git:ssh://git@github.com/yangentao/$githubLib.git")
        }
    }
}