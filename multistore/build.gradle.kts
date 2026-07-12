plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("maven-publish")
    id("signing")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    wasmJs {
        browser()
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "multistore"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // common dependencies
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }
        androidUnitTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.ext.junit)
            implementation(libs.robolectric)
        }
    }
}

android {
    namespace = "io.github.yutarosuzuki_jp.multistore"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("MultiStore")
            description.set("A Kotlin Multiplatform Key-Value storage library wrapping platform-specific secure and standard storage systems.")
            url.set("https://github.com/YutaroSuzuki-JP/MultiStore")
            
            licenses {
                license {
                    name.set("The MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("yutarosuzuki-jp")
                    name.set("Yutaro Suzuki")
                    email.set("i.buzzbuzzinc@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/YutaroSuzuki-JP/MultiStore.git")
                developerConnection.set("scm:git:ssh://github.com/YutaroSuzuki-JP/MultiStore.git")
                url.set("https://github.com/YutaroSuzuki-JP/MultiStore")
            }
        }
    }
    
    repositories {
        maven {
            name = "layout"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY")
    val signingPassword = System.getenv("GPG_SIGNING_PASSWORD")
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
