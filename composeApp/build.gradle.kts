import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.0"
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("net.java.dev.jna:jna:5.17.0")
            implementation("com.github.tulskiy:jkeymaster:1.3")
            implementation("com.twelvemonkeys.imageio:imageio-icns:3.10.1")
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.nullinnix.clippr.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Clippr"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "com.nullinnix.clippr"
                infoPlist {
                    extraKeysRawXml = """
                        <key>LSUIElement</key>
                        <string>true</string>
                    """.trimIndent()

//                    extraKeysRawXml = """
//                        <key>NSPasteboardUsageDescription</key>
//                        <string>Clippr needs clipboard access to detect copied items and manage your clips.</string>
//                    """.trimIndent()
                }
                appCategory = "public.app-category.productivity"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/MyIcon.icns"))
            }
        }
    }
}

tasks.withType<JavaExec> {
    jvmArgs(
        "--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
    )
}

dependencies {
    ksp(libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}