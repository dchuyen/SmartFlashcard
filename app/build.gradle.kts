plugins {
    id("com.android.application")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.smartflashcard"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartflashcard"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Các thư viện giao diện cốt lõi của AndroidX và Material 3
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")

    // Khai báo chủ động RecyclerView và CardView để làm giao diện danh sách bộ thẻ
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.activity.ktx)
    implementation(libs.firebase.database)

    // Các thư viện test mặc định
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    configurations.all {
        resolutionStrategy {
            eachDependency {
                if (requested.group == "androidx.core" && requested.name == "core") {
                    useVersion("1.12.0")
                }
                if (requested.group == "androidx.core" && requested.name == "core-ktx") {
                    useVersion("1.12.0")
                }
                if (requested.group == "androidx.activity" && requested.name == "activity") {
                    useVersion("1.8.2")
                }
                if (requested.group == "androidx.activity" && requested.name == "activity-ktx") {
                    useVersion("1.8.2")
                }
            }
        }
    }
    implementation("com.github.yuyakaido:cardstackview:2.3.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    // Thư viện tự động bắt lỗi và bung màn hình thông báo lỗi trực tiếp trên điện thoại

}