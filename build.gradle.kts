import java.util.Properties

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
    signing
}

group = "com.singularity-universe.tokeng"
version = "1.0.0-rc1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            artifactId = "TokenG"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name = "TokenG"
                description = "A Kotlin library for generating structured, signable tokens."
                url = "https://github.com/SingularityIndonesia/TokenG"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/SingularityIndonesia/TokenG/blob/main/LICENSE.md"
                    }
                }
                developers {
                    developer {
                        id = "SingularityIndonesia"
                        name = "Singularity Indonesia"
                        email = "singularity.indonesia@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/SingularityIndonesia/TokenG.git"
                    developerConnection = "scm:git:ssh://github.com/SingularityIndonesia/TokenG.git"
                    url = "https://github.com/SingularityIndonesia/TokenG"
                }
            }
        }
    }
    repositories {
        maven {
            name = "CentralPortal"
            url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            credentials {
                username = (localProperties["TOKENG_SONATYPE_TokenG_Project_USERNAME"] as String?)
                    ?: (project.findProperty("TOKENG_SONATYPE_TokenG_Project_USERNAME") as String?)
                    ?: System.getenv("TOKENG_SONATYPE_TokenG_Project_USERNAME")
                password = (localProperties["TOKENG_SONATYPE_TokenG_Project_PASSWORD"] as String?)
                    ?: (project.findProperty("TOKENG_SONATYPE_TokenG_Project_PASSWORD") as String?)
                    ?: System.getenv("TOKENG_SONATYPE_TokenG_Project_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenKotlin"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
