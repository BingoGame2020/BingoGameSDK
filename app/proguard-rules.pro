# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#bingo sdk
-keep class com.bingo.sdk.BingoSdkCore{
    public <methods>;
}

-keep class com.bingo.sdk.web.FormatResponse{
    *;
}
-keep class com.bingo.sdk.bean.**{*;}
-keep class com.bingo.sdk.inner.bean.**{*;}
-keep class com.bingo.sdk.callback.**{*;}

-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {
    public <methods>;
}


#巨量引擎
-renamesourcefileattribute SourceFile
-keepparameternames
-keep class com.bytedance.applog.AppLog { public *; }
-keep public interface com.bytedance.applog.IDataObserver { *; }
-keep public interface com.bytedance.applog.IAppParam { *; }
-keep public interface com.bytedance.applog.IExtraParams { *; }
-keep public interface com.bytedance.applog.IPicker { *; }
-keep public interface com.bytedance.applog.IOaidObserver { *; }
-keep class com.bytedance.applog.IOaidObserver$Oaid { *; }
-keep class com.bytedance.applog.GameReportHelper { public *; }
-keep class com.bytedance.applog.WhalerGameHelper { *; }
-keep class com.bytedance.applog.WhalerGameHelper$Result { *; }
-keep class com.bytedance.applog.InitConfig { public *; }
-keep class com.bytedance.applog.util.UriConfig { public *; }
-keep class com.bytedance.applog.tracker.Tracker { public *; }
-keep class com.bytedance.applog.picker.Picker { public *; }
-keep class com.bytedance.applog.tracker.WebViewJsUtil { *; }
-keep interface com.bytedance.base_bdtracker.bt { public *; }
-keep class com.bytedance.base_bdtracker.bt$a { public *; }
-keep class com.bytedance.base_bdtracker.bt$a$a { public *; }

-dontwarn com.tencent.smtt.sdk.WebView
-dontwarn com.tencent.smtt.sdk.WebChromeClient

-dontwarn androidx.annotation.Nullable
-dontwarn androidx.annotation.NonNull
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient
-dontwarn com.google.android.gms.ads.identifier.AdvertisingIdClient$Info
-dontwarn androidx.appcompat.app.AlertDialog
-dontwarn androidx.appcompat.view.menu.ListMenuItemView
-dontwarn androidx.recyclerview.widget.RecyclerView
-dontwarn androidx.swiperefreshlayout.widget.SwipeRefreshLayout
-dontwarn androidx.viewpager.widget.ViewPager
-dontwarn androidx.recyclerview.widget.RecyclerView
-dontwarn androidx.annotation.RequiresApi
-dontwarn androidx.fragment.app.FragmentActivity
-dontwarn androidx.fragment.app.Fragment
-dontwarn androidx.annotation.AnyThread
-dontwarn androidx.annotation.WorkerThread

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

##---------------End: proguard configuration for Gson  ----------

#广点通
-dontwarn com.qq.gdt.action.**
-keep class com.qq.gdt.action.** {*;}

-keepclasseswithmembers class * {
native <methods>;
}
