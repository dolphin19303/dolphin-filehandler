//------------------------------------------------------------------------------
// Copyright (c) 2012 Microsoft Corporation. All rights reserved.
//------------------------------------------------------------------------------

package vinicorp.dolphin.file.backuprestorefiles.sky;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import vinicorp.dolphin.file.backuprestorefiles.DFile;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveAlbum;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveAudio;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveFile;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveFolder;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveObject;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveObject.Visitor;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDrivePhoto;
import vinicorp.dolphin.file.backuprestorefiles.sky.skydrive.SkyDriveVideo;
import vinicorp.dolphin.file.backuprestorefiles.sky.util.JsonKeys;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.Log;
import android.widget.TextView;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveDownloadOperation;
import com.microsoft.live.LiveDownloadOperationListener;
import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveStatus;
import com.microsoft.live.LiveUploadOperationListener;

public class SkydriveHandler {
	private LiveSdkSampleApplication mApp;
	private LiveAuthClient mAuthClient;
	private ProgressDialog mInitializeDialog;
	private TextView mBeginTextView;
	private Activity act;
	private Application app;

	private LiveConnectClient mClient;

	public SkydriveHandler(Activity act, Application app) {
		this.act = act;
		this.app = app;
		mApp = (LiveSdkSampleApplication) app;
		mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
		mApp.setAuthClient(mAuthClient);

		// mInitializeDialog = ProgressDialog.show(this, "",
		// "Initializing. Please wait...", true);
		// make something wait here

		// Check to see if the CLIENT_ID has been changed.
		if (Config.CLIENT_ID.equals("YOUR CLIENT ID HERE")) {

		}
	}

	public boolean isLoggedIn() {
		boolean isLoggedin = true;
		try {
			mApp = (LiveSdkSampleApplication) app;
			mClient = mApp.getConnectClient();
			if (mClient == null)
				isLoggedin = false;
		} catch (NullPointerException e) {
			isLoggedin = false;
		}
		return isLoggedin;
	}

