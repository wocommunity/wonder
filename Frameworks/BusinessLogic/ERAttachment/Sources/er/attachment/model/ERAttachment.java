package er.attachment.model;

import java.io.File;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;

import er.attachment.processors.ERAttachmentProcessor;
import er.attachment.utils.ERMimeType;
import er.attachment.utils.ERMimeTypeManager;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.qualifiers.ERXKeyValueQualifier;

/**
 * <span class="en">
 * ERAttachment is the superclass of all attachment types.  An attachment object
 * encapsulates a small amount of metadata and the information necessary to
 * construct a url or a stream onto the attachment data.
 * </span>
 * 
 * <span class="ja">
 * 全アタッチメント・タイプのスーパークラスである。
 * アタッチメント・オブジェクトはメタデータの一部を保存し、さらに URL 生成やストリーム作成の
 * 情報も含みます。
 * </span>
 * 
 * @author mschrag
 */
public abstract class ERAttachment extends _ERAttachment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ERAttachment.class);

  private boolean isInNestedEditingContext(EOEditingContext attachmentEc) {
	  return (attachmentEc.parentObjectStore() instanceof EOEditingContext); 
  }
  
  private boolean isInNestedEditingContext() {
	  return isInNestedEditingContext(editingContext());
  }

  @Override
  public void didInsert() {
    super.didInsert();
    if (!isInNestedEditingContext()) {
    	ERAttachmentProcessor.processorForType(this).attachmentInserted(this);
    }
  }

  /**
   * <span class="en">
   * Returns the file name portion of the webPath.
   * 
   * @return the file name portion of the webPath
   * </span>
   * 
   * <span class="ja">
   * webPath よりのファイル名部分を戻します。
   * 
   * @return webPath よりのファイル名部分
   * </span>
   */
  public String fileName() {
    return new File(webPath()).getName();
  }
  
  /**
   * <span class="en">
   * Returns the ERMimeType that corresponds to the mimeType.
   * 
   * @return the ERMimeType that corresponds to the mimeType
   * </span>
   * 
   * <span class="ja">
   * mimeType に対応する ERMimeType を戻します。
   * 
   * @return mimeType に対応する ERMimeType
   * </span>
   */
  public ERMimeType erMimeType() {
    return ERMimeTypeManager.mimeTypeManager().mimeTypeForMimeTypeString(mimeType(), false);
  }
  
  /**
   * <span class="en">
   * Returns the file extension of this attachment, first checking the mime type, 
   * then returning the actual extension.
   *  
   * @return the file extension of this attachment
   * </span>
   * 
   * <span class="ja">
   * アタッチメントの拡張子を戻します。
   * 最初あ MIME タイプをチェックされ、その後では本当の拡張子をチェックします。
   *  
   * @return アタッチメントの拡張子
   * </span>
   */
  public String extension() {
    String ext;
    ERMimeType mimeType = erMimeType();
    if (mimeType == null) {
      ext = ERXFileUtilities.fileExtension(originalFileName()).toLowerCase();
    }
    else {
      ext = mimeType.primaryExtension();
    }
    return ext;
  }
  
  /**
   * <span class="en">
   * Fetches the required attachment associated with the given web path.
   * 
   * @param editingContext the editing context to load in
   * @param webPath the web path of the attachment
   * 
   * @return the attachment
   * 
   * @throws NoSuchElementException if there is no attachment with the given web path
   * </span>
   * 
   * <span class="ja">
   * 指定 web パスと関連されているアタッチメントをフェッチします。
   * 
   * @param editingContext - ロードする編集コンテキスト
   * @param webPath - アタッチメントの web パス
   * 
   * @return アタッチメント
   * 
   * @throws NoSuchElementException - 指定 web パスのアタッチメントが無い場合
   * </span>
   */
  public static ERAttachment fetchRequiredAttachmentWithWebPath(EOEditingContext editingContext, String webPath) {
    ERAttachment attachment = ERAttachment.fetchRequiredERAttachment(editingContext, thatAreForWebPath(webPath));
    return attachment;
  }

	public static EOQualifier thatAreForWebPath(String webPath) {
		final ERXKeyValueQualifier qualifier = ERAttachment.WEB_PATH.is(webPath);
		return qualifier;
	}
  
  @Override
  public void didDelete(EOEditingContext ec) {
    super.didDelete(ec);
    if (!isInNestedEditingContext(ec)) {
    	try {
    		ERAttachmentProcessor.processorForType(this).deleteAttachment(this);
    	}
    	catch (Throwable e) {
    		log.error("Failed to delete attachment '{}'.", primaryKey(), e);
    	}
    }
  }
}
