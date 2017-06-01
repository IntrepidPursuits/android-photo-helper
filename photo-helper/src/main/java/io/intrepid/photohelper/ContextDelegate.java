package io.intrepid.photohelper;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

interface ContextDelegate {
    void requestPermissions(@NonNull String[] permissions, int requestCode);

    @NonNull
    Context getContext();

    void startActivityForResult(@NonNull Intent intent, int requestCode);
}
