# ── 业务代码 ───────────────────────────────────────────────────────────────────
-keep class com.pengjunlong.app.** { *; }

# ── 保留行号，方便崩溃堆栈定位 ────────────────────────────────────────────────
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# ── Gson：保留被 @SerializedName 标注的数据类字段 ─────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ── Retrofit：保留接口方法（避免方法被混淆导致运行时找不到） ────────────────────
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepattributes Exceptions

# ── OkHttp ────────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── ACRA 崩溃上报：保留完整的崩溃处理链 ──────────────────────────────────────
-keep class org.acra.** { *; }
-keep interface org.acra.** { *; }
-keep enum org.acra.** { *; }
-keepnames class * implements org.acra.sender.ReportSender

