import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm)
}

if (project.properties["publishToMaven"].toString().toBoolean()) {
    apply(plugin = "com.vanniktech.maven.publish")
    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
    }
} else {
    apply(plugin = "com.gradle.plugin-publish")
    group = "io.github.qq549631030"//这里group id 不一样
    version = project.properties["VERSION_NAME"].toString()
    configure<GradlePluginDevelopmentExtension> {
        website.set(project.properties["POM_URL"].toString())
        vcsUrl.set(project.properties["POM_SCM_URL"].toString())
        plugins {
            create("androidJunkCode") {
                id = "io.github.qq549631030.android-junk-code"
                implementationClass = "cn.hx.plugin.junkcode.plugin.AndroidJunkCodePlugin"
                displayName = "AndroidJunkCode plugin"
                description = project.properties["POM_DESCRIPTION"].toString()
                tags.set(listOf("android", "generate", "junk", "code"))
            }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin.api)
    implementation(gradleKotlinDsl())
    implementation(libs.javapoet)
}