package com.example.myapp.DataModel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class deals with cretion of an image file from a given bitmap.
 * It is using the patient and activity status to compose the file name
 */
public class ImageStorage implements IImageStorage {

    public String patientID;
    public String activityStatus;
    private Context contextWrapper;
    private ISettingsProvider patientProvider;

    public ImageStorage(Context contextWrapper, ISettingsProvider patientProvider) {
        this.contextWrapper = contextWrapper;
        this.patientProvider = patientProvider;
    }

    public File storeImage(Bitmap image) {
        //obtain a file in the external storage directory
        File pictureFile = getOutputMediaFile();
        //if the file cannot be created throw an error about permissions
        if (pictureFile == null) {
            Log.d(this.getClass().getName(),
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return null;
        }
        //at this point the file is empty
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            //compress the bitmap into a JPG and drop its content into the file previosly created
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            return pictureFile;
        } catch (FileNotFoundException e) {
            Log.d(this.getClass().getName(), "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(this.getClass().getName(), "Error accessing file: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String storageStamp() {
        return this.patientProvider.getPatientName().replace(' ','-')+"-" +this.patientProvider.getPatientStatus() +"-" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + this.contextWrapper.getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String fileName =this.patientProvider.getPatientName().replace(' ','-')+"-"
                +this.patientProvider.getPatientStatus() +"-" + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        // Create a media file name

        File mediaFile;

        //todo use patientID and activity status when constructing the file name
        String mImageName = fileName + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

}
