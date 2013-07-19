package vinicorp.dolphin.file.backuprestorefiles;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnDownloadListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnFileHostLoginListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnLogoutListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnUploadListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DBRoulette;
import vinicorp.dolphin.file.backuprestorefiles.googledrive.GoogleActivity;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler;
import vinicorp.dolphin.file.backuprestorefiles.sky.util.FilePicker;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
//Hello, Im' dolphin from home
//Hello, Im' dolphin from Vinicorp
//Im' dolphin from Vinicorp too

public class MainActivity extends Activity implements OnFileHostLoginListener, OnDownloadListener, OnUploadListener, OnLogoutListener {
	FileHandler mFileHandler;
	public static int COMMAND_UPLOAD_FILE_DROPBOX = 0;
	public static int COMMAND_UPLOAD_FILE_SKYDRIVE = 1;
	public static int COMMAND_UPLOAD_FILE_GOOGLEDRIVE = 2;
	int currentCommand;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mFileHandler = new FileHandler(getApplication(), this, this);
		mFileHandler.setOnFileHostLoginListener(this);
		mFileHandler.setOnDownloadListener(this);
		mFileHandler.setOnUploadListener(this);
		mFileHandler.setOnLogoutListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// google
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;
	private GoogleAccountCredential credential;
	private static Uri fileUri;
	private static Drive service;

	public void onLoginGoogle(View v) {
		credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE);
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	public void onUploadGoogle(View v) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// File's binary content
					java.io.File fileContent = new java.io.File("/sdcard/a.jpg");
					FileContent mediaContent = new FileContent("image/jpeg", fileContent);

					// File's metadata.
					com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
					body.setTitle(fileContent.getName());
					body.setMimeType("image/jpeg");

					com.google.api.services.drive.model.File file = service.files().insert(body, mediaContent).execute();
					if (file != null) {
						showToast("Photo uploaded: " + file.getTitle());
						// startCameraIntent();
					}
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void onDownloadGoogle(View v) {
		downloadFile();
	}

	private void downloadFile() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					com.google.api.services.drive.model.File file = service.files().get("0B-Iak7O9SfIpYk9zTjZvY2xreVU").execute();
					// FileList file = service.files().list().execute();
					// List<com.google.api.services.drive.model.File> fileList =
					// file.getItems();
					// com.google.api.services.drive.model.File fileItem =
					// fileList.get(0);
					// Log.d("FileID" , fileItem.getId());
					// Log.d("Count", "Retreived file list");
					if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
						HttpResponse resp = service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl())).execute();
						InputStream inputStream = resp.getContent();
						// writeToFile(inputStream);
					}
				} catch (IOException e) {
					Log.e("WriteToFile", e.toString());
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	List<File> mGFile;

	public void onListGoogle(View v) {
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... arg0) {
				// TODO Auto-generated method stub
				try {
					mGFile = retrieveAllFiles();
					int i = 0;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			protected void onPostExecute(String result) {
				Log.d("Dolphin get glist", String.valueOf(mGFile.size()));
			};
		};
		task.execute();
		Log.d("Dolphin counting", "aa");
	}

	private List<File> retrieveAllFiles() throws IOException {
		List<File> result = new ArrayList<File>();
		Files.List request = service.files().list();

		do {
			try {
				FileList files = request.execute();

				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
				System.out.println("An error occurred: " + e);
				request.setPageToken(null);
			}
		} while (request.getPageToken() != null && request.getPageToken().length() > 0);

		return result;
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}

	// dropbox
	public void onLoginDropbox(View v) {
		mFileHandler.Login(FileHandler.FILEHOST_DROPBOX);
	}

	public void onDropboxLogout(View v) {
		mFileHandler.LogOut(FileHandler.FILEHOST_DROPBOX);
	}

	public void onDownloadDropbox(View v) {
		mFileHandler.DownloadFile("dolphin.jpg", FileHandler.FILEHOST_DROPBOX);
	}

	public void onUploadDropbox(View v) {
		currentCommand = COMMAND_UPLOAD_FILE_DROPBOX;
		Intent intent = new Intent(getApplicationContext(), FilePicker.class);
		startActivityForResult(intent, FilePicker.PICK_FILE_REQUEST);
	}

	public void onListFileDropbox(View v) {
		mFileHandler.getListFile(FileHandler.FILEHOST_DROPBOX);
	}

	// Skydrive
	public void onLoginSky(View v) {
		mFileHandler.Login(FileHandler.FILEHOST_SKYDRIVE);
	}

	public void onLogoutSky(View v) {
		mFileHandler.LogOut(FileHandler.FILEHOST_SKYDRIVE);

	}

	public void onDownLoadSky(View v) {
		mFileHandler.DownloadFile("dolphin.jpg", FileHandler.FILEHOST_SKYDRIVE);
	}

	public void onUploadSky(View v) {
		currentCommand = COMMAND_UPLOAD_FILE_SKYDRIVE;
		Intent intent = new Intent(getApplicationContext(), FilePicker.class);
		startActivityForResult(intent, FilePicker.PICK_FILE_REQUEST);
	}

	public void onListFileSky(View v) {
		mFileHandler.getListFile(FileHandler.FILEHOST_SKYDRIVE);
	}

	// process
	@Override
	public void onFileHostLogin(boolean loginResult, String messenger) {
		// TODO Auto-generated method stub
		if (loginResult)
			Toast.makeText(this, "Login complete." + messenger, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "Login false" + messenger, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFileHostUpload(DFile file, boolean upResult, String messenger) {
		// TODO Auto-generated method stub
		if (upResult)
			Toast.makeText(this, "Upload complete", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "Upload failed." + messenger, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onFileHostDownload(DFile file, boolean downloadResult, String messenger) {
		// TODO Auto-generated method stub
		if (downloadResult)
			Toast.makeText(this, "Download complete", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "Download failed." + messenger, Toast.LENGTH_SHORT).show();
	}

	public void onFilePicker(View v) {
		Intent intent = new Intent(getApplicationContext(), FilePicker.class);
		startActivityForResult(intent, FilePicker.PICK_FILE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case FilePicker.PICK_FILE_REQUEST:
			if (resultCode == RESULT_OK) {
				String filePath = data.getStringExtra(FilePicker.EXTRA_FILE_PATH);
				if (TextUtils.isEmpty(filePath)) {
					Toast.makeText(this, "No file was choosen.", Toast.LENGTH_SHORT).show();
					return;
				}
				// mCurrentFolderId
				// File file = new File(filePath);
				String[] mPaths = filePath.split("/");
				String fileName = mPaths[mPaths.length - 1];
				String fileLocalDir = filePath.replace("/" + fileName, "");
				DFile dolphinFile;
				switch (currentCommand) {
				// COMMAND_UPLOAD_FILE_DROPBOX
				case 0:
					dolphinFile = new DFile(fileLocalDir, "/Photos/", fileName, DFile.FILEHOST_DROPBOX);
					mFileHandler.UploadFile(dolphinFile, FileHandler.FILEHOST_DROPBOX);
					break;
				// COMMAND_UPLOAD_FILE_SKYDRIVE
				case 1:
					dolphinFile = new DFile(fileLocalDir, SkydriveHandler.DIR_HOME, fileName, DFile.FILEHOST_SKYDRIVE);
					mFileHandler.UploadFile(dolphinFile, FileHandler.FILEHOST_SKYDRIVE);
					break;
				// COMMAND_UPLOAD_FILE_GOOGLEDRIVE
				case 2:
					// upload code for google here
					break;
				default:
					break;
				}
			}
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					credential.setSelectedAccountName(accountName);
					service = getDriveService(credential);
					// startCameraIntent();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				// saveFileToDrive();
				Toast.makeText(this, "Login complete", Toast.LENGTH_SHORT);
			} else {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;
		}
	}

	@Override
	public void OnLogout(boolean outResult, String messenger) {
		// TODO Auto-generated method stub
		if (outResult)
			Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
	}

	// ////////////////////////////////////////////////
	// dump
	// ////////////////////////////////////////////////
	public void doDropbox(View v) {
		Intent i = new Intent(this, DBRoulette.class);
		startActivity(i);
	}

	public void doSky(View v) {
		// Intent i = new Intent(this, SignInActivity.class);
		// startActivity(i);
	}

	public void doGoogle(View v) {
		Intent i = new Intent(this, GoogleActivity.class);
		startActivity(i);
	}

}
