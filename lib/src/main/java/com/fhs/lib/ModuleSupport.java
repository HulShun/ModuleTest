package com.fhs.lib;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class ModuleSupport {

    public static boolean startModuleTest(Context context, String className) {
        try {
            context.startActivity(getIntent(context, className));
        } catch (ActivityNotFoundException e) {
            e.fillInStackTrace();
            return false;
        } catch (RuntimeException m) {
            m.printStackTrace();
            return false;
        }
        return true;
    }

    public static Intent getIntent(Context context, String className) {
        try {
            Class<?> clz = Class.forName(className);
            return new Intent(context, clz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
