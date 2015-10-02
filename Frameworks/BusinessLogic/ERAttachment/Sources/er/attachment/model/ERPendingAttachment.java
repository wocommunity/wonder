package er.attachment.model;

import java.io.File;

import er.extensions.foundation.ERXFileUtilities;

/**
 * <div class="en">
 * ERPendingAttachment is just a convenience wrapper for tracking 
 * uploaded file state for later passing into an attachment processor.
 * </div>
 * 
 * <div class="ja">
 * ERPendingAttachment はファイル・アップロード・ステータスを調べるための簡単なラッパーです。
 * </div>
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
   * <div class="en">
   * Constructs an ERPendingAttachment.
   * </div>
   * 
   * <div class="ja">
   * コンストラクター
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file (which will be deleted at the end)</div>
   *                     <div class="ja">アップロードする一時的ファイル（最後には削除される）</div>
   */
  public ERPendingAttachment(File uploadedFile) {
    this(uploadedFile, uploadedFile.getName(), null, null, null);
  }

  /**
   * <div class="en">
   * Constructs an ERPendingAttachment.
   * </div>
   * 
   * <div class="ja">
   * コンストラクター
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file (which will be deleted at the end)</div>
   *                     <div class="ja">アップロードする一時的ファイル（最後には削除される）</div>
   * @param recommendedFilePath <div class="en">the path recommended by the user during import</div>
   *                            <div class="ja">ユーザによる希望のファイル・パス</div>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath) {
    this(uploadedFile, recommendedFilePath, null, null, null);
  }

  /**
   * <div class="en">
   * Constructs an ERPendingAttachment.
   * </div>
   * 
   * <div class="ja">
   * コンストラクター
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file (which will be deleted at the end)</div>
   *                     <div class="ja">アップロードする一時的ファイル（最後には削除される）</div>
   * @param recommendedFilePath <div class="en">the path recommended by the user during import</div>
   *                            <div class="ja">ユーザによる希望のファイル・パス</div>
   * @param mimeType <div class="en">the mimeType to use (null = guess based on file extension)</div>
   *                 <div class="ja">使用する mimeType (null = ファイル拡張子で自動認識)</div>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType) {
    this(uploadedFile, recommendedFilePath, mimeType, null, null);
  }

  /**
   * <div class="en">
   * Constructs an ERPendingAttachment.
   * </div>
   * 
   * <div class="ja">
   * コンストラクター
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file (which will be deleted at the end)</div>
   *                     <div class="ja">アップロードする一時的ファイル（最後には削除される）</div>
   * @param recommendedFilePath <div class="en">the path recommended by the user during import</div>
   *                            <div class="ja">ユーザによる希望のファイル・パス</div>
   * @param mimeType <div class="en">the mimeType to use (null = guess based on file extension)</div>
   *                 <div class="ja">使用する mimeType (null = ファイル拡張子で自動認識)</div>
   * @param configurationName <div class="en">the name of the configuration settings to use for this processor (see top level docs)</div>
   *                          <div class="ja">定義設定の名称</div>
   * @param ownerID <div class="en">an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance)</div>
   *                <div class="ja">サムナイルの「owner」を持つ表記文字列　(例えば、Person.primaryKey)</div>
   */
  public ERPendingAttachment(File uploadedFile, String recommendedFilePath, String mimeType, String configurationName, String ownerID) {
  	this(uploadedFile, recommendedFilePath, mimeType, -1, -1, configurationName, ownerID);
  }

  /**
   * <div class="en">
   * Constructs an ERPendingAttachment.
   * </div>
   * 
   * <div class="ja">
   * コンストラクター
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file (which will be deleted at the end)</div>
   *                     <div class="ja">アップロードする一時的ファイル（最後には削除される）</div>
   * @param recommendedFilePath <div class="en">the path recommended by the user during import</div>
   *                            <div class="ja">ユーザによる希望のファイル・パス</div>
   * @param mimeType <div class="en">the mimeType to use (null = guess based on file extension)</div>
   *                 <div class="ja">使用する mimeType (null = ファイル拡張子で自動認識)</div>
   * @param width <div class="en">the desired width of the attachment</div>
   *              <div class="ja">アタッチメントの希望幅</div>
   * @param height <div class="en">the desired height of the attachment</div>
   *               <div class="ja">アタッチメントの希望高</div>
   * @param configurationName <div class="en">the name of the configuration settings to use for this processor (see top level docs)</div>
   *                          <div class="ja">定義設定の名称</div>
   * @param ownerID <div class="en">an arbitrary string that represents the ID of the "owner" of this thumbnail (Person.primaryKey, for instance)</div>
   *                <div class="ja">サムナイルの「owner」を持つ表記文字列　(例えば、Person.primaryKey)</div>
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
   * <div class="en">
   * Returns the uploaded temporary file (which will be deleted at the end).
   * </div>
   * 
   * <div class="ja">
   * 一時的のアップロード・ファイルを戻します。（最後には削除される）
   * </div>
   * 
   * @return <div class="en">the uploaded temporary file</div>
   *         <div class="ja">一時的のアップロード・ファイル</div>
   */
  public File uploadedFile() {
    return _uploadedFile;
  }

  /**
   * <div class="en">
   * Sets the uploaded temporary file (which will be deleted at the end).
   * </div>
   * 
   * <div class="ja">
   * 一時的のアップロード・ファイルをセットします。（最後には削除される）
   * </div>
   * 
   * @param uploadedFile <div class="en">the uploaded temporary file</div>
   *                     <div class="ja">一時的のアップロード・ファイル</div>
   */
  public void setUploadedFile(File uploadedFile) {
    _uploadedFile = uploadedFile;
  }

  /**
   * <div class="en">
   * Returns the path recommended by the user during import.
   * </div>
   * 
   * <div class="ja">
   * ユーザによる希望のファイル・パスを戻します。
   * </div>
   * 
   * @return <div class="en">the path recommended by the user during import</div>
   *         <div class="ja">ユーザによる希望のファイル・パス</div>
   */
  public String recommendedFilePath() {
    return _recommendedFilePath;
  }

  /**
   * <div class="en">
   * Sets the path recommended by the user during import.
   * </div>
   * 
   * <div class="ja">
   * ユーザによる希望のファイル・パスをセットします。
   * </div>
   * 
   * @param recommendedFilePath <div class="en">the path recommended by the user during import</div>
   *                            <div class="ja">ユーザによる希望のファイル・パス</div>
   */
  public void setRecommendedFilePath(String recommendedFilePath) {
    _recommendedFilePath = recommendedFilePath;
  }

  /**
   * <div class="en">
   * Returns the cleansed file name recommended by the user during import.
   * </div>
   * 
   * <div class="ja">
   * ユーザによる希望のファイル名を戻します。
   * </div>
   * 
   * @return <div class="en">the cleansed file name recommended by the user during import</div>
   *         <div class="ja">ユーザによる希望のファイル名</div>
   */
  public String recommendedFileName() {
    return ERXFileUtilities.fileNameFromBrowserSubmittedPath(_recommendedFilePath);
  }

  /**
   * <div class="en">
   * Returns the mime type (or null if there isn't an explicit one) for this file.
   * </div>
   * 
   * <div class="ja">
   * このファイルの mime type (指定が無い場合には null) を戻します。
   * </div>
   * 
   * @return <div class="en">the mime type (or null if there isn't an explicit one) for this file</div>
   *         <div class="ja">このファイルの mime type (指定が無い場合には null)</div>
   */
  public String mimeType() {
    return _mimeType;
  }

  /**
   * <div class="en">
   * Sets the mime type (or null if there isn't an explicit one) for this file.
   * </div>
   * 
   * <div class="ja">
   * このファイルの mime type (指定が無い場合には null) をセットします。
   * </div>
   * 
   * @param mimeType <div class="en">the mime type (or null if there isn't an explicit one) for this file</div>
   *                 <div class="ja">このファイルの mime type (指定が無い場合には null)</div>
   */
  public void setMimeType(String mimeType) {
    _mimeType = mimeType;
  }

  /**
   * <div class="en">
   * Returns the name of the configuration settings to use for this upload.
   * </div>
   * 
   * <div class="ja">
   * このアップロードに使用する定義ファイル名を戻します。
   * </div>
   * 
   * @return <div class="en">the name of the configuration settings to use for this upload</div>
   *         <div class="ja">このアップロードに使用する定義ファイル名</div>
   */
  public String configurationName() {
    return _configurationName;
  }

  /**
   * <div class="en">
   * Sets the name of the configuration settings to use for this upload.
   * </div>
   * 
   * <div class="ja">
   * このアップロードに使用する定義ファイル名をセットします。
   * </div>
   * 
   * @param configurationName <div class="en">the name of the configuration settings to use for this upload</div>
   *                          <div class="ja">このアップロードに使用する定義ファイル名</div>
   */
  public void setConfigurationName(String configurationName) {
    _configurationName = configurationName;
  }

  /**
   * <div class="en">
   * Returns the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * </div>
   * 
   * <div class="ja">
   * 「owner」を持つ表記文字列　(例えば、Person.primaryKey) を戻します。
   * </div>
   * 
   * @return <div class="en">the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance)</div>
   *         <div class="ja">「owner」を持つ表記文字列　(例えば、Person.primaryKey)</div>
   */
  public String ownerID() {
    return _ownerID;
  }

  /**
   * <div class="en">
   * Sets the arbitrary string that represents the ID of the "owner" of this attachment (Person.primaryKey, for instance).
   * </div>
   * 
   * <div class="ja">
   * 「owner」を持つ表記文字列　(例えば、Person.primaryKey) をセットします。
   * </div>
   * 
   * @param ownerID <div class="en">the arbitrary string that represents the ID of the "owner" of this attachment</div>
   *                <div class="ja">「owner」を持つ表記文字列　(例えば、Person.primaryKey)</div>
   */
  public void setOwnerID(String ownerID) {
    _ownerID = ownerID;
  }
  
  /**
   * <div class="en">
   * Sets whether or not this attachment should be deleted after import.
   * </div>
   * 
   * <div class="ja">
   * インポート後にアタッチメントを削除するかどうかをセットします。
   * </div>
   * 
   * @param pendingDelete <div class="en">whether or not this attachment should be deleted after import</div>
   *                      <div class="ja">インポート後にアタッチメントを削除するかどうか</div>
   */
  public void setPendingDelete(boolean pendingDelete) {
    _pendingDelete = pendingDelete;
  }
  
  /**
   * <div class="en">
   * Returns whether or not this attachment should be deleted after import.
   * </div>
   * 
   * <div class="ja">
   * インポート後にアタッチメントを削除するかどうかを戻します。
   * </div>
   * 
   * @return <div class="en">whether or not this attachment should be deleted after import</div>
   *         <div class="ja">インポート後にアタッチメントを削除するかどうか</div>
   */
  public boolean isPendingDelete() {
    return _pendingDelete;
  }
  
  /**
   * <div class="en">
   * Sets the desired width of this attachment (or -1 to not specify).
   * </div>
   * 
   * <div class="ja">
   * アタッチメントの希望幅をセットします。 (又は -1 は未設定)
   * </div>
   * 
   * @param width <div class="en">the desired width of this attachment</div>
   *              <div class="ja">アタッチメントの希望幅</div>
   */
  public void setWidth(int width) {
		_width = width;
	}
  
  /**
   * <div class="en">
   * Returns the desired width of this attachment.
   * </div>
   * 
   * <div class="ja">
   * アタッチメントの希望幅を戻します。
   * </div>
   * 
   * @return <div class="en">the desired width of this attachment</div>
   *         <div class="ja">アタッチメントの希望幅</div>
   */
  public int width() {
		return _width;
	}
  
  /**
   * <div class="en">
   * Sets the desired height of this attachment (or -1 to not specify).
   * </div>
   * 
   * <div class="ja">
   * アタッチメントの希望高をセットします。 (又は -1 は未設定)
   * </div>
   * 
   * @param height <div class="en">the desired height of this attachment</div>
   *               <div class="ja">アタッチメントの希望高</div>
   */
  public void setHeight(int height) {
		_height = height;
	}
  
  /**
   * <div class="en">
   * Returns the desired height of this attachment.
   * </div>
   * 
   * <div class="ja">
   * アタッチメントの希望高を戻します。
   * </div>
   * 
   * @return <div class="en">the desired height of this attachment</div>
   *         <div class="ja">アタッチメントの希望高</div>
   */
  public int height() {
		return _height;
	}

  @Override
  public String toString() {
    return "[ERPendingAttachment: file = " + _uploadedFile + "]";
  }
}
