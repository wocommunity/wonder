package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;

import er.attachment.utils.ERMimeType;

/**
 * <div class="en">
 * Provides an interface for processing an image, performing various operations on it (most commonly, thumbnailing).
 * </div>
 * 
 * <div class="ja">
 * イメージのサムナイル関連などの画像処理インタフェースを提供します
 * </div>
 *  
 * @author mschrag
 */
public interface IERImageProcessor {
  
  /**
   * <div class="en">
   * Processes the given image with all of the given configuration settings.
   * 
   * Order: resize/dpi, colorspace, crop, sharpen, gamma, watermark
   * </div>
   * 
   * <div class="ja">
   * 指定イメージを指定パラメータで処理します
   * 
   * 順番: リサイズ/dpi, カラー・スペース, 切り取り, シャープ, ガンマ, ウォーターマーク
   * </div>
   * 
   * @param resizeWidth <div class="en">the maximum width to resize to (or -1 to not specify)</div>
   *                    <div class="ja">リサイズする最大幅 (又は -1 は指定なし)</div>
   * @param resizeHeight <div class="en">the maximum height to resize to (or -1 to not specify)</div>
   *                     <div class="ja">リサイズする最大高 (又は -1 は指定なし)</div>
   * @param resizeQuality <div class="en">influences the resize algorithm to use (or null to not specify)</div>
   *                     <div class="ja">リサイズ・アルゴリズム (又は null は指定なし)</div>
   * @param dpi <div class="en">the dpi to resize to (or -1 to skip)</div>
   *            <div class="ja">リサイズする新 dpi (又は -1 はスキップ)</div>
   * @param sharpenRadius <div class="en">the radius of the unsharp mask (or &lt;= 0.0 to skip)</div>
   *                      <div class="ja">アンシャープマスクの半径 (又は &lt;= 0.0 はスキップ)</div>
   * @param sharpenIntensity <div class="en">the intensity of the unsharp mask (or &lt;= 0.0 to skip)</div>
   *                         <div class="ja">アンシャープマスクの強度 (又は &lt;= 0.0 はスキップ)</div>
   * @param gamma <div class="en">the gamma to apply (or 0.0 to skip)</div>
   *              <div class="ja">適用するガンマ (又は 0.0 はスキップ)</div>
   * @param cropX <div class="en">the x position of the crop (or &lt;= 0 to skip)</div>
   *              <div class="ja">x 切り取り位置 (又は &lt;= 0 はスキップ)</div>
   * @param cropY <div class="en">the y position of the crop (or &lt;= 0 to skip)</div>
   *              <div class="ja">y 切り取り位置 (又は &lt;= 0 はスキップ)</div>
   * @param cropWidth <div class="en">the width of the crop (or &lt;= 0 to skip)</div>
   *                  <div class="ja">切り取り幅 (又は &lt;= 0 はスキップ)</div>
   * @param cropHeight <div class="en">the height of the crop (or &lt;= 0 to skip)</div>
   *                   <div class="ja">切り取り高 (又は &lt;= 0 はスキップ)</div>
   * @param watermarkFile <div class="en">the watermark File to apply to this image (or null to skip)</div>
   *                      <div class="ja">ウォーターマーク・ファイル (又は null はスキップ)</div>
   * @param tileWatermark <div class="en">whether or not to tile the watermark</div>
   *                      <div class="ja">ウォーターマークをタイルするかどうか</div>
   * @param compressionQuality <div class="en">the compression quality of the resize to perform (0.0 to ignore) -- range is 0.0 to 1.0 (1.0 = best)</div>
   *                           <div class="ja">リサイズの圧縮品質 (0.0 は無視) -- 範囲は 0.0 〜 1.0 (1.0 = 最高)</div>
   * @param colorProfileFile <div class="en">the ICC profile to use (or null to skip)</div>
   *                         <div class="ja">ICC カラー・プロファイル (又は null はスキップ)</div>
   * @param inputFile <div class="en">the File to perform the given operations on</div>
   *                  <div class="ja">処理対象ファイル</div>
   * @param outputFile <div class="en">the File to write the resulting output image to</div>
   *                   <div class="ja">結果ファイルの出力先</div>
   * @param outputMimeType <div class="en">the desired mime type of the output file</div>
   *                       <div class="ja">出力 Mime タイプ</div>
   * 
   * @throws IOException <div class="en">if the image processing fails</div>
   *                     <div class="ja">イメージ処理中にエラーが発生した場合</div>
   */
  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;

  /**
   * <div class="en">
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * </div>
   * 
   * <div class="ja">
   * 簡易イメージのサムナイル作成。
   * 他のパラメータはデフォルトで最高の出力で設定されています。
   * </div>
   * 
   * @param resizeWidth <div class="en">the maximum resize width</div>
   *                    <div class="ja">リサイズする最大幅</div>
   * @param resizeHeight <div class="en">the maximum resize height.</div>
   *                     <div class="ja">リサイズする最大高</div>
   * @param inputFile <div class="en">the input file to thumbnail</div>
   *                  <div class="ja">処理対象ファイル</div>
   * @param outputFile <div class="en">the output file to write the thumbnail into</div>
   *                   <div class="ja">結果ファイルの出力先</div>
   * 
   * @throws IOException <div class="en">if the thumbnailing fails</div>
   *                     <div class="ja">イメージ処理中にエラーが発生した場合</div>
   */
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile) throws IOException;

  /**
   * <div class="en">
   * Provides a shortcut to thumbnailing an image using some default values that produce
   * decent quality thumbnail outputs.
   * </div>
   * 
   * <div class="ja">
   * 簡易イメージのサムナイル作成。
   * 他のパラメータはデフォルトで最高の出力で設定されています。
   * </div>
   * 
   * @param resizeWidth <div class="en">the maximum resize width</div>
   *                    <div class="ja">リサイズする最大幅</div>
   * @param resizeHeight <div class="en">the maximum resize height.</div>
   *                     <div class="ja">リサイズする最大高</div>
   * @param inputFile <div class="en">the input file to thumbnail</div>
   *                  <div class="ja">処理対象ファイル</div>
   * @param outputFile <div class="en">the output file to write the thumbnail into</div>
   *                   <div class="ja">結果ファイルの出力先</div>
   * @param outputMimeType <div class="en">the output mime type</div>
   *                       <div class="ja">出力 Mime タイプ</div>
   * 
   * @throws IOException <div class="en">if the thumbnailing fails</div>
   *                     <div class="ja">イメージ処理中にエラーが発生した場合</div>
   */
  public void thumbnail(int resizeWidth, int resizeHeight, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException;

  /**
   * <div class="en">
   * Quality is an enumerated type used to specify the resize quality.
   * </div>
   * 
   * <div class="ja">
   * リサイズ品質タイプ
   * </div>
   * 
   * @author mschrag
   */
  public enum Quality {
    Low, Medium, High
  }

}