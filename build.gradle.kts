plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "cn.yesand.intellijplugin"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
}

dependencies {
    implementation("org.json:json:20210307")
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
//  version.set("2022.2.5")
//  type.set("IC") // Target IDE Platform
    localPath.set("/Users/wangyj/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/231.9392.1/IntelliJ IDEA.app/Contents")
    plugins.set(listOf("Git4Idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
