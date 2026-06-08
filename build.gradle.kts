plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
    signing
}

group = "com.singularity_universe.tokeng"
version = "1.0.0"

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
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["java"])

            pom {
                name.set("TokenG")
                description.set("A Kotlin library for generating structured, signable tokens.")
                url.set("https://github.com/SingularityIndonesia/TokenG")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/SingularityIndonesia/TokenG/blob/main/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("SingularityIndonesia")
                        name.set("Singularity Indonesia")
                        email.set("singularity.indonesia@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/SingularityIndonesia/TokenG.git")
                    developerConnection.set("scm:git:ssh://github.com/SingularityIndonesia/TokenG.git")
                    url.set("https://github.com/SingularityIndonesia/TokenG")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri(
                if (version.toString().endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = project.findProperty("TOKENG_SONATYPE_TokenG_Project_USERNAME") as String? ?: System.getenv("TOKENG_SONATYPE_TokenG_Project_USERNAME")
                password = project.findProperty("TOKENG_SONATYPE_TokenG_Project_PASSWORD") as String? ?: System.getenv("TOKENG_SONATYPE_TokenG_Project_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenKotlin"])
}
