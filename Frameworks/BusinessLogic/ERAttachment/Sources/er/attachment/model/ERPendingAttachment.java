package er.attachment.model;

import java.io.File;

import er.extensions.foundation.ERXFileUtilities;

/**
 * <span class="en">
 * ERPendingAttachment is just a convenience wrapper for tracking 
 * uploaded file state for later passing into an attachment processor.
 * </span>
 * 
 * <span class="ja">
 * ERPendingAttachment はファイル・アップロード・ステータスを調べるための簡単なラッパーです。
 * </span>
 * 
 * @author mschrag
 */
public class ERPendingAttachment {
  private File _uploadedFile;
  private String _recommendedFilePath;
  private String _mimeType;
  private String _configurationName;
  private String _ownerID;
  private int _width;
  private int _height;
  private boolean _pendingDelete;

  /**
   * <span class="en">
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * </span>
   * 
   * <span class="ja">
   * コンストラクター
   * 
   * @param uploadedFile - アップロードする一時的ファイル（最後には削除される）
   * </span>
   */
  public ERPendingAttachment(File uploadedFile) {
    this(uploadedFile, uploadedFile.getName(), null, null, null);
  }

  /**
   * <span class="en">
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * </span>
   * 
   * <span class="ja">
   * コンストラクター
   * 
   * @param uploadedFile - アップロードする一時的ファイル（最後には削除される）
   * @param recommendedFilePath - ユーザによる希望のファイル・パス
   * </span>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath) {
    this(uploadedFile, recommendedFilePath, null, null, null);
  }

  /**
   * <span class="en">
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * </span>
   * 
   * <span class="ja">
   * コンストラクター
   * 
   * @param uploadedFile - アップロードする一時的ファイル（最後には削除される）
   * @param recommendedFilePath - ユーザによる希望のファイル・パス
   * @param mimeType - 使用する mimeType (null = ファイル拡張子で自動認識)
   * </span>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType) {
    this(uploadedFile, recommendedFilePath, mimeType, null, null);
  }

  /**
   * <span class="en">
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs) 
   * @param ownerID an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance) 
   * </span>
   * 
   * <span class="ja">
   * コンストラクター
   * 
   * @param uploadedFile - アップロードする一時的ファイル（最後には削除される）
   * @param recommendedFilePath - ユーザによる希望のファイル・パス
   * @param mimeType - 使用する mimeType (null = ファイル拡張子で自動認識)
   * @param configurationName - 定義設定の名称
   * @param ownerID - サムナイルの「owner」を持つ表記文字列　(例えば、Person.primaryKey) 
   * </span>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType, String configurationName, String ownerID) {
  	this(uploadedFile, recommendedFilePath, mimeType, -1, -1, configurationName, ownerID);
  }

  /**
   * <span class="en">
   * Constructs an ERPendingAttachment.
   * 
   * @param uploadedFile the uploaded temporary file (which will be deleted at the end)
   * @param recommendedFilePath the path recommended by the user during import
   * @param mimeType the mimeType to use (null = guess based on file extension)
   * @param width the desired width of the attachment
   * @param height the desired height of the attachment
   * @param configurationName the name of the configuration settings to use for this processor (see top level docs) 
   * @param ownerID an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance) 
   * </span>
   * 
   * <span class="ja">
   * コンストラクター
   * 
   * @param uploadedFile - アップロードする一時的ファイル（最後には削除される）
   * @param recommendedFilePath - ユーザによる希望のファイル・パス
   * @param mimeType - 使用する mimeType (null = ファイル拡張子で自動認識)
   * @param width - アタッチメントの希望幅
   * @param height - アタッチメントの希望高
   * @param configurationName - 定義設定の名称
   * @param ownerID - サムナイルの「owner」を持つ表記文字列　(例えば、Person.primaryKey) 
   * </span>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType, int width, int height, String configurationName, String ownerID) {
    _uploadedFile = uploadedFile;
    if (recommendedFilePath == null) {
      _recommendedFilePath = _uploadedFile.getName();
    }
    else {
      _recommendedFilePath = recommendedFilePath;
    }
    _width = width;
    _height = height;
    _mimeType = mimeType;
    _configurationName = configurationName;
    _ownerID = ownerID;
    _pendingDelete = true;
  }

  /**
   * <span class="en">
   * Returns the uploaded temporary file (which will be deleted at the end).
   * 
   * @return the uploaded temporary file
   * </span>
   * 
   * <span class="ja">
   * 一時的のアップロード・ファイルを戻します。（最後には削除される）
   * 
   * @return 一時的のアップロード・ファイル
   * </span>
   */
  public File uploadedFile() {
    return _uploadedFile;
  }

  /**
   * <span class="en">
   * Sets the uploaded temporary file (which will be deleted at the end).
   * 
   * @param uploadedFile the uploaded temporary file
   * </span>
   * 
   * <span class="ja">
   * 一時的のアップロード・ファイルをセットします。（最後には削除される）
   * 
   * @param uploadedFile - 一時的のアップロード・ファイル
   * </span>
   */
  public void setUploadedFile(File uploadedFile) {
    _uploadedFile = uploadedFile;
  }

  /**
   * <span class="en">
   * Returns the path recommended by the user during import.
   * 
   * @return the path recommended by the user during import
   * </span>
   * 
   * <span class="ja">
   * ユーザによる希望のファイル・パスを戻します。
   * 
   * @return ユーザによる希望のファイル・パス
   * </span>
   */
  public String recommendedFilePath() {
    return _recommendedFilePath;
  }

  /**
   * <span class="en">
   * Sets the path recommended by the user during import.
   * 
   * @param recommendedFilePath the path recommended by the user during import
   * </span>
   * 
   * <span class="ja">
   * ユーザによる希望のファイル・パスをセットします。
   * 
   * @param recommendedFilePath - ユーザによる希望のファイル・パス
   * </span>
   */
  public void setRecommendedFilePath(String recommendedFilePath) {
    _recommendedFilePath = recommendedFilePath;
  }

  /**
   * <span class="en">
   * Returns the cleansed file name recommended by the user during import.
   * 
   * @return the cleansed file name recommended by the user during import
   * </span>
   * 
   * <span class="ja">
   * ユーザによる希望のファイル名を戻します。
   * 
   * @return ユーザによる希望のファイル名
   * </span>
   */
  public String recommendedFileName() {
    return ERXFileUtilities.fileNameFromBrowserSubmittedPath(_recommendedFilePath);
  }

  /**
   * <span class="en">
   * Returns the mime type (or null if there isn't an explicit one) for this file.
   * 
   * @return the mime type (or null if there isn't an explicit one) for this file
   * </span>
   * 
   * <span class="ja">
   * このファイルの mime type (指定が無い場合には null) を戻します。
   * 
   * @return このファイルの mime type (指定が無い場合には null)
   * </span>
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * <span class="en">
   * Sets the mime type (or null if there isn't an explicit one) for this file.
   * 
   * @param mimeType the mime type (or null if there isn't an explicit one) for this file
   * </span>
   * 
   * <span class="ja">
   * このファイルの mime type (指定が無い場合には null) をセットします。
   * 
   * @param mimeType - このファイルの mime type (指定が無い場合には null)
   * </span>
   */
  public void setMimeType(String mimeType) {
    _mimeType = mimeType;
  }

  /**
   * <span class="en">
   * Returns the name of the configuration settings to use for this upload.
   * 
   * @return the name of the configuration settings to use for this upload
   * </span>
   * 
   * <span class="ja">
   * このアップロードに使用する定義ファイル名を戻します。
   * 
   * @return このアップロードに使用する定義ファイル名
   * </span
   */
  public String configurationName() {
    return _configurationName;
  }

  /**
   * <span class="en">
   * Sets the name of the configuration settings to use for this upload.
   * 
   * @param configurationName the name of the configuration settings to use for this upload
   * </span>
   * 
   * <span class="ja">
   * このアップロードに使用する定義ファイル名をセットします。
   * 
   * @param configurationName - このアップロードに使用する定義ファイル名
   * </span>
   */
  public void setConfigurationName(String configurationName) {
    _configurationName = configurationName;
  }

  /**
   * <span class="en">
   * Returns the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * 
   * @return the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance)
   * </span>
   * 
   * <span class="ja">
   * 「owner」を持つ表記文字列　(例えば、Person.primaryKey) を戻します。
   * 
   * @return 「owner」を持つ表記文字列　(例えば、Person.primaryKey)
   * </span>
   */
  public String ownerID() {
    return _ownerID;
  }

  /**
   * <span class="en">
   * Sets the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * 
   * @param ownerID the arbitrary string that represents the ID of the "owner" of this attachment
   * </span>
   * 
   * <span class="ja">
   * 「owner」を持つ表記文字列　(例えば、Person.primaryKey) をセットします。
   * 
   * @param ownerID - owner」を持つ表記文字列　(例えば、Person.primaryKey)
   * </span>
   */
  public void setOwnerID(String ownerID) {
    _ownerID = ownerID;
  }
  
  /**
   * <span class="en">
   * Sets whether or not this attachment should be deleted after import.
   * 
   * @param pendingDelete whether or not this attachment should be deleted after import
   * </span>
   * 
   * <span class="ja">
   * インポート後にアタッチメントを削除するかどうかをセットします。
   * 
   * @param pendingDelete - インポート後にアタッチメントを削除するかどうか
   * </span>
   */
  public void setPendingDelete(boolean pendingDelete) {
    _pendingDelete = pendingDelete;
  }
  
  /**
   * <span class="en">
   * Returns whether or not this attachment should be deleted after import.
   * 
   * @return whether or not this attachment should be deleted after import
   * </span>
   * 
   * <span class="ja">
   * インポート後にアタッチメントを削除するかどうかを戻します。
   * 
   * @return インポート後にアタッチメントを削除するかどうか
   * </span>
   */
  public boolean isPendingDelete() {
    return _pendingDelete;
  }
  
  /**
   * <span class="en">
   * Sets the desired width of this attachment (or -1 to not specify).
   * 
   * @param width the desired width of this attachment
   * </span>
   * 
   * <span class="ja">
   * アタッチメントの希望幅をセットします。 (又は -1 は未設定)
   * 
   * @param width - アタッチメントの希望幅
   * </span>
   */
  public void setWidth(int width) {
		_width = width;
	}
  
  /**
   * <span class="en">
   * Returns the desired width of this attachment.
   * 
   * @return the desired width of this attachment
   * </span>
   * 
   * <span class="ja">
   * アタッチメントの希望幅を戻します。
   * 
   * @return アタッチメントの希望幅
   * </span>
   */
  public int width() {
		return _width;
	}
  
  /**
   * <span class="en">
   * Sets the desired height of this attachment (or -1 to not specify).
   * 
   * @param height the desired height of this attachment
   * </span>
   * 
   * <span class="ja">
   * アタッチメントの希望高をセットします。 (又は -1 は未設定)
   * 
   * @param height - アタッチメントの希望高
   * </span>
   */
  public void setHeight(int height) {
		_height = height;
	}
  
  /**
   * <span class="en">
   * Returns the desired height of this attachment.
   * 
   * @return the desired height of this attachment
   * </span>
   * 
   * <span class="ja">
   * アタッチメントの希望高を戻します。
   * 
   * @return アタッチメントの希望高
   * </span>
   */
  public int height() {
		return _height;
	}

  @Override
  public String toString() {
    return "[ERPendingAttachment: file = " + _uploadedFile + "]";
  }
}
