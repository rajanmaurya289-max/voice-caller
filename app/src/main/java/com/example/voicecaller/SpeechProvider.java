package com.example.voicecaller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.speech.RecognitionService;
import java.util.List;

public final class SpeechProvider {

    private SpeechProvider() {}

    public static ComponentName find(Context context) {
        List<ResolveInfo> providers =
            context.getPackageManager().queryIntentServices(
                new Intent(RecognitionService.SERVICE_INTERFACE),
                0);

        ComponentName system = null;
        ComponentName other = null;

        for (ResolveInfo resolved : providers) {
            ServiceInfo service = resolved.serviceInfo;

            if (service == null
                    || !service.enabled
                    || !service.exported
                    || service.packageName.equals(
                        context.getPackageName())) {
                continue;
            }

            ComponentName component = new ComponentName(
                service.packageName, service.name);

            if (service.packageName.equals(
                    "com.google.android.googlequicksearchbox")) {
                return component;
            }

            if ((service.applicationInfo.flags
                    & ApplicationInfo.FLAG_SYSTEM) != 0
                    && system == null) {
                system = component;
            }

            if (other == null) {
                other = component;
            }
        }

        return system != null ? system : other;
    }
}
