package com.example.myapp.DropBox;

import com.dropbox.client2.exception.DropboxException;
import com.example.myapp.IDisplayImage;
import com.example.myapp.IUpdater;
import com.example.myapp.ViewModel.IDisplayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.EventListenerProxy;
import java.util.List;

public interface IDropboxSession {
    boolean isTheAppLoggedInDropbox();

    void authenticateMyApp();

    boolean AuthenticateAndStore();

    void logOut();

    // uploads a file
    boolean putFileAsync(File file, IDisplayImage displayImage) throws FileNotFoundException, DropboxException;

    // history
    void listAsync(IDisplayList displayList);

    void addUpdater(IUpdater updater);

    void removeUpdater(IUpdater updater);
}


