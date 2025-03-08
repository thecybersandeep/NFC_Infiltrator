package com.cybersandeep.nfcinfiltrator;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;

/**
 * Utility class for compatibility with different Android versions
 */
public class CompatUtils {

    /**
     * Get color compatible with all Android versions
     */
    public static int getCompatColor(Context context, int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorResId, null);
        } else {
            return context.getResources().getColor(colorResId);
        }
    }
    
    /**
     * Get PendingIntent flags compatible with all Android versions
     */
    public static int getPendingIntentFlags(boolean mutable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return mutable ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_IMMUTABLE;
        } else {
            return 0;
        }
    }
    
    /**
     * Apply window insets in a compatible way
     */
    public static void applyWindowInsets(Context context) {
        // No-op for compatibility
    }
}