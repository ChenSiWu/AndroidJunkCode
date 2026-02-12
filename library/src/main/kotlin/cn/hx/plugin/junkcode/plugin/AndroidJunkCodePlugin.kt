package cn.hx.plugin.junkcode.plugin

import cn.hx.plugin.junkcode.ext.AndroidJunkCodeExt
import cn.hx.plugin.junkcode.ext.JunkCodeConfig
import cn.hx.plugin.junkcode.task.GenerateJunkCodeTask
import cn.hx.plugin.junkcode.task.ManifestMergeTask
import cn.hx.plugin.junkcode.utils.capitalizeCompat
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class AndroidJunkCodePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val androidJunkCodeExt = extensions.create(
                "androidJunkCode",
                AndroidJunkCodeExt::class.java,
                objects.domainObjectContainer(JunkCodeConfig::class.java)
            )
            val androidComponents =
                extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                val variantName = variant.name
                val junkCodeConfig =
                    androidJunkCodeExt.variantConfig.findByName(variantName) ?: return@onVariants
                if (androidJunkCodeExt.debug) {
                    println("AndroidJunkCode: generate code for variant $variantName")
                }
                //生成垃圾代码目录
                val junkCodeOutDir =
                    layout.buildDirectory.dir("generated/source/junk/${variantName}")
                val generateJunkCodeTaskProvider =
                    tasks.register<GenerateJunkCodeTask>("generate${variantName.capitalizeCompat()}JunkCode") {
                        config.set(junkCodeConfig)
                        namespace.set(variant.namespace)
                        outputFolder.set(junkCodeOutDir)
                    }
                //java文件
                variant.sources.java?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    objects.directoryProperty().value(it.outputFolder.dir("java"))
                }
                //资源文件
                variant.sources.res?.addGeneratedSourceDirectory(generateJunkCodeTaskProvider) {
                    objects.directoryProperty().value(it.outputFolder.dir("res"))
                }
                //AndroidManifest.xml
                val manifestUpdater =
                    tasks.register<ManifestMergeTask>("merge" + variantName.capitalizeCompat() + "JunkCodeManifest") {
                        genManifestFile.set(generateJunkCodeTaskProvider.flatMap {
                            it.outputFolder.file(
                                "AndroidManifest.xml"
                            )
                        })
                    }
                variant.artifacts.use(manifestUpdater)
                    .wiredWithFiles(
                        { it.mergedManifest },
                        { it.updatedManifest })
                    .toTransform(SingleArtifact.MERGED_MANIFEST)
                //混淆文件
                variant.proguardFiles.add(generateJunkCodeTaskProvider.flatMap {
                    it.outputFolder.file("proguard-rules.pro")
                })
            }
        }
    }
}