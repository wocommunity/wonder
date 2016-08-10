package er.attachment.metadata;

import java.util.HashMap;
import java.util.Map;

import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class ERMetadataUtils {
  private static Map<String, Integer> IPTC_NAME_TO_TYPE = new HashMap<String, Integer>();
  private static Map<String, Integer> EXIF_NAME_TO_TYPE = new HashMap<String, Integer>();
  static {
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Caption/Abstract", Integer.valueOf(IptcDirectory.TAG_CAPTION));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("City", Integer.valueOf(IptcDirectory.TAG_CITY));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("CopyrightNotice", Integer.valueOf(IptcDirectory.TAG_COPYRIGHT_NOTICE));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Country/PrimaryLocationName",
				Integer.valueOf(IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_NAME));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Credit", Integer.valueOf(IptcDirectory.TAG_CREDIT));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("DateCreated", Integer.valueOf(IptcDirectory.TAG_DATE_CREATED));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("TimeCreated", Integer.valueOf(IptcDirectory.TAG_TIME_CREATED));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Province/State", Integer.valueOf(IptcDirectory.TAG_PROVINCE_OR_STATE));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Source", Integer.valueOf(IptcDirectory.TAG_SOURCE));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Category", Integer.valueOf(IptcDirectory.TAG_CATEGORY));
		ERMetadataUtils.IPTC_NAME_TO_TYPE.put("ObjectName", Integer.valueOf(IptcDirectory.TAG_OBJECT_NAME));

		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureTime", Integer.valueOf(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Flash", Integer.valueOf(ExifSubIFDDirectory.TAG_FLASH));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ColorSpace", Integer.valueOf(ExifSubIFDDirectory.TAG_COLOR_SPACE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SceneCaptureType", Integer.valueOf(ExifSubIFDDirectory.TAG_SCENE_TYPE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneYResolution",
				Integer.valueOf(ExifSubIFDDirectory.TAG_FOCAL_PLANE_Y_RESOLUTION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneResolutionUnit",
				Integer.valueOf(ExifSubIFDDirectory.TAG_FOCAL_PLANE_RESOLUTION_UNIT));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalLength", Integer.valueOf(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ShutterSpeedValue", Integer.valueOf(ExifSubIFDDirectory.TAG_SHUTTER_SPEED));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FNumber", Integer.valueOf(ExifSubIFDDirectory.TAG_FNUMBER));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PixelYDimension", Integer.valueOf(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ApertureValue", Integer.valueOf(ExifSubIFDDirectory.TAG_APERTURE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PixelXDimension", Integer.valueOf(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("CustomRendered", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("WhiteBalance", Integer.valueOf(ExifSubIFDDirectory.TAG_WHITE_BALANCE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTimeDigitized",
				Integer.valueOf(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureProgram", Integer.valueOf(ExifSubIFDDirectory.TAG_EXPOSURE_PROGRAM));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTimeOriginal", Integer.valueOf(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("MeteringMode", Integer.valueOf(ExifSubIFDDirectory.TAG_METERING_MODE));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureMode", Integer.valueOf(ExifIFD0Directory.TAG_EXPO));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureBiasValue", Integer.valueOf(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneXResolution",
				Integer.valueOf(ExifSubIFDDirectory.TAG_FOCAL_PLANE_X_RESOLUTION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Orientation", Integer.valueOf(ExifIFD0Directory.TAG_ORIENTATION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Model", Integer.valueOf(ExifIFD0Directory.TAG_MODEL));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("YResolution", Integer.valueOf(ExifIFD0Directory.TAG_Y_RESOLUTION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("XResolution", Integer.valueOf(ExifIFD0Directory.TAG_X_RESOLUTION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTime", Integer.valueOf(ExifIFD0Directory.TAG_DATETIME));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Make", Integer.valueOf(ExifIFD0Directory.TAG_MAKE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ResolutionUnit", Integer.valueOf(ExifIFD0Directory.TAG_RESOLUTION_UNIT));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Copyright", Integer.valueOf(ExifIFD0Directory.TAG_COPYRIGHT));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Artist", Integer.valueOf(ExifIFD0Directory.TAG_ARTIST));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Software", Integer.valueOf(ExifIFD0Directory.TAG_SOFTWARE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ImageDescription", Integer.valueOf(ExifIFD0Directory.TAG_IMAGE_DESCRIPTION));

		//MetadataUtils.EXIF_NAME_TO_TYPE.put("FocalLenIn35mmFilm", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("UserComment", Integer.valueOf(ExifSubIFDDirectory.TAG_USER_COMMENT));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("GainControl", Integer.valueOf(ExifIFD0Directory.TAG_));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("DigitalZoomRatio", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("MaxApertureValue", Integer.valueOf(ExifSubIFDDirectory.TAG_MAX_APERTURE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("LightSource", Integer.valueOf(ExifSubIFDDirectory.TAG_WHITE_BALANCE));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("CompressedBitsPerPixel", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SensingMethod", Integer.valueOf(ExifSubIFDDirectory.TAG_SENSING_METHOD));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("Sharpness", Integer.valueOf(ExifIFD0Directory.TAG_));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("Contrast", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTimeDigitized",
				Integer.valueOf(ExifSubIFDDirectory.TAG_SUBSECOND_TIME_DIGITIZED));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubjectDistRange", Integer.valueOf(ExifSubIFDDirectory.TAG_SUBJECT_DISTANCE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE
				.put("RelatedSoundFile", Integer.valueOf(ExifSubIFDDirectory.TAG_RELATED_SOUND_FILE));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTimeOriginal",
				Integer.valueOf(ExifSubIFDDirectory.TAG_SUBSECOND_TIME_ORIGINAL));
		//MetadataUtils.EXIF_NAME_TO_TYPE.put("Saturation", Integer.valueOf(ExifIFD0Directory.TAG_));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTime", Integer.valueOf(ExifSubIFDDirectory.TAG_SUBSECOND_TIME));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PhotometricInterpretation",
				Integer.valueOf(ExifSubIFDDirectory.TAG_PHOTOMETRIC_INTERPRETATION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Compression", Integer.valueOf(ExifDirectoryBase.TAG_COMPRESSION));
		ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubjectDistance", Integer.valueOf(ExifSubIFDDirectory.TAG_SUBJECT_DISTANCE));
	}

  public static int typeForExifTagName(String name) {
    return ERMetadataUtils.typeForTagName(ERMetadataUtils.EXIF_NAME_TO_TYPE, name);
  }

  public static int typeForIptcTagName(String name) {
    return ERMetadataUtils.typeForTagName(ERMetadataUtils.IPTC_NAME_TO_TYPE, name);
  }

  public static int typeForPdfTagName(String name) {
    return ERMetadataUtils.typeForTagName(IERMetadataDirectory.PDF, name);
  }

  public static int typeForTagName(String directoryName, String name) {
    int type;
    if (IERMetadataDirectory.EXIF.equalsIgnoreCase(directoryName)) {
      type = ERMetadataUtils.typeForTagName(ERMetadataUtils.EXIF_NAME_TO_TYPE, name);
    }
    else if (IERMetadataDirectory.IPTC.equalsIgnoreCase(directoryName)) {
      type = ERMetadataUtils.typeForTagName(ERMetadataUtils.IPTC_NAME_TO_TYPE, name);
    }
    else if (IERMetadataDirectory.PDF.equalsIgnoreCase(directoryName)) {
      type = name.hashCode();
    }
    else if (ERMetadataUtils.customMetadataDirectoryName().equals(directoryName)) {
      type = name.hashCode();
    }
    else {
      type = -1;
    }
    return type;
  }

  public static int typeForTagName(Map<String, Integer> map, String name) {
    int type;
    Integer typeInteger = map.get(name);
    if (typeInteger != null) {
      type = typeInteger.intValue();
    }
    else {
      type = -1;
    }
    return type;
  }

  public static Class classForTagName(String directoryName, String name) {
    return String.class;
  }

  public static String customMetadataDirectoryName() {
    return "Custom";
  }
}
