package vinicorp.dolphin.file.backuprestorefiles;

import java.util.List;

import vinicorp.dolphin.file.backuprestorefiles.dropbox.DropboxHandler;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DropboxHandler.OnDownloadDropboxListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DropboxHandler.OnDropboxLoginListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DropboxHandler.OnGetListDropboxListener;
import vinicorp.dolphin.file.backuprestorefiles.dropbox.DropboxHandler.OnUploadDropboxListener;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler.OnDownloadSkyListener;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler.OnGetFileIdFromNameListener;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler.OnGetListSkydriveListener;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler.OnSkydriveLoginListener;
import vinicorp.dolphin.file.backuprestorefiles.sky.SkydriveHandler.OnUploadSkyListener;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class FileHandler implements OnSkydriveLoginListener, OnDropboxLoginListener, OnGetListSkydriveListener, OnGetListDropboxListener, OnGetFileIdFromNameListener,
		OnDownloadSkyListener, OnUploadSkyListener, OnDownloadDropboxListener, OnUploadDropboxListener {
	public static final String FILEHOST_SKYDRIVE = "skydrive";
	public static final String FILEHOST_GOOGLEDRIVE = "googledrive";
	public static final String FILEHOST_DROPBOX = "dropbox";
	public SkydriveHandler mSkydriveHandler;
	public DropboxHandler mDropboxHandler;
	Application app;
	Activity act;
	Context ct;
	DFile mFile;

	public FileHandler(Application app, Activity act, Context ct) {
		this.app = app;
		this.act = act;
		this.ct = ct;
		mSkydriveHandler = new SkydriveHandler(act, app);
		mSkydriveHandler.setOnSkydriveLoginListener(this);
		mSkydriveHandler.setOnGetListListener(this);
		mSkydriveHandler.setOnGetFileIdFromNameListener(this);
		mSkydriveHandler.setOnDownloadSkyListener(this);
		mSkydriveHandler.setOnUploadSkyListener(this);
		mDropboxHandler = new DropboxHandler(act);
		mDropboxHandler.setOnDropboxLoginListener(this);
		mDropboxHandler.setOnGetListDropboxListener(this);
		mDropboxHandler.setOnDownloadDropboxListener(this);
		mDropboxHandler.setOnUploadDropboxListener(this);

	}

	// process
	public void UploadFile(DFile uploadFile, String host) {
		mFile = uploadFile;
		if (host.compareTo(FILEHOST_DROPBOX) == 0) {
			mDropboxHandler.UploadFile(uploadFile);
		} else if (host.compareTo(FILEHOST_GOOGLEDRIVE) == 0) {
		} else if (host.compareTo(FILEHOST_SKYDRIVE) == 0) {
			mSkydriveHandler.UploadFile(uploadFile);
		}

	}

	public void DownloadFile(String fileName, String host) {
		if (host.compareTo(FILEHOST_DROPBOX) == 0) {
			mDropboxHandler.downloadFile(fileName);
		} else if (host.compareTo(FILEHOST_GOOGLEDRIVE) == 0) {
		} else if (host.compareTo(FILEHOST_SKYDRIVE) == 0) {
			mSkydriveHandler.getFileIdFromName(fileName);
		}
	}

	public void getListFile(String host) {
		if (host.compareTo(FILEHOST_DROPBOX) == 0) {
			mDropboxHandler.getListFile();
		} else if (host.compareTo(FILEHOST_GOOGLEDRIVE) == 0) {
		} else if (host.compareTo(FILEHOST_SKYDRIVE) == 0) {
			mSkydriveHandler.getListFile(SkydriveHandler.DIR_HOME);
		}

	}

	public void Login(String hostedFile) {
		if (hostedFile.compareTo(FILEHOST_DROPBOX) == 0) {
			mDropboxHandler.Login();
		} else if (hostedFile.compareTo(FILEHOST_GOOGLEDRIVE) == 0) {
		} else if (hostedFile.compareTo(FILEHOST_SKYDRIVE) == 0) {
			mSkydriveHandler.Login();
		}
	}

	public void LogOut(String hostedFile) {
		if (hostedFile.compareTo(FILEHOST_DROPBOX) == 0) {
			mDropboxHandler.logOut();
		} else if (hostedFile.compareTo(FILEHOST_GOOGLEDRIVE) == 0) {
		} else if (hostedFile.compareTo(FILEHOST_SKYDRIVE) == 0) {
			mSkydriveHandler.Logout();
		}
	}

	// skydrive
	@Override
	public void onSkydriveLogin(boolean loginResult, String messenger) {
		// TODO Auto-generated method stub
		OnFileHostLoginChecked(loginResult, messenger);
	}

	@Override
	public void onUploadSky(boolean result, String messenger) {
		// TODO Auto-generated method stub
		onFileHostUploadChecked(mFile, result, messenger);
	}

	@Override
	public void onDownloadSky(boolean result, String messenger) {
		// TODO Auto-generated method stub
		onFileHostDownloadChecked(mFile, result, messenger);
	}

	@Override
	public void onReceivedListSkydrive(List<DFile> fileList, String mess) {
		// TODO Auto-generated method stub
		for (DFile f : fileList) {
			Log.d("Dolphin file skydrive: ", f.getFileName());
		}
	}

	@Override
	public void onGetFileIdFromName(List<DFile> mListFile, String messenger) {
		// TODO Auto-generated method stub
		if (mListFile != null)
			for (DFile dolphinFile : mListFile) {
				dolphinFile.setFileDir("/mnt/sdcard");
				mSkydriveHandler.downloadFile(dolphinFile);
				mFile = dolphinFile;
			}
		else {
			onFileHostDownloadChecked(null, false, "File not existed or Not logged in");
		}
	}

	@Override
	public void onSkydriveLogout(boolean logoutResult, String messenger) {
		// TODO Auto-generated method stub
		OnLogoutChecked(logoutResult, messenger);
	}

	// dropbox
	@Override
	public void onDropboxLogin(boolean loginResult, String messenger) {
		// TODO Auto-generated method stub
		OnFileHostLoginChecked(loginResult, messenger);
	}

	@Override
	public void onUploadDropbox(boolean uploadResult, String mess) {
		// TODO Auto-generated method stub
		onFileHostUploadChecked(mFile, uploadResult, mess);
	}

	@Override
	public void onDownloadDropbox(boolean downloadResult, String mess) {
		// TODO Auto-generated method stub
		onFileHostDownloadChecked(mFile, downloadResult, mess);
	}

	@Override
	public void onSignoutDropbox(boolean signout, String mess) {
		// TODO Auto-generated method stub
		OnLogoutChecked(signout, mess);
	}

	@Override
	public void onReceivedListDropbox(List<DFile> fileList, String mess) {
		// TODO Auto-generated method stub
		for (DFile f : fileList) {
			Log.d("Dolphin file dropbox: ", f.getFileName());
		}
	}

	@Override
	public void onDropboxSession(boolean sessionExisted, String messenger) {
		// TODO Auto-generated method stub
		OnFileHostLoginChecked(sessionExisted, "Session existed. Logged in");
	}

	// /////////////////////////////////////////////////////////////////////////
	// Listener
	// /////////////////////////////////////////////////////////////////////////
	OnFileHostLoginListener onFileHostLoginListener;
	OnDownloadListener onDownloadListener;
	OnUploadListener onUploadListener;
	OnLogoutListener onLogoutListener;

	public void setOnFileHostLoginListener(OnFileHostLoginListener listener) {
		onFileHostLoginListener = listener;
	}

	public void setOnDownloadListener(OnDownloadListener listener) {
		onDownloadListener = listener;
	}

	public void setOnUploadListener(OnUploadListener listener) {
		onUploadListener = listener;
	}

	public void setOnLogoutListener(OnLogoutListener listener) {
		onLogoutListener = listener;
	}

	private void OnFileHostLoginChecked(boolean loginResult, String messenger) {
		if (onFileHostLoginListener != null) {
			onFileHostLoginListener.onFileHostLogin(loginResult, messenger);
		}
	}

	private void onFileHostDownloadChecked(DFile file, boolean downloadResult, String messenger) {
		// Check if the Listener was set, otherwise we'll get an Exception when
		// we try to call it
		if (onDownloadListener != null) {
			onDownloadListener.onFileHostDownload(file, downloadResult, messenger);
		}
	}

	private void onFileHostUploadChecked(DFile file, boolean uploadResult, String messenger) {
		// Check if the Listener was set, otherwise we'll get an Exception when
		// we try to call it
		if (onUploadListener != null) {
			onUploadListener.onFileHostUpload(file, uploadResult, messenger);
		}
	}

	private void OnLogoutChecked(boolean outResult, String messenger) {
		// Check if the Listener was set, otherwise we'll get an Exception when
		// we try to call it
		if (onLogoutListener != null) {
			onLogoutListener.OnLogout(outResult, messenger);
		}
	}

	public interface OnFileHostLoginListener {
		public abstract void onFileHostLogin(boolean loginResult, String messenger);
	}

	public interface OnDownloadListener {
		public abstract void onFileHostDownload(DFile file, boolean downloadResult, String messenger);
	}

	public interface OnUploadListener {
		public abstract void onFileHostUpload(DFile file, boolean upResult, String messenger);
	}

	public interface OnLogoutListener {
		public abstract void OnLogout(boolean outResult, String messenger);
	}

}
