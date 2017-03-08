package er.attachment.thumbnail;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import er.attachment.thumbnail.BlendComposite.BlendingMode;
import er.attachment.utils.ERMimeType;

/**
 * Everyone's got a fallback school, and Java2DImageProcessor is that 
 * for image processing. It's slow, it's memory intensive, and it
 * works everywhere (well .. except that it's not fully implemented,
 * but it supports THUMBNAILING everywhere).
 * 
 * @author mschrag
 */
public class Java2DImageProcessor extends ERImageProcessor {
  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
    BufferedImage image = ImageIO.read(inputFile);

    if (resizeWidth == -1 && resizeHeight == -1) {
      // just use the buffer we have
    }
    else {
      int width;
      int height;
      int originalHeight = image.getHeight();
      int originalWidth = image.getWidth();
      if (resizeWidth != -1 && resizeHeight != -1) {
        if (originalWidth > originalHeight) {
          width = resizeWidth;
          height = (int) (((float) resizeWidth / originalWidth) * originalHeight);
        }
        else {
          width = (int) (((float) resizeHeight / originalHeight) * originalWidth);
          height = resizeHeight;
        }
      }
      else if (resizeHeight != -1) {
        width = (int) (((float) resizeHeight / originalHeight) * originalWidth);
        height = resizeHeight;
      }
      else {
        width = resizeWidth;
        height = (int) (((float) resizeWidth / originalWidth) * originalHeight);
      }
      BufferedImage workingImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D gScaledImg = workingImage.createGraphics();
      if (resizeQuality == Quality.High) {
        gScaledImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      }
      else if (resizeQuality == null || resizeQuality == Quality.Medium) {
        gScaledImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      }
      else if (resizeQuality == Quality.Low) {
        gScaledImg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      }
      gScaledImg.drawImage(image, 0, 0, width, height, null);
      gScaledImg.dispose();
      image = workingImage;
    }

    if (dpi > 0.0) {
      log.info("Java2DProcessor does not yet support changing the image DPI.");
    }

    if (sharpenRadius > 0.0f || sharpenIntensity > 0.0f) {
      log.info("Java2DProcessor does not yet support sharpening.");
    }

    if (gamma > 0.0f) {
      log.info("Java2DProcessor does not yet support changing the image gamma.");
    }

    if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
      throw new IOException("Failed to create the folder '" + outputFile.getParentFile() + "'.");
    }

    if (cropWidth > 0 || cropHeight > 0) {
      image = image.getSubimage(cropX, cropY, cropWidth, cropHeight);
    }

    if (colorProfileFile != null) {
      log.info("Java2DProcessor does not yet support changing the image color profile.");
    }

    if (watermarkFile != null) {
      BufferedImage watermarkImage = ImageIO.read(watermarkFile);
      Graphics2D graphics = image.createGraphics();
      int width = image.getWidth();
      int height = image.getHeight();
      int watermarkWidth = watermarkImage.getWidth();
      int watermarkHeight = watermarkImage.getHeight();
      graphics.setComposite(BlendComposite.getInstance(BlendingMode.SCREEN));
      if (tileWatermark) {
        for (int x = 0; x < width; x += watermarkWidth) {
          for (int y = 0; y < height; y += watermarkHeight) {
            graphics.drawImage(watermarkImage, x, y, null);
          }
        }
      }
      else {
        int watermarkX = (width - watermarkWidth) / 2;
        int watermarkY = (height - watermarkHeight) / 2;
        graphics.drawImage(watermarkImage, watermarkX, watermarkY, null);
      }
      graphics.dispose();
    }

    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile))) {
      String outputMimeTypeStr = outputMimeType.mimeType();
      String outputType = outputMimeTypeStr.substring(outputMimeTypeStr.indexOf('/') + 1);
      ImageIO.write(image, outputType, bos);
    }

    if (!outputFile.exists()) {
      throw new IOException("Failed to process image '" + inputFile + "' into '" + outputFile + "'.");
    }
  }
}
