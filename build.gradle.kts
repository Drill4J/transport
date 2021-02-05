import java.net.*

plugins {
    base
    id("com.github.hierynomus.license")
}

val scriptUrl: String by extra

allprojects {
    apply(from = "$scriptUrl/git-version.gradle.kts")
}

subprojects {
    repositories {
        mavenLocal()
        apply(from = "$scriptUrl/maven-repo.gradle.kts")
        jcenter()
    }
}

val licenseFormatSettings by tasks.registering(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)
