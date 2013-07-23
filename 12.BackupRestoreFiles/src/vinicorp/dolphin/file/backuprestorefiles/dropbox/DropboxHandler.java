/*
 * Copyright (c) 2010-11 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package vinicorp.dolphin.file.backuprestorefiles.dropbox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vinicorp.dolphin.file.backuprestorefiles.DFile;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DownloadPicture.OnAsyncDownloadListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.UploadPicture.OnAsyncUploadListener;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxHandler implements OnAsyncDownloadListener, OnAsyncUploadListener {
	private static final String TAG = "DBRoulette";

	// /////////////////////////////////////////////////////////////////////////
	// Your app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static private String APP_KEY = "pi3w6cmueewctip";
	final static private String APP_SECRET = "iq3scz8glgy6dtk";
	// If you'd like to change the access type to the full Dropbox instead of
	// an app folder, change this value.
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

	// /////////////////////////////////////////////////////////////////////////
	// End app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// You don't need to change these, leave them alone.
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	DropboxAPI<AndroidAuthSession> mApi;
	DropboxAPI<WebAuthSession> mWebApi;
	private boolean mLoggedIn;

	// Android widgets
	private Button mSubmit;
	private LinearLayout mDisplay;
	private Button mPhoto;
	private Button mRoulette;

	private ImageView mImage;

	private final String PHOTO_DIR = "/Photos/";

	final static private int NEW_PICTURE = 1;
	private String mCameraFileName;
	Activity ct;

	public DropboxHandler(Activity ct) {
		this.ct = ct;

		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);
//		mWebApi = new DropboxAPI<WebAuthSession>(session);
		checkAppKeySetup();

		// Display the proper UI state if logged in or not
		setLoggedIn(mApi.getSession().isLinked());
		CheckSession();

	}

	public void Login() {
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		checkAppKeySetup();
		CheckSession();
		if (mApi.getSession().isLinked()) {
			// logOut();
			OnSessionChecked(true, "Session existed");
		} else {
			// Start the remote authentication
			mApi.getSession().startAuthentication(ct);

		}
	}

	public void getAccountInfo() {
		Account mAccount;
		try {
			mAccount = mApi.accountInfo();
		} catch (DropboxUnlinkedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DropboxServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DropboxIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// DropboxUnlinkedException - if you have not set an access token pair on
	// the session, or if the user has revoked access.
	// DropboxServerException - if the server responds with an error code. See
	// the constants in DropboxServerException for the meaning of each error
	// code.
	// DropboxIOException - if any network-related error occurs.
	// DropboxException - for any other unknown errors. This is also a
	// superclass of all other Dropbox exceptions, so you may want to only catch
	// this exception which signals that some
	// public void WriteFile() {
	// Intent intent = new Intent();
	// // Picture from camera
	// intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
	//
	// // This is not the right way to do this, but for some reason,
	// // having
	// // it store it in
	// // MediaStore.Images.Media.EXTERNAL_CONTENT_URI isn't working
	// // right.
	//
	// Date date = new Date();
	// DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
	//
	// String newPicFile = df.format(date) + ".jpg";
	// String outPath = "/sdcard/" + newPicFile;
	// File outFile = new File(outPath);
	//
	// mCameraFileName = outFile.toString();
	// Uri outuri = Uri.fromFile(outFile);
	// intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);
	// Log.i(TAG, "Importing New Picture: " + mCameraFileName);
	// try {
	// startActivityForResult(intent, NEW_PICTURE);
	// } catch (ActivityNotFoundException e) {
	// showToast("There doesn't seem to be a camera.");
	// }
	// }

	public void downloadFile(String fileName) {
		if (CheckSession()) {
			DownloadPicture download = new DownloadPicture(ct, mApi, PHOTO_DIR, "dolphin.jpg");
			download.setOnAsyncDownloadListener(this);
			download.execute();
		} else {
			OnDownloadDropboxChecked(false, "Not logged in.");
		}

	}

	public boolean CheckSession() {
		AndroidAuthSession session = mApi.getSession();
		boolean isSessionExisted = true;
		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
				OnSessionChecked(true, "Session existed");
			} catch (IllegalStateException e) {
				isSessionExisted = false;
				OnSessionChecked(false, e.getLocalizedMessage());
			}
		} else
			isSessionExisted = false;
		return isSessionExisted;
	}

	// This is what gets called on finishing a media piece to import
	// @Override
	// public void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// if (requestCode == NEW_PICTURE) {
	// // return from file upload
	// if (resultCode == Activity.RESULT_OK) {
	// Uri uri = null;
	// if (data != null) {
	// uri = data.getData();
	// }
	// if (uri == null && mCameraFileName != null) {
	// uri = Uri.fromFile(new File(mCameraFileName));
	// }
	// File file = new File(mCameraFileName);
	//
	// if (uri != null) {
	// UploadPicture upload = new UploadPicture(ct, mApi,
	// PHOTO_DIR, file);
	// upload.execute();
	// }
	// } else {
	// Log.w(TAG, "Unknown Activity Result from mediaImport: "
	// + resultCode);
	// }
	// }
	// }

	public void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();

		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setLoggedIn(false);
		OnSignoutDropboxChecked(true, "Logged out");
	}

	/**
	 * Convenience function to change UI state based on being logged in
	 */
	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		if (loggedIn) {
			// do something here
		} else {
			// mSubmit.setText("Link with Dropbox");
			// mDisplay.setVisibility(View.GONE);
			// mImage.setImageDrawable(null);
			// do something here
		}
	}

	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
			// showToast("You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.");
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = ct.getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			// showToast("URL scheme in your app's "
			// + "manifest is not set up correctly. You should have a "
			// + "com.dropbox.client2.android.AuthActivity with the "
			// + "scheme: " + scheme);

		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 * 
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	private String[] getKeys() {
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 */
	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys() {
		SharedPreferences prefs = ct.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		}

		return session;
	}

	public void UploadFile(DFile uploadFile) {
		String fileName = null;
		String fileHostPath = null;
		try {
			fileName = uploadFile.getNameAndDir();
			fileHostPath = uploadFile.getFileHostDir();
		} catch (NullPointerException e) {
			OnUploadDropboxChecked(false, "File not found!");
		}

		File file = new File(fileName);
		UploadPicture upload = new UploadPicture(ct, mApi, fileHostPath, file);
		upload.setOnAsyncUploadListener(this);
		upload.execute();
	}

	public void getListFile() {
		com.dropbox.client2.DropboxAPI.Entry entries = null;
		try {
			entries = mApi.metadata("/Photos/", 100, null, true, null);
		} catch (DropboxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<DFile> mFile = new ArrayList<DFile>();
		for (com.dropbox.client2.DropboxAPI.Entry e : entries.contents) {
			if (!e.isDeleted) {
				Log.i("Is Folder", String.valueOf(e.isDir));
				Log.i("Item Name", e.fileName());
				if (e.isDir)
					mFile.add(new DFile("", "", e.fileName(), DFile.FILEHOST_DROPBOX, DFile.FILETYPE_FOLDER));
				else
					mFile.add(new DFile("", "", e.fileName(), DFile.FILEHOST_DROPBOX, DFile.FILETYPE_FILE));
			}
		}
		OnGetListChecked(mFile, "Dropbox get file successful");
	}

	@Override
	public void OnAsyncDownload(boolean res, String messenger) {
		// TODO Auto-generated method stub
		OnDownloadDropboxChecked(res, messenger);
	}

	@Override
	public void OnAsyncUpload(boolean res, String messenger) {
		// TODO Auto-generated method stub
		OnUploadDropboxChecked(res, messenger);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// /Listener
	// ////////////////////////////////////////////////////////////////////////////

	OnGetListDropboxListener onGetListDropboxListener;
	OnDropboxLoginListener onDropboxLoginListener;
	OnDownloadDropboxListener onDownloadDropboxListener;
	OnUploadDropboxListener onUploadDropboxListener;

	private void OnLoginChecked(boolean loginResult, String messenger) {
		// Check if the Listener was set, otherwise we'll get an Exception when
		// we try to call it
		if (onDropboxLoginListener != null) {
			onDropboxLoginListener.onDropboxLogin(loginResult, messenger);
		}
	}

	private void OnSessionChecked(boolean sessionExisted, String messenger) {
		// Check if the Listener was set, otherwise we'll get an Exception when
		// we try to call it
		if (onDropboxLoginListener != null) {
			onDropboxLoginListener.onDropboxSession(sessionExisted, messenger);
		}
	}

	private void OnSignoutDropboxChecked(boolean res, String mess) {
		if (onDropboxLoginListener != null) {
			onDropboxLoginListener.onSignoutDropbox(res, mess);
		}
	}

	private void OnGetListChecked(List<DFile> fileList, String mess) {
		if (onGetListDropboxListener != null) {
			onGetListDropboxListener.onReceivedListDropbox(fileList, mess);
		}
	}

	private void OnDownloadDropboxChecked(boolean res, String mess) {
		if (onDownloadDropboxListener != null) {
			onDownloadDropboxListener.onDownloadDropbox(res, mess);
		}
	}

	private void OnUploadDropboxChecked(boolean res, String mess) {
		if (onUploadDropboxListener != null) {
			onUploadDropboxListener.onUploadDropbox(res, mess);
		}
	}

	public void setOnDropboxLoginListener(OnDropboxLoginListener listener) {
		onDropboxLoginListener = listener;
	}

	public void setOnGetListDropboxListener(OnGetListDropboxListener listener) {
		onGetListDropboxListener = listener;
	}

	public void setOnDownloadDropboxListener(OnDownloadDropboxListener listener) {
		onDownloadDropboxListener = listener;
	}

	public void setOnUploadDropboxListener(OnUploadDropboxListener listener) {
		onUploadDropboxListener = listener;
	}

	public interface OnDropboxLoginListener {
		public abstract void onDropboxLogin(boolean loginResult, String messenger);

		public abstract void onDropboxSession(boolean sessionExisted, String messenger);

		public abstract void onSignoutDropbox(boolean signout, String mess);
	}

	public interface OnGetListDropboxListener {
		public abstract void onReceivedListDropbox(List<DFile> fileList, String mess);
	}

	public interface OnDownloadDropboxListener {
		public abstract void onDownloadDropbox(boolean downloadResult, String mess);
	}

	public interface OnUploadDropboxListener {
		public abstract void onUploadDropbox(boolean uploadResult, String mess);
	}

}