	public void UploadFile(DFile uploadFile) {
		boolean canUpload = true;
		if (!isLoggedIn()) {
			OnUploadSkyChecked(false, "Not login or check session failed");
			canUpload = false;
		}

		File file = null;
		String mCurrentFolderId = null;
		try {
			file = new File(uploadFile.getNameAndDir());
			mCurrentFolderId = uploadFile.getFileHostDir();
		} catch (NullPointerException e) {
			OnUploadSkyChecked(false, "File doesn't exists");
			canUpload = false;
		}
		if (canUpload) {
			final ProgressDialog uploadProgressDialog = new ProgressDialog(act);
			uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			uploadProgressDialog.setMessage("Uploading...");
			uploadProgressDialog.setCancelable(true);
			uploadProgressDialog.show();

			final LiveOperation operation = mClient.uploadAsync(mCurrentFolderId, file.getName(), file, new LiveUploadOperationListener() {
				@Override
				public void onUploadProgress(int totalBytes, int bytesRemaining, LiveOperation operation) {
					int percentCompleted = computePrecentCompleted(totalBytes, bytesRemaining);

					uploadProgressDialog.setProgress(percentCompleted);
				}

				@Override
				public void onUploadFailed(LiveOperationException exception, LiveOperation operation) {
					uploadProgressDialog.dismiss();
					Log.d("Dolphin got error", exception.getMessage());
					OnUploadSkyChecked(false, exception.getMessage());
				}

				@Override
				public void onUploadCompleted(LiveOperation operation) {
					uploadProgressDialog.dismiss();

					JSONObject result = operation.getResult();

					if (result.has(JsonKeys.ERROR)) {
						JSONObject error = result.optJSONObject(JsonKeys.ERROR);
						String message = error.optString(JsonKeys.MESSAGE);
						String code = error.optString(JsonKeys.CODE);
						Log.d("Dolphin got error", code + ": " + message);
						OnUploadSkyChecked(true, "Upload complete." + message);
						return;
					} else
						OnUploadSkyChecked(true, "Upload complete!");
					// loadFolder(mCurrentFolderId);
				}
			});

			uploadProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					operation.cancel();
				}
			});
		}
	}

	public void downloadFile(DFile dFile) {
		boolean canDownload = true;
		if (!isLoggedIn()) {
			OnDownloadSkyChecked(false, "Not login or check session failed");
			canDownload = false;
		}

		String name = null;
		String fileId = null;
		try {

			fileId = dFile.getFileId();
			name = dFile.getFileName();
		} catch (NullPointerException e) {
			OnDownloadSkyChecked(false, "File isn't existsed");
			canDownload = false;
		}
		if (canDownload) {
			File file = new File(dFile.getFileDir(), name);

			final ProgressDialog progressDialog = new ProgressDialog(act);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Downloading...");
			progressDialog.setCancelable(true);
			progressDialog.show();
			final LiveDownloadOperation operation = mClient.downloadAsync(fileId + "/content", file, new LiveDownloadOperationListener() {
				@Override
				public void onDownloadProgress(int totalBytes, int bytesRemaining, LiveDownloadOperation operation) {
					int percentCompleted = computePrecentCompleted(totalBytes, bytesRemaining);

					progressDialog.setProgress(percentCompleted);
				}

				@Override
				public void onDownloadFailed(LiveOperationException exception, LiveDownloadOperation operation) {
					progressDialog.dismiss();
					Log.d("Dolphin download sky err", exception.getMessage());
					OnDownloadSkyChecked(false, exception.getMessage());
				}

				@Override
				public void onDownloadCompleted(LiveDownloadOperation operation) {
					progressDialog.dismiss();
					Log.d("Dolphin download sky done", "File downloaded.");
					OnDownloadSkyChecked(true, "Download complete!");
				}
			});

			progressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					operation.cancel();
					OnDownloadSkyChecked(false, "Download cancel!");
				}
			});
		} else {
			OnDownloadSkyChecked(false, "Download failed.");
		}
	}

	private int computePrecentCompleted(int totalBytes, int bytesRemaining) {
		return (int) (((float) (totalBytes - bytesRemaining)) / totalBytes * 100);
	}

	public void Login() {

		mApp = (LiveSdkSampleApplication) act.getApplication();
		mAuthClient = new LiveAuthClient(mApp, Config.CLIENT_ID);
		mApp.setAuthClient(mAuthClient);

		mInitializeDialog = ProgressDialog.show(act, "", "Initializing. Please wait...", true);

		mAuthClient.initialize(Arrays.asList(Config.SCOPES), new LiveAuthListener() {
			@Override
			public void onAuthError(LiveAuthException exception, Object userState) {
				mInitializeDialog.dismiss();
				showSignIn();
				Log.d("Dolphin got error", exception.getMessage());
			}

			@Override
			public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
				mInitializeDialog.dismiss();

				if (status == LiveStatus.CONNECTED) {
					SetSession(session);
					OnLoginChecked(true, "Session existed. Logged in");
				} else {
					showSignIn();
					Log.d("Dolphin got error", "Initialize did not connect. Please try login in.");

				}
			}
		});
	}

	private void SetSession(LiveConnectSession session) {
		assert session != null;
		mApp.setSession(session);
		mApp.setConnectClient(new LiveConnectClient(session));
		// startActivity(new Intent(getApplicationContext(),
		// MainActivity.class));
	}

	private void showSignIn() {
		mAuthClient.login(act, Arrays.asList(Config.SCOPES), new LiveAuthListener() {
			@Override
			public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
				if (status == LiveStatus.CONNECTED) {
					SetSession(session);
					// //connected
					OnLoginChecked(true, "Login successfully");
				} else {
					OnLoginChecked(false, "Login did not connect. Status is " + status + ".");
				}
			}

			@Override
			public void onAuthError(LiveAuthException exception, Object userState) {
				OnLoginChecked(false, exception.getMessage());
			}
		});
	}

	public void Logout() {
		mAuthClient.logout(new LiveAuthListener() {
			@Override
			public void onAuthError(LiveAuthException exception, Object userState) {
				Log.d("Dophin logout error", exception.getMessage());
			}

			@Override
			public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
				mApp = (LiveSdkSampleApplication) app;
				mApp.setSession(null);
				mApp.setConnectClient(null);
				OnLogoutChecked(true, "Logged out");
			}
		});
	}

	public List<DFile> getFileIdFromName(String fileName) {
		DFile res = null;
		// List<DFile> dolphinListFile = getListFile(DIR_HOME);
		// for (DFile dolphin : dolphinListFile) {
		// if (dolphin.getFileName().compareTo(fileName) == 0)
		// res = dolphin;
		// }
		boolean canGetId = true;
		if (!isLoggedIn()) {
			OnGetIdFromFileNameChecked(null, "Not logged in or check session false");
			canGetId = false;
		}
		if (canGetId) {
			mFileList = new ArrayList<DFile>();
			final String mFileName = fileName;
			mClient.getAsync(DIR_HOME + "/files", new LiveOperationListener() {
				@Override
				public void onComplete(LiveOperation operation) {

					JSONObject result = operation.getResult();
					if (result.has(JsonKeys.ERROR)) {
						JSONObject error = result.optJSONObject(JsonKeys.ERROR);
						String message = error.optString(JsonKeys.MESSAGE);
						String code = error.optString(JsonKeys.CODE);
						Log.d("Dolphin download err", code + ": " + message);
						return;
					}
					JSONArray data = result.optJSONArray(JsonKeys.DATA);

					for (int i = 0; i < data.length(); i++) {
						SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));

						skyDriveObj.accept(new Visitor() {
							@Override
							public void visit(SkyDriveAlbum album) {
								mDFile = new DFile(album.getId(), "N/A", album.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_ALBUM);

							}

							@Override
							public void visit(SkyDrivePhoto photo) {
								mDFile = new DFile(photo.getId(), "N/A", photo.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_PHOTO);
							}

							@Override
							public void visit(SkyDriveFolder folder) {
								mDFile = new DFile(folder.getId(), "N/A", folder.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_FOLDER);
							}

							@Override
							public void visit(SkyDriveFile file) {
								mDFile = new DFile(file.getId(), "N/A", file.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_FILE);
							}

							@Override
							public void visit(SkyDriveVideo video) {
								mDFile = new DFile(video.getId(), "N/A", video.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_VIDEO);
							}

							@Override
							public void visit(SkyDriveAudio audio) {
								mDFile = new DFile(audio.getId(), "N/A", audio.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_AUDIO);
							}
						});
						if (mDFile.getFileName().compareTo(mFileName) == 0) {
							mFileList.add(mDFile);
						}
					}
					OnGetIdFromFileNameChecked(mFileList, "Complete");
				}

				@Override
				public void onError(LiveOperationException exception, LiveOperation operation) {

					Log.d("Dolphin download err", exception.getMessage());
					OnGetListChecked(null, exception.getMessage());
				}
			});
		}
		return mFileList;
	}

	public DFile mDFile;
	public static String DIR_HOME = "me/skydrive";
	public List<DFile> mFileList;

	public List<DFile> getListFile(String dir) {

		boolean canGetListFile = true;
		if (!isLoggedIn()) {
			canGetListFile = false;
		}
		if (canGetListFile) {
			mFileList = new ArrayList<DFile>();

			mClient.getAsync(dir + "/files", new LiveOperationListener() {
				@Override
				public void onComplete(LiveOperation operation) {

					JSONObject result = operation.getResult();
					if (result.has(JsonKeys.ERROR)) {
						JSONObject error = result.optJSONObject(JsonKeys.ERROR);
						String message = error.optString(JsonKeys.MESSAGE);
						String code = error.optString(JsonKeys.CODE);
						Log.d("Dolphin download err", code + ": " + message);
						return;
					}
					JSONArray data = result.optJSONArray(JsonKeys.DATA);

					for (int i = 0; i < data.length(); i++) {
						SkyDriveObject skyDriveObj = SkyDriveObject.create(data.optJSONObject(i));

						skyDriveObj.accept(new Visitor() {
							@Override
							public void visit(SkyDriveAlbum album) {
								mDFile = new DFile(album.getId(), "N/A", album.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_ALBUM);

							}

							@Override
							public void visit(SkyDrivePhoto photo) {
								mDFile = new DFile(photo.getId(), "N/A", photo.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_PHOTO);
							}

							@Override
							public void visit(SkyDriveFolder folder) {
								mDFile = new DFile(folder.getId(), "N/A", folder.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_FOLDER);
							}

							@Override
							public void visit(SkyDriveFile file) {
								mDFile = new DFile(file.getId(), "N/A", file.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_FILE);
							}

							@Override
							public void visit(SkyDriveVideo video) {
								mDFile = new DFile(video.getId(), "N/A", video.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_VIDEO);
							}

							@Override
							public void visit(SkyDriveAudio audio) {
								mDFile = new DFile(audio.getId(), "N/A", audio.getName(), DFile.FILEHOST_SKYDRIVE, DFile.FILETYPE_AUDIO);
							}
						});
						mFileList.add(mDFile);
					}
					OnGetListChecked(mFileList, "Complete");
				}

				@Override
				public void onError(LiveOperationException exception, LiveOperation operation) {

					Log.d("Dolphin download err", exception.getMessage());
					OnGetListChecked(null, exception.getMessage());
				}
			});
		}
		return mFileList;
	}

	// Listener
	OnSkydriveLoginListener onSkydriveLoginListener;
	OnGetListSkydriveListener onGetListListener;
	OnGetFileIdFromNameListener onGetFileIdFromNameListener;
	OnDownloadSkyListener onDownloadSkyListener;
	OnUploadSkyListener onUploadSkyListener;

	public void setOnSkydriveLoginListener(OnSkydriveLoginListener listener) {
		onSkydriveLoginListener = listener;
	}

	public void setOnGetListListener(OnGetListSkydriveListener listener) {
		onGetListListener = listener;
	}

	public void setOnGetFileIdFromNameListener(OnGetFileIdFromNameListener listener) {
		onGetFileIdFromNameListener = listener;
	}

	public void setOnDownloadSkyListener(OnDownloadSkyListener listener) {
		onDownloadSkyListener = listener;
	}

	public void setOnUploadSkyListener(OnUploadSkyListener listener) {
		onUploadSkyListener = listener;
	}

	private void OnLoginChecked(boolean loginResult, String messenger) {
		if (onSkydriveLoginListener != null) {
			onSkydriveLoginListener.onSkydriveLogin(loginResult, messenger);
		}
	}

	private void OnLogoutChecked(boolean logoutuResult, String messenger) {
		if (onSkydriveLoginListener != null) {
			onSkydriveLoginListener.onSkydriveLogout(logoutuResult, messenger);
		}
	}

	private void OnGetListChecked(List<DFile> fileList, String mess) {
		if (onGetListListener != null) {
			onGetListListener.onReceivedListSkydrive(fileList, mess);
		}
	}

	private void OnGetIdFromFileNameChecked(List<DFile> fileList, String mess) {
		if (onGetFileIdFromNameListener != null) {
			onGetFileIdFromNameListener.onGetFileIdFromName(fileList, mess);
		}
	}

	private void OnDownloadSkyChecked(boolean result, String messenger) {
		if (onDownloadSkyListener != null) {
			onDownloadSkyListener.onDownloadSky(result, messenger);
		}
	}

	private void OnUploadSkyChecked(boolean result, String messenger) {
		if (onUploadSkyListener != null) {
			onUploadSkyListener.onUploadSky(result, messenger);
		}
	}

	public interface OnSkydriveLoginListener {
		public abstract void onSkydriveLogin(boolean loginResult, String messenger);

		public abstract void onSkydriveLogout(boolean logoutResult, String messenger);
	}

	public interface OnGetListSkydriveListener {
		public abstract void onReceivedListSkydrive(List<DFile> fileList, String mess);
	}

	public interface OnGetFileIdFromNameListener {
		public abstract void onGetFileIdFromName(List<DFile> mListFile, String messenger);
	}

	public interface OnDownloadSkyListener {
		public abstract void onDownloadSky(boolean result, String messenger);
	}

	public interface OnUploadSkyListener {
		public abstract void onUploadSky(boolean result, String messenger);
	}

}
