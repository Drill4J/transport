plugins {
    base
}

val scriptUrl: String by extra

allprojects {
    apply(from = "$scriptUrl/git-version.gradle.kts")

    repositories {
        mavenLocal()
        apply(from = "$scriptUrl/maven-repo.gradle.kts")
        jcenter()
    }
}
