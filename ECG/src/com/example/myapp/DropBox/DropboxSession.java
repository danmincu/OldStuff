package com.example.myapp.DropBox;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.example.myapp.IDisplayImage;
import com.example.myapp.IToastMessages;
import com.example.myapp.IUpdater;
import com.example.myapp.ViewModel.IDisplayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;



//http://stackoverflow.com/questions/28254321/android-dropbox-core-api-upload-file-sample?noredirect=1#comment44919614_28254321
//https://www.dropbox.com/developers/core/start/android

/**
 * Dropbox session responsibe for
 * 1: links the application to a dropbox app
 * 2: once obtained it saves the access token in order to communicate with dropbox.
 * 3: saves the access token beyond the lifecycle of the ECG app
 * 4: maintains status (once linked the application is getting authenticated)
 * 5: uploads a file to the dropbox app folder
 * 6: todo - dropbox history
 */
public class DropboxSession implements IDropboxSession {

    // Don't change these, leave them alone.
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    //put your own values here
    final static private String APP_KEY = "v94cozgvcx2losn";
    final static private String APP_SECRET = "9tycrc72pr9g2j2";

    public DropboxAPI<AndroidAuthSession> mDBApi;
    Context contextWrapper;
    List<IUpdater> updaterList = new ArrayList<IUpdater>();

    public DropboxSession(Context contextWrapper) {
        this.contextWrapper = contextWrapper;
        AndroidAuthSession session = buildSession();
        this.mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    /**
     * @return the status of the Dropbox login
     */
    @Override
    public boolean isTheAppLoggedInDropbox() {
        return mDBApi != null && mDBApi.getSession().isLinked();
    }

    /**
     * this method initiates the Dropbox authetication by starting the drobox app on your phone
     * should dropbox not be installed the browser opens inviting the user to login and authenticate
     */
    @Override
    public void authenticateMyApp() {
        this.mDBApi.getSession().startOAuth2Authentication(this.contextWrapper);
    }

    /**
     * @return if successfully authenticated it stores into the SharedPreferences the access token to be reused on the next session
     */
    @Override
    public boolean AuthenticateAndStore() {
        AndroidAuthSession session = mDBApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                session.finishAuthentication();
                this.storeAuth(session);
                update();
                return true;
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
                throw e;
            }
        }
        return false;
    }

    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = this.contextWrapper.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
    }

    @Override
    public void logOut() {
        // Remove credentials from the session
        mDBApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        update();
    }

    @Override
    public boolean putFileAsync(File file, IDisplayImage displayImage) throws FileNotFoundException, DropboxException {
        if (this.isTheAppLoggedInDropbox()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                (new UploadFileToDropboxTask(displayImage)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
            else
                new UploadFileToDropboxTask(displayImage).execute(file);
            return true;
        }
        return false;
    }

    public void listAsync(IDisplayList displayList) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            (new ListFilesTask(displayList)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        else
            (new ListFilesTask(displayList)).execute("");
    }

    @Override
    public void addUpdater(IUpdater updater) {
        this.updaterList.add(updater);
    }

    @Override
    public void removeUpdater(IUpdater updater) {
        this.updaterList.remove(updater);
    }

    private void update() {
        for (IUpdater update : updaterList) {
            update.Update();
        }
    }

    private void clearKeys() {
        SharedPreferences prefs = this.contextWrapper.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = this.contextWrapper.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    private class UploadFileToDropboxTask extends AsyncTask<File, Void, Void> {
               IToastMessages toastMessages;

        public UploadFileToDropboxTask(IToastMessages toastMessages) {
            this.toastMessages = toastMessages;
        }

        @Override
        protected Void doInBackground(File... params) {
            File file = params[0];
            try {
                FileInputStream inputStream = new FileInputStream(file);
                //Im using hte putFileOverride method to overide the eventual file with the same name - should there be one on your dropbox folder
                DropboxAPI.Entry response = mDBApi.putFileOverwrite(file.getName(), inputStream, file.length(), null);
                Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
           // if (this.toastMessages != null)
           //     this.toastMessages.showToast("Image was saved..");
        }
    }


    private class ListFilesTask extends AsyncTask<String, Integer, List<String>> {

        IDisplayList displayList;

        public ListFilesTask(IDisplayList displayList) {
            this.displayList = displayList;
        }

        @Override
        protected List<String> doInBackground(String... params) {

            try {

                if (DropboxSession.this.isTheAppLoggedInDropbox()) {
                    DropboxAPI.Entry entries = DropboxSession.this.mDBApi.metadata("", 1000, null, true, null);

                    //reference http://stackoverflow.com/questions/4066538/sort-an-arraylist-based-on-an-object-field
                    Collections.sort(entries.contents, new Comparator<DropboxAPI.Entry>() {
                        @Override
                        public int compare(DropboxAPI.Entry lhs, DropboxAPI.Entry rhs) {

                            //reference http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
                            //Thu, 19 Feb 2015 05:46:32 +0000
                            DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                            try {
                                Date lhs_parsed = format.parse(lhs.clientMtime);
                                Date rhs_parsed = format.parse(rhs.clientMtime);
                                //-1 for desc order
                                return -1 * lhs_parsed.compareTo(rhs_parsed);
                            } catch (ParseException e) {
                                return 0;
                            }
                        }
                    });

                    List<String> list = new ArrayList<String>();
                    for (DropboxAPI.Entry e : entries.contents) {
                        if (!e.isDeleted && !e.isDir) {
                            list.add(e.fileName());
                            DropboxAPI.DropboxLink shareLink = DropboxSession.this.mDBApi.share(e.path);
                            list.add(shareLink.url);


                        }
                    }
                    return list;
                } else
                    return Arrays.asList("Not linked to dropbox. Cannot display history", "http://dropbox.com");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Arrays.asList("Error - check internet connection. Cannot display history", "http://dropbox.com");
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (this.displayList != null)
                this.displayList.displayList(result);
            DropboxSession.this.update();
        }

    }


}
