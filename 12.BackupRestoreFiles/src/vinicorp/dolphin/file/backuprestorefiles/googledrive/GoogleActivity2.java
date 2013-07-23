package vinicorp.dolphin.file.backuprestorefiles.googledrive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vinicorp.dolphin.file.backuprestorefiles.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class GoogleActivity2 extends Activity {

	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_AUTHORIZATION = 2;
	static final int CAPTURE_IMAGE = 3;

	private static Uri fileUri;
	private static Drive service;
	private GoogleAccountCredential credential;

	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

	private static final String CLIENTSECRETS_LOCATION = "client_secrets.json";

	private static final String CLIENT_ID = "761092507359-9l445lht0iep8ghvi6lplpnehqji9tja.apps.googleusercontent.com";

	private static final String CLIENT_SECRET = "mruykd18e-XI-qxLRpV4H6rv ";

	private static GoogleClientSecrets clientSecrets = null;
	private static final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/drive.file", "https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_test);

		credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE);
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
				String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					// credential.setSelectedAccountName(accountName);
					// service = getDriveService(credential);
					startCameraIntent();
					// saveFileToDrive();
				}
			}
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				saveFileToDrive();
			} else {
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
			}
			break;
		case CAPTURE_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				saveFileToDrive();
			}
		}
	}

	private void startCameraIntent() {
		String mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		// fileUri = Uri.fromFile(new java.io.File(mediaStorageDir +
		// java.io.File.separator + "IMG_"
		// + timeStamp + ".jpg"));

		fileUri = Uri.fromFile(new java.io.File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + "IMG_" + timeStamp + ".jpg"));

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(cameraIntent, CAPTURE_IMAGE);
	}

	private void saveFileToDrive() {
		final String TAG = null;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// media scanned to each file for every new times it was
					// created

					File fil = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");

					File[] Files = fil.listFiles();

					for (int count = 0; count < Files.length; count++) {
						java.io.File fs = new java.io.File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + Files[count].getName());

						MediaScannerConnection.scanFile(GoogleActivity2.this, new String[] { fs.toString() }, null,
								new MediaScannerConnection.OnScanCompletedListener() {

									public void onScanCompleted(String path, Uri uri) {
										// TODO Auto-generated method stub

									}
								});

					}

					// File's binary content
					AccountManager am = (AccountManager) getSystemService(ACCOUNT_SERVICE);

					Account[] accounts = am.getAccountsByType("com.google");

					AccountManagerFuture<Bundle> accFut = AccountManager.get(getApplicationContext()).getAuthToken(accounts[0],
							"oauth2:" + "https://www.googleapis.com/auth/drive", true, null, null);
					Bundle authTokenBundle;

					authTokenBundle = accFut.getResult();

					final String token = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();

					am.invalidateAuthToken("com.google", token);

					HttpTransport httpTransport2 = new NetHttpTransport();

					Log.d(TAG, "AuthToken: " + token);

					JacksonFactory jsonFactory2 = new JacksonFactory();
					Log.d(TAG, "RefreshToken 123");

					// Intent intent = new Intent();
					String accountname = authTokenBundle.get(AccountManager.KEY_ACCOUNT_NAME).toString();
					Log.d(TAG, "Account Name : " + accountname);

					GoogleCredential credent = new GoogleCredential.Builder().setTransport(new NetHttpTransport()).setJsonFactory(new JacksonFactory())
							.setClientSecrets(CLIENT_ID, CLIENT_SECRET).build().setAccessToken(token);

					HttpTransport httpTransport = new NetHttpTransport();

					JacksonFactory jsonFactory = new JacksonFactory();

					Drive.Builder b = new Drive.Builder(httpTransport, jsonFactory, credent);

					final Drive drive = b.build();

					Log.d(TAG, "Drive is been build 789");

					java.io.File fileContent = new java.io.File(fileUri.getPath());
					// java.io.File fileContent = new
					// java.io.File(Environment.getExternalStorageDirectory() +
					// "/DCIM/Camera/IMG.jpg");
					FileContent mediaContent = new FileContent("image/jpeg", fileContent);

					// File's metadata.
					com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
					body.setTitle(fileContent.getName());
					body.setMimeType("image/jpeg");

					com.google.api.services.drive.model.File file = drive.files().insert(body, mediaContent).execute();
					Log.d(TAG, "File is been sent 101112");
					if (file != null) {
						showToast("Photo is uploaded to Google drive: " + file.getTitle());
						startCameraIntent();
					}
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (OperationCanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	private static GoogleClientSecrets loadClientSecrets(JsonFactory jsonFactory) throws IOException {
		if (clientSecrets == null) {
			InputStream inputStream = GoogleActivity2.class.getResourceAsStream(CLIENTSECRETS_LOCATION);
			Preconditions.checkNotNull(inputStream, "missing resource %s", CLIENTSECRETS_LOCATION);
			clientSecrets = GoogleClientSecrets.load(jsonFactory, inputStream);
			Preconditions.checkArgument(
					!clientSecrets.getDetails().getClientId().startsWith("[[") && !clientSecrets.getDetails().getClientSecret().startsWith("[["),
					"Please enter your client ID and secret from the Google APIs Console in %s from the " + "root samples directory", CLIENTSECRETS_LOCATION);
		}
		return clientSecrets;
	}

	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).build();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
