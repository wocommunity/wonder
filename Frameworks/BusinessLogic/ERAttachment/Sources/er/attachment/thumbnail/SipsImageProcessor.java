package er.attachment.thumbnail;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import er.attachment.utils.ERMimeType;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXRuntimeUtilities.Result;

public class SipsImageProcessor extends ERImageProcessor {

  public void processImage(int resizeWidth, int resizeHeight, Quality resizeQuality, int dpi, float sharpenRadius, float sharpenIntensity, float gamma, int cropX, int cropY, int cropWidth, int cropHeight, File watermarkFile, boolean tileWatermark, float compressionQuality, File colorProfileFile, File inputFile, File outputFile, ERMimeType outputMimeType) throws IOException {
    try {
      if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
        throw new IOException("Failed to create the folder '" + outputFile.getParentFile() + "'.");
      }

      List<String> commands = new LinkedList<>();
      commands.add("/usr/bin/sips");
      commands.add("--resampleHeightWidthMax");
      commands.add(String.valueOf(Math.max(resizeWidth, resizeHeight)));
      
      if (resizeQuality != null) {
        if (resizeQuality == Quality.High) {
          commands.add("--setProperty");
          commands.add("quality");
          commands.add("best");
        }
        else if (resizeQuality == Quality.Medium) {
          commands.add("--setProperty");
          commands.add("quality");
          commands.add("normal");
        }
        else if (resizeQuality == Quality.Low) {
          commands.add("--setProperty");
          commands.add("quality");
          commands.add("draft");
        }
      }
      
      if (outputMimeType != null) {
        commands.add("--setProperty");
        commands.add("format");
        commands.add(outputMimeType.subtype());
      }
      
      if (compressionQuality != -1) {
        commands.add("--setProperty");
        commands.add("formatOptions");
        commands.add(String.format("%.0f", compressionQuality * 100));
      }
      
      if (dpi > 0.0) {
        commands.add("--setProperty");
        commands.add("dpiWidth");
        commands.add(String.valueOf(dpi));
        commands.add("--setProperty");
        commands.add("dpiHeight");
        commands.add(String.valueOf(dpi));
      }

      if (sharpenRadius > 0.0f || sharpenIntensity > 0.0f) {
        log.info("SipsImageProcessor does not yet support sharpening.");
      }

      if (gamma > 0.0f) {
        log.info("SipsImageProcessor does not yet support changing the image gamma.");
      }

      if (cropX > 0.0f || cropY > 0.0f) {
        log.info("SipsImageProcessor does not yet support cropping offsets.");
      }
      
      if (cropWidth > 0.0f && cropHeight > 0.0f) {
        commands.add("--cropToHeightWidth");
        commands.add(String.valueOf(cropHeight));
        commands.add(String.valueOf(cropWidth));
      }

      if (watermarkFile != null) {
        log.info("SipsImageProcessor does not yet support watermarking.");
      }
      
      if (colorProfileFile != null) {
        commands.add("-m");
        commands.add(colorProfileFile.getCanonicalPath());
      }
      
      commands.add(inputFile.getCanonicalPath());
      
      commands.add("--out");
      commands.add(outputFile.getCanonicalPath());

      Result result = ERXRuntimeUtilities.execute(commands.toArray(new String[commands.size()]), null, null, 30000);
      if (result.getExitValue() != 0) {
        throw new RuntimeException("Sips exited with the exit code #" + result.getExitValue());
      }
    }
    catch (Throwable t) {
      throw new IOException("Failed to thumbnail image: " + t.getMessage() + " (also, Java 1.5 is stupid!)");
    }
  }
}
