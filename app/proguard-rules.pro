# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }

# Keep our data models for JSON serialization
-keep class com.yiqiu.readingquiz.data.model.** { *; }
-keepclassmembers class com.yiqiu.readingquiz.data.model.** { *; }

# Keep AI client classes
-keep class com.yiqiu.readingquiz.ai.** { *; }

# Coil（图片加载库，使用反射注册组件；release R8 必须保留）
-keep class coil.** { *; }
-dontwarn coil.**

# Markwon（Markdown 渲染引擎，使用反射加载插件）
-keep class io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**

# compose-markdown（MarkdownText 组件，依赖 Markwon fork）
-keep class dev.jeziellago.compose.markdowntext.** { *; }
-dontwarn dev.jeziellago.compose.markdowntext.**