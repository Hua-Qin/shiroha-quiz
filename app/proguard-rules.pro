# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }

# Keep our data models for JSON serialization
-keep class com.yiqiu.readingquiz.data.model.** { *; }
-keepclassmembers class com.yiqiu.readingquiz.data.model.** { *; }

# Keep AI client classes
-keep class com.yiqiu.readingquiz.ai.** { *; }