package vinicorp.dolphin.file.backuprestorefiles;

import java.io.File;

import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnDownloadListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnFileHostLoginListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnLogoutListener;
import vinicorp.dolphin.file.backuprestorefiles.FileHandler.OnUploadListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DBRoulette;
import vinicorp.dolphin.file.backuprestorefiles.googledrive.GoogleActivity;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler;
import vinicorp.dolphin.file.backuprestorefiles.sky.util.FilePicker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
//Hello, Im' dolphin from home
//Hello, Im' dolphin from Vinicorp
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

	public void onLoginGoogle(View v) {

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
		if (requestCode == FilePicker.PICK_FILE_REQUEST) {
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
