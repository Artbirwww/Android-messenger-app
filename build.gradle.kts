// Точка конфигурации плагинов для всего проекта
plugins {
    // Android Gradle Plugin (совместим с Gradle 8+)
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false

    // Обновленный плагин Kotlin (где HasConvention больше не вызывается)
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // Обновленный плагин Google Services (исправляет проблему совместимости с Firebase)
    id("com.google.gms.google-services") version "4.4.2" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}