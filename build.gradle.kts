plugins {
    `maven-publish`
}
subprojects {
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
        maven(url = "https://dl.bintray.com/kotlin/ktor/")
    }

    apply {
        plugin(org.gradle.api.publish.maven.plugins.MavenPublishPlugin::class)
    }
    publishing {
        repositories {
            maven {
                url = uri("https://oss.jfrog.org/oss-release-local")
                credentials {
                    username =
                        if (project.hasProperty("bintrayUser"))
                            project.property("bintrayUser").toString()
                        else System.getenv("BINTRAY_USER")
                    password =
                        if (project.hasProperty("bintrayApiKey"))
                            project.property("bintrayApiKey").toString()
                        else System.getenv("BINTRAY_API_KEY")
                }
            }
        }
    }
}