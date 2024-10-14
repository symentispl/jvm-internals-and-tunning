import com.github.jrubygradle.api.core.RepositoryHandlerExtension

plugins {
    id("org.asciidoctor.jvm.revealjs") version "3.3.2"
    id("org.asciidoctor.jvm.gems") version "3.3.2"
    id("org.kordamp.gradle.livereload") version "0.4.0"
}

repositories {
    gradlePluginPortal()
    // https://github.com/jruby-gradle/jruby-gradle-plugin/issues/407
    this as ExtensionAware
    the<RepositoryHandlerExtension>().gems()
}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "com.burgstaller" && requested.name == "okhttp-digest" && requested.version == "1.10") {
            useTarget("io.github.rburgst:${requested.name}:1.21")
            because("Dependency has moved")
        }
    }
}


dependencies {
    dependencies {
        asciidoctorGems("rubygems:asciidoctor-revealjs:4.1.0")
    }
}

tasks.asciidoctorRevealJs {
    baseDirFollowsSourceDir()
    sourceDir("src/main/slides")
    sources {
        include("*.adoc")
    }
    setOutputDir("build/slides")
    resources {
        from("src/main/resources") {
            include("**")
        }
    }
    asciidoctorj {
        modules {
            diagram.setVersion("2.2.1")
        }
    }
}

tasks.liveReload {
    setDocRoot(tasks.asciidoctorRevealJs.get().outputDir.absolutePath)
}

tasks.register<Exec>("concurrencyPrimitives") {
    workingDir("modules/concurrency-primitives")
    commandLine = listOf("./mvnw", "-B", "verify")
}

tasks.register<Exec>("voyeursInJvmLand") {
    workingDir("modules/voyeurs-in-jvm-land")
    commandLine = listOf("./mvnw", "-B", "verify")
}

tasks.register("buildModules") {
    dependsOn("concurrencyPrimitives", "voyeursInJvmLand")
}