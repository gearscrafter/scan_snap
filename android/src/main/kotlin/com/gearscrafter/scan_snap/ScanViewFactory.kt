package com.gearscrafter.scan_snap

import android.content.Context
import android.util.Log
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

/**
 * Factory class responsible for creating instances of the native platform view (ScanPlatformView).
 * This class is used to bridge the native Android view with the Flutter side.
 */
class ScanViewFactory(
    private val messenger: BinaryMessenger
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    // Holds a reference to the current activity binding, needed to access lifecycle and permission APIs.
    private var activityBinding: ActivityPluginBinding? = null

    /**
     * Updates the current activity binding.
     * This must be called from the plugin when the activity is attached or detached.
     */
    fun setActivityBinding(binding: ActivityPluginBinding?) {
        activityBinding = binding
    }

    /**
     * Creates the actual platform view that will be rendered in the Flutter UI.
     *
     * @param context The context to use for creating views.
     * @param viewId A unique identifier for the view.
     * @param args Optional creation arguments passed from the Flutter side.
     * @return The platform view to be embedded into Flutter's widget tree.
     */
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        Log.d("scan_snap", "📦 Reached create() method")

        // Safely cast creation arguments to a Map<String, Any>, if possible
        val params = if (args is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            args as? Map<String, Any>
        } else {
            Log.w("scan_snap", "⚠️ 'args' is not a Map<String, Any>: $args")
            null
        }

        val binding = activityBinding
            ?: throw IllegalStateException("❌ ActivityPluginBinding is not available in create()")

        // Return a new instance of the native scan view
        return ScanPlatformView(
            messenger,
            context,
            binding.activity,
            binding,
            viewId,
            params
        )
    }
}
