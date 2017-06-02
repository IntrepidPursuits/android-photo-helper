package io.intrepid.photohelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Concrete implementation of the {@link io.intrepid.photohelper.PhotoContract.Helper PhotoContract.Helper} interface
 */
@SuppressWarnings("WeakerAccess")
public class PhotoHelper implements PhotoContract.Helper {
    @VisibleForTesting
    static final String CAMERA_IMAGE_FILE_PATH = "camera_image_file_path";
    @VisibleForTesting
    static final int REQUEST_CODE_PICK_PHOTO = 31;
    @VisibleForTesting
    static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 45;

    @NonNull
    private final ContextDelegate contextDelegate;
    private PhotoContract.View view;
    @VisibleForTesting
    private boolean removeUnusedPhotos;
    String cameraImageFilePath;

    /**
     * Creates a PhotoHelper instance
     *
     * @param fragment           - The {@link Fragment} that will be calling the {@link PhotoHelper}
     * @param view               - The {@link io.intrepid.photohelper.PhotoContract.View View} interface (may very likely be the {@link Fragment})
     * @param removeUnusedPhotos - If true, delete unused new photos that the user takes with the camera
     */
    public PhotoHelper(@NonNull final Fragment fragment, @NonNull PhotoContract.View view, boolean removeUnusedPhotos) {
        this(new ContextDelegate() {
            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {
                fragment.requestPermissions(permissions, requestCode);
            }

            @Override
            @NonNull
            public Context getContext() {
                return fragment.getActivity();
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {
                fragment.startActivityForResult(intent, requestCode);
            }
        }, view, removeUnusedPhotos);
    }

    /**
     * Creates a PhotoHelper instance
     *
     * @param activity           - The {@link Activity} that will be calling the {@link PhotoHelper}
     * @param view               - The {@link io.intrepid.photohelper.PhotoContract.View View} interface (may very likely be the {@link Fragment})
     * @param removeUnusedPhotos - If true, delete unused new photos that the user takes with the camera
     */
    public PhotoHelper(@NonNull final Activity activity, @NonNull PhotoContract.View view, boolean removeUnusedPhotos) {
        this(new ContextDelegate() {
            @Override
            public void requestPermissions(@NonNull String[] permissions, int requestCode) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
            }

            @Override
            @NonNull
            public Context getContext() {
                return activity;
            }

            @Override
            public void startActivityForResult(@NonNull Intent intent, int requestCode) {
                activity.startActivityForResult(intent, requestCode);
            }
        }, view, removeUnusedPhotos);
    }

    private PhotoHelper(@NonNull ContextDelegate contextDelegate,
                        @NonNull PhotoContract.View view,
                        boolean removeUnusedPhotos) {
        this.contextDelegate = contextDelegate;
        this.view = view;
        this.removeUnusedPhotos = removeUnusedPhotos;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_PHOTO) {
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    // User picked the picture from gallery
                    view.getPhotoPresenter().onPictureChosen(imageUri.toString());
                    deleteUnusedPhotos();
                    return;
                } else {
                    if (cameraImageFilePath != null) {
                        // User took a new photo
                        view.getPhotoPresenter().onPictureChosen(cameraImageFilePath);

                        // Reset this for next time
                        cameraImageFilePath = null;
                        return;
                    }
                }
            }
        }

        view.getPhotoPresenter().onNoPictureChosen();
        deleteUnusedPhotos();
    }

    /**
     * Helper function to delete unused photos
     */
    @VisibleForTesting
    void deleteUnusedPhotos() {
        if (removeUnusedPhotos && cameraImageFilePath != null) {
            File file = new File(cameraImageFilePath);
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        cameraImageFilePath = null;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            cameraImageFilePath = savedInstanceState.getString(CAMERA_IMAGE_FILE_PATH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImagePickerIntent();
            } else {
                PhotoContract.Presenter photoPresenter = view.getPhotoPresenter();
                photoPresenter.logError(view.getPermissionDeniedMessage(), null);
                photoPresenter.onPermissionDenied();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (cameraImageFilePath != null) {
            outState.putString(CAMERA_IMAGE_FILE_PATH, cameraImageFilePath);
        }
    }

    @Override
    public void showImagePicker() {
        if (ContextCompat.checkSelfPermission(contextDelegate.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            contextDelegate.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                               REQUEST_CODE_WRITE_STORAGE_PERMISSION);
        } else {
            showImagePickerIntent();
        }
    }

    /**
     * Helper function which launches the image picker intent
     */
    @VisibleForTesting
    void showImagePickerIntent() {
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");

        final List<Intent> extraIntents = new ArrayList<>();
        Context context = contextDelegate.getContext();
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File tempCameraImageFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg", storageDir);
                Uri uri = Uri.fromFile(tempCameraImageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                extraIntents.add(cameraIntent);

                cameraImageFilePath = "file://" + tempCameraImageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            view.getPhotoPresenter().logError(view.getIOExceptionMessage(), e);
        }

        final Intent chooserIntent = Intent.createChooser(galleryIntent,
                                                          context.getString(view.getChooserLabel()));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toArray(new Parcelable[extraIntents.size()]));
        contextDelegate.startActivityForResult(chooserIntent, REQUEST_CODE_PICK_PHOTO);
    }
}
