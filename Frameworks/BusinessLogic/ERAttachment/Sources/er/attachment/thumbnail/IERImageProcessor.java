package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

/**
 * <span class="en">
 * Provides an interface for processing an image, performing various operations on it (most commonly, thumbnailing).
 * </span>
 * 
 * <span class="ja">
 * イメージのサムナイル関連などの画像処理インタフェースを提供します
 * </span>
 *  
 * @author mschrag
 */
public interface IERImageProcessor {
  
  /**
   * <span class="en">
   * Processes the given image with all of the given configuration settings.
   * 
   * Order: resize/dpi, colorspace, crop, sharpen, gamma, watermark
   * 
   * @param resizeWidth the maximum width to resize to (or -1 to not specify)
   * @param resizeHeight the maximum height to resize to (or -1 to not specify)
   * @param resizeQuality influences the resize algorithm to use (or null to not specify) 
   * @param dpi the dpi to resize to (or -1 to skip)
   * @param sharpenRadius the radius of the unsharp mask (or <= 0.0 to skip)
   * @param sharpenIntensity the intensity of the unsharp mask (or <= 0.0 to skip)
   * @param gamma the gamma to apply (or 0.0 to skip)
   * @param cropX the x position of the crop (or <= 0 to skip)
   * @param cropY the y position of the crop (or <= 0 to skip)
   * @param cropWidth the width of the crop (or <= 0 to skip) 
   * @param cropHeight the height of the crop (or <= 0 to skip)
   * @param watermarkFile the watermark File to apply to this image (or null to skip)
   * @param tileWatermark whether or not to tile the watermark
   * @param compressionQuality the compression quality of the resize to perform (0.0 to ignore) -- range is 0.0 to 1.0 (1.0 = best)
   * @param colorProfileFile the ICC profile to use (or null to skip)
   * @param inputFile the File to perform the given operations on
   * @param outputFile the File to write the resulting output image to
   * @param outputMimeType the desired mime type of the output file
   * 
   * @throws IOException if the image processing fails
   * </span>
   * 
   * <span class="ja">
   * 指定イメージを指定パラメータで処理します
   * 
   * 順番: リサイズ/dpi, カラー・スペース, 切り取り, シャープ, ガンマ, ウォーターマーク
   * 
   * @param resizeWidth - リサイズする最大幅 (又は -1 は指定なし)
   * @param resizeHeight - リサイズする最大高 (又は -1 は指定なし)
   * @param resizeQuality - リサイズ・アルゴリズム (又は null は指定なし) 
   * @param dpi - リサイズする新 dpi (又は -1 はスキップ)
   * @param sharpenRadius - アンシャープマスクの半径 (又は <= 0.0 はスキップ)
   * @param sharpenIntensity - アンシャープマスクの強度 (又は <= 0.0 はスキップ)
   * @param gamma - 適用するガンマ (又は 0.0 はスキップ)
   * @param cropX - x 切り取り位置 (又は <= 0 はスキップ)
   * @param cropY - y 切り取り位置 (又は <= 0 はスキップ)
   * @param cropWidth - 切り取り幅 (又は <= 0 はスキップ)
   * @param cropHeight - 切り取り高 (又は <= 0 はスキップ)
   * @param watermarkFile - ウォーターマーク・ファイル (又は null はスキップ)
   * @param tileWatermark - ウォーターマークをタイルするかどうか
   * @param compressionQuality - リサイズの圧縮品質 (0.0 は無視) -- 範囲は 0.0 〜 1.0 (1.0 = 最高)
   * @param colorProfileFile - ICC カラー・プロファイル (又は null はスキップ)
   * @param inputFile - 処理対象ファイル
   * @param outputFile - 結果ファイルの出力先
   * @param outputMimeType - 出力 Mime タイプ
   * 
   * @throws IOException - イメージ処理中にエラーが発生した場合
   * </span>
   */
  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;

  /**
   * <span class="en">
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * 
   * @param resizeWidth the maximum resize width
   * @param resizeHeight the maximum resize height
   * @param inputFile the input file to thumbnail
   * @param outputFile the output file to write the thumbnail into
   * 
   * @throws IOException if the thumbnailing fails
   * </span>
   * 
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
   * <span class="en">
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * 
   * @param resizeWidth the maximum resize width
   * @param resizeHeight the maximum resize height
   * @param inputFile the input file to thumbnail
   * @param outputFile the output file to write the thumbnail into
   * @param outputMimeType the output mime type
   * 
   * @throws IOException if the thumbnailing fails
   * </span>
   * 
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

  /**
   * <span class="en">
   * Quality is an enumerated type used to specify the resize quality.
   * </span>
   * 
   * <span class="ja">
   * リサイズ品質タイプ
   * </span>
   * 
   * @author mschrag
   */
  public enum Quality {
    Low, Medium, High
  }

}