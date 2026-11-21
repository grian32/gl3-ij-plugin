import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
    id("org.jetbrains.grammarkit")
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
    intellijPlatform {
        intellijIdea("2025.3.6.1")
        testFramework(TestFrameworkType.Platform)
    }
}

val generatedGrammarDir = layout.buildDirectory.dir("generated/sources/grammarkit/java")

sourceSets {
    main {
        java.srcDir(generatedGrammarDir)
    }
}

tasks {
    val generateGl3Parser by registering(GenerateParserTask::class) {
        sourceFile.set(file("src/main/grammar/gl3.bnf"))
        targetRootOutputDir.set(generatedGrammarDir)
        pathToParser.set("com/github/grian32/gl3ijplugin/parser/Gl3Parser.java")
        pathToPsiRoot.set("com/github/grian32/gl3ijplugin/psi")
        purgeOldFiles.set(true)
    }

    compileKotlin {
        dependsOn(generateGl3Parser)
    }

    compileJava {
        dependsOn(generateGl3Parser)
    }
}
