package com.example.myapp.DataModel;

import android.graphics.Bitmap;

import java.io.File;

public interface IImageStorage {
    File storeImage(Bitmap image);
    String storageStamp();
}
