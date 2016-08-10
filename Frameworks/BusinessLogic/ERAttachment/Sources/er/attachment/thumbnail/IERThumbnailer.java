package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

public interface IERThumbnailer {

  /**
   * <span class="ja">
   * 指定 mime タイプのサムナイルの作成可能かどうか
   * 
   * @param mimeType - Mime タイプ
   * 
   * @return サムナイルの作成が可能な場合には true が戻ります
   * </span>
   */
  public boolean canThumbnail(ERMimeType mimeType);

  /**
   * <span class="ja">
   * 簡易イメージのサムナイル作成。
   * 他のパラメータはデフォルトで最高の出力で設定されています。
   * 
   * @param resizeWidth - リサイズする最大幅
   * @param resizeHeight - リサイズする最大高
   * @param inputFile - 処理対象ファイル
   * @param outputFile - 結果ファイルの出力先
   * 
   * @throws IOException - イメージ処理中にエラーが発生した場合
   * </span>
   */
	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException;

  /**
   * <span class="ja">
   * 簡易イメージのサムナイル作成。
   * 他のパラメータはデフォルトで最高の出力で設定されています。
   * 
   * @param resizeWidth - リサイズする最大幅
   * @param resizeHeight - リサイズする最大高
   * @param inputFile - 処理対象ファイル
   * @param outputFile - 結果ファイルの出力先
   * @param outputMimeType - 出力 Mime タイプ
   * 
   * @throws IOException - イメージ処理中にエラーが発生した場合
   * </span>
   */
	public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;
}
