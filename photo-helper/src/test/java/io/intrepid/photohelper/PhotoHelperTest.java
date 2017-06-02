package io.intrepid.photohelper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static io.intrepid.photohelper.PhotoHelper.CAMERA_IMAGE_FILE_PATH;
import static io.intrepid.photohelper.PhotoHelper.REQUEST_CODE_PICK_PHOTO;
import static io.intrepid.photohelper.PhotoHelper.REQUEST_CODE_WRITE_STORAGE_PERMISSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public abstract class PhotoHelperTest {
    @Mock
    PhotoContract.View mockView;
    @Mock
    PhotoContract.Presenter<PhotoContract.View> mockPresenter;

    @NonNull
    protected abstract PhotoHelper getPhotoHelper();

    private PhotoContract.Helper photoHelper;

    @RunWith(MockitoJUnitRunner.class)
    public static class ActivityTest extends PhotoHelperTest {
        @Mock
        Activity mockActivity;

        @NonNull
        @Override
        protected PhotoHelper getPhotoHelper() {
            return new PhotoHelper(mockActivity, mockView, false);
        }
    }

    @RunWith(MockitoJUnitRunner.class)
    public static class FragmentTest extends PhotoHelperTest {
        @Mock
        Fragment mockFragment;

        @NonNull
        @Override
        protected PhotoHelper getPhotoHelper() {
            return new PhotoHelper(mockFragment, mockView, false);
        }
    }

    @Before
    public void setup() {
        photoHelper = spy(getPhotoHelper());
        when(mockView.getPhotoPresenter()).thenReturn(mockPresenter);
    }

    @Test
    public void onActivityResult_selectFromLibrary() {
        final String PHOTO_URI = "http://example.com/foo.png";
        Uri mockUri = Mockito.mock(Uri.class);
        when(mockUri.toString()).thenReturn(PHOTO_URI);
        Intent mockIntent = Mockito.mock(Intent.class);
        when(mockIntent.getData()).thenReturn(mockUri);

        photoHelper.onActivityResult(REQUEST_CODE_PICK_PHOTO, Activity.RESULT_OK, mockIntent);

        verify(mockPresenter).onPictureChosen(PHOTO_URI);
        verify(mockPresenter, never()).onNoPictureChosen();
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        verify(realPhotoHelper).deleteUnusedPhotos();
    }

    @Test
    public void onActivityResult_takeNewPhoto() {
        final String PHOTO_URI = "http://example.com/foo.png";
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        realPhotoHelper.cameraImageFilePath = PHOTO_URI;

        photoHelper.onActivityResult(REQUEST_CODE_PICK_PHOTO, Activity.RESULT_OK, null);

        verify(mockPresenter).onPictureChosen(PHOTO_URI);
        verify(mockPresenter, never()).onNoPictureChosen();
        verify(realPhotoHelper, never()).deleteUnusedPhotos();
    }

    @Test
    public void onActivityResult_cancel() {
        Intent mockIntent = Mockito.mock(Intent.class);

        photoHelper.onActivityResult(REQUEST_CODE_PICK_PHOTO, Activity.RESULT_CANCELED, mockIntent);

        verify(mockPresenter, never()).onPictureChosen(anyString());
        verify(mockPresenter).onNoPictureChosen();
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        verify(realPhotoHelper).deleteUnusedPhotos();
    }

    @Test
    public void onCreate_withSavedState() {
        Bundle mockBundle = Mockito.mock(Bundle.class);
        final String SAVED_CAMERA_IMAGE_FILE_PATH = "path";
        when(mockBundle.getString(CAMERA_IMAGE_FILE_PATH)).thenReturn(SAVED_CAMERA_IMAGE_FILE_PATH);

        photoHelper.onCreate(mockBundle);

        verify(mockBundle).getString(CAMERA_IMAGE_FILE_PATH);
    }

    @Test
    public void onRequestPermissionsResult_permissionGranted() {
        // Mock out the showImagePickerIntent() method, since it's dependent on Android-specific code
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        doNothing().when(realPhotoHelper).showImagePickerIntent();

        photoHelper.onRequestPermissionsResult(REQUEST_CODE_WRITE_STORAGE_PERMISSION,
                                               new String[] {},
                                               new int[] { PackageManager.PERMISSION_GRANTED });

        verify(realPhotoHelper).showImagePickerIntent();
        verify(mockPresenter, never()).logError(anyString(), any(Exception.class));
        verify(mockPresenter, never()).onPermissionDenied();
    }

    @Test
    public void onRequestPermissionsResult_permissionDenied() {
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        final String PERMISSION_DENIED_MESSAGE = "DENIED";
        when(mockView.getPermissionDeniedMessage()).thenReturn(PERMISSION_DENIED_MESSAGE);

        photoHelper.onRequestPermissionsResult(REQUEST_CODE_WRITE_STORAGE_PERMISSION,
                                               new String[] {},
                                               new int[] { PackageManager.PERMISSION_DENIED });

        verify(realPhotoHelper, never()).showImagePickerIntent();
        verify(mockView).getPermissionDeniedMessage();
        verify(mockPresenter).logError(PERMISSION_DENIED_MESSAGE, null);
    }

    @Test
    public void onSaveInstanceState_noCameraImageFilePath() {
        Bundle mockBundle = Mockito.mock(Bundle.class);

        photoHelper.onSaveInstanceState(mockBundle);

        verify(mockBundle, never()).putString(CAMERA_IMAGE_FILE_PATH, null);
    }

    @Test
    public void onSaveInstanceState_withCameraImageFilePath() {
        Bundle mockBundle = Mockito.mock(Bundle.class);
        final String CAMERA_PATH = "path";
        PhotoHelper realPhotoHelper = (PhotoHelper) photoHelper;
        realPhotoHelper.cameraImageFilePath = CAMERA_PATH;

        photoHelper.onSaveInstanceState(mockBundle);

        verify(mockBundle).putString(CAMERA_IMAGE_FILE_PATH, CAMERA_PATH);
    }
}
