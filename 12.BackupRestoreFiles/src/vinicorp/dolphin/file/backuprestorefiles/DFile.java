package vinicorp.dolphin.file.backuprestorefiles;

public class DFile {
	private String sFileId;
	private String sFileLocalDir;
	private String sFileHostDir;
	private int iFileHost;
	private int iFileType;
	private String sFileName;
	public static final int FILEHOST_DROPBOX = 0;
	public static final int FILEHOST_SKYDRIVE = 1;
	public static final int FILEHOST_GOOGLEDRIVE = 2;
	public static final int FILETYPE_ALBUM = 0;
	public static final int FILETYPE_PHOTO = 1;
	public static final int FILETYPE_FOLDER = 2;
	public static final int FILETYPE_FILE = 3;
	public static final int FILETYPE_VIDEO = 4;
	public static final int FILETYPE_AUDIO = 5;

	public DFile() {
	}

	public DFile(String fileId, String sFileDir, String sFileName, int fileHost, int fileType) {
		this.sFileId = fileId;
		this.sFileLocalDir = sFileDir;
		this.sFileName = sFileName;
		this.iFileHost = fileHost;
		this.iFileType = fileType;

	}

	public DFile(String sFileLocalDir, String sFileHostDir, String sFileName, int fileHost) {
		this.sFileLocalDir = sFileLocalDir;
		this.sFileHostDir = sFileHostDir;
		this.sFileName = sFileName;
		this.iFileHost = fileHost;
	}

	public String getFileName() {
		return sFileName;
	}

	public String getFileDir() {
		return sFileLocalDir;
	}

	public String getFileId() {
		return sFileId;
	}

	public void setFileName(String sFileName) {
		this.sFileName = sFileName;
	}

	public void setFileDir(String sFileDir) {
		this.sFileLocalDir = sFileDir;
	}

	public String getNameAndDir() {
		return sFileLocalDir + "/" + sFileName;
	}

	public String getFileHostDir() {
		return sFileHostDir;
	}
}
