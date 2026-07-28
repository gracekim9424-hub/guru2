// 안드로이드 앱 개발에 필요한 플러그인 설정
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

// 앱의 기본 빌드 설정
android {
    namespace = "com.damyeoom.app"
    compileSdk = 34

    // 앱 ID와 지원 안드로이드 버전 설정
    defaultConfig {
        applicationId = "com.damyeoom.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // 배포용 앱 빌드 설정
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Java 호환 버전 설정
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Kotlin JVM 버전 설정
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Jetpack Compose 사용 설정
    buildFeatures {
        compose = true
    }

    // 중복 리소스 제외 설정
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// 앱에서 사용하는 외부 라이브러리
dependencies {

    // 안드로이드 기본 기능 및 생명주기
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Jetpack Compose 화면 구성
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // 화면 이동 기능
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Compose 개발 및 미리보기 도구
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 이미지 표시 라이브러리
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Room 로컬 데이터베이스
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // JSON 데이터 변환
    implementation("com.google.code.gson:gson:2.11.0")

    // 기존 Activity 및 Fragment 지원
    implementation("androidx.appcompat:appcompat:1.7.0")

    // 네이버 지도 SDK
    implementation("com.naver.maps:map-sdk:3.23.0")

    // 현재 위치 조회
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // 지도·날씨 API 통신
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}