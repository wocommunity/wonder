package er.attachment.model;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * <span class="en">
 * ERFileAttachment (type = "file") represents an attachment whose
 * data is stored on the local filesystem.  An ERFileAttachment can
 * either be proxied or not.  If the attachment is not proxied, then
 * the backing file must be located in a folder that is reachable 
 * by your front-end webserver.  If the file is proxied, then the
 * data will be served via a custom request handler, and thus can
 * be written anywhere on the filesystem.
 * </span>
 * 
 * <span class="ja">
 * ERFileAttachment (type "file") はアタッチメントがローカル・ファイルシステムに保存されます。
 * ERFileAttachment はプロクシー化することも可能です。アタッチメントがプロクシー化されない場合、
 * ファイルは Webserver がアクセス可能なフォルダ内に保存されないといけません。アタッチメントが
 * プロクシー化されるとデータはカスタム・レクエスト・ハンドラー経由でアクセスされ、ファイルは
 * ファイルシステム上のどこでも保存が可能です。
 * </span>
 * 
 * @author mschrag
 */
public class ERFileAttachment extends _ERFileAttachment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  public static final String STORAGE_TYPE = "file";

  @Override
  public void awakeFromInsertion(EOEditingContext editingContext) {
    super.awakeFromInsertion(editingContext);
    setStorageType(ERFileAttachment.STORAGE_TYPE);
  }
  
  /**
   * <span class="en">
   * If the FilePath is set it will be used on Top of the Result of the filesystemPath
   * This conversion makes it easy to switch Data easily between Deploy and Develop
   * </span>
   * 
   * <span class="ja">
   * データベース内にファイルストアへのパスを設定しないとファイルストアの移動が簡単になります。
   * </span>
   * 
   * @property er.attachment.file.filebasePath - FilePath
   */
  @Override
  public String filesystemPath() {
    String filebasePath = ERXProperties.stringForKey("er.attachment.file.filebasePath");
    String result = super.filesystemPath();
    if(!ERXStringUtilities.stringIsNullOrEmpty(filebasePath)) {
      result = filebasePath + result;
    }
    return result;
  }

  /**
   * <span class="en">
   * If the FilePath is set it will be used on Top of the Result of the filesystemPath
   * This conversion makes it easy to switch Data easily between Deploy and Develop
   * </span>
   * 
   * <span class="ja">
   * データベース内にファイルストアへのパスを設定しないとファイルストアの移動が簡単になります。
   * </span>
   * 
   * @property er.attachment.file.filebasePath - FilePath
   */
  @Override
  public void setFilesystemPath(String value) {
    String filebasePath = ERXProperties.stringForKey("er.attachment.file.filebasePath");
    if(!ERXStringUtilities.stringIsNullOrEmpty(filebasePath)) {
      value = value.replace(filebasePath, ""); // 
    }

    super.setFilesystemPath(value);
  }

}
