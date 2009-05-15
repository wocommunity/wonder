package er.attachment.metadata;

import java.util.HashMap;
import java.util.Map;

import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.iptc.IptcDirectory;

public class ERMetadataUtils {
  private static Map<String, Integer> IPTC_NAME_TO_TYPE = new HashMap<String, Integer>();
  private static Map<String, Integer> EXIF_NAME_TO_TYPE = new HashMap<String, Integer>();
  static {
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Caption/Abstract", new Integer(IptcDirectory.TAG_CAPTION));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("City", new Integer(IptcDirectory.TAG_CITY));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("CopyrightNotice", new Integer(IptcDirectory.TAG_COPYRIGHT_NOTICE));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Country/PrimaryLocationName", new Integer(IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Credit", new Integer(IptcDirectory.TAG_CREDIT));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("DateCreated", new Integer(IptcDirectory.TAG_DATE_CREATED));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("TimeCreated", new Integer(IptcDirectory.TAG_TIME_CREATED));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Province/State", new Integer(IptcDirectory.TAG_PROVINCE_OR_STATE));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Source", new Integer(IptcDirectory.TAG_SOURCE));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("Category", new Integer(IptcDirectory.TAG_CATEGORY));
    ERMetadataUtils.IPTC_NAME_TO_TYPE.put("ObjectName", new Integer(IptcDirectory.TAG_OBJECT_NAME));

    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureTime", new Integer(ExifDirectory.TAG_EXPOSURE_TIME));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Flash", new Integer(ExifDirectory.TAG_FLASH));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ColorSpace", new Integer(ExifDirectory.TAG_COLOR_SPACE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SceneCaptureType", new Integer(ExifDirectory.TAG_SCENE_TYPE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneYResolution", new Integer(ExifDirectory.TAG_FOCAL_PLANE_Y_RES));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneResolutionUnit", new Integer(ExifDirectory.TAG_FOCAL_PLANE_UNIT));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalLength", new Integer(ExifDirectory.TAG_FOCAL_LENGTH));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ShutterSpeedValue", new Integer(ExifDirectory.TAG_SHUTTER_SPEED));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FNumber", new Integer(ExifDirectory.TAG_FNUMBER));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PixelYDimension", new Integer(ExifDirectory.TAG_EXIF_IMAGE_WIDTH));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ApertureValue", new Integer(ExifDirectory.TAG_APERTURE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PixelXDimension", new Integer(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("CustomRendered", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("WhiteBalance", new Integer(ExifDirectory.TAG_WHITE_BALANCE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTimeDigitized", new Integer(ExifDirectory.TAG_DATETIME_DIGITIZED));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureProgram", new Integer(ExifDirectory.TAG_EXPOSURE_PROGRAM));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTimeOriginal", new Integer(ExifDirectory.TAG_DATETIME_ORIGINAL));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("MeteringMode", new Integer(ExifDirectory.TAG_METERING_MODE));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureMode", new Integer(ExifDirectory.TAG_EXPO));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ExposureBiasValue", new Integer(ExifDirectory.TAG_EXPOSURE_BIAS));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("FocalPlaneXResolution", new Integer(ExifDirectory.TAG_FOCAL_PLANE_X_RES));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Orientation", new Integer(ExifDirectory.TAG_ORIENTATION));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Model", new Integer(ExifDirectory.TAG_MODEL));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("YResolution", new Integer(ExifDirectory.TAG_Y_RESOLUTION));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("XResolution", new Integer(ExifDirectory.TAG_X_RESOLUTION));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("DateTime", new Integer(ExifDirectory.TAG_DATETIME));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Make", new Integer(ExifDirectory.TAG_MAKE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ResolutionUnit", new Integer(ExifDirectory.TAG_RESOLUTION_UNIT));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Copyright", new Integer(ExifDirectory.TAG_COPYRIGHT));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Artist", new Integer(ExifDirectory.TAG_ARTIST));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Software", new Integer(ExifDirectory.TAG_SOFTWARE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("ImageDescription", new Integer(ExifDirectory.TAG_IMAGE_DESCRIPTION));

    //MetadataUtils.EXIF_NAME_TO_TYPE.put("FocalLenIn35mmFilm", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("UserComment", new Integer(ExifDirectory.TAG_USER_COMMENT));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("GainControl", new Integer(ExifDirectory.TAG_));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("DigitalZoomRatio", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("MaxApertureValue", new Integer(ExifDirectory.TAG_MAX_APERTURE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("LightSource", new Integer(ExifDirectory.TAG_LIGHT_SOURCE));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("CompressedBitsPerPixel", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SensingMethod", new Integer(ExifDirectory.TAG_SENSING_METHOD));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("Sharpness", new Integer(ExifDirectory.TAG_));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("Contrast", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTimeDigitized", new Integer(ExifDirectory.TAG_SUBSECOND_TIME_DIGITIZED));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubjectDistRange", new Integer(ExifDirectory.TAG_SUBJECT_DISTANCE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("RelatedSoundFile", new Integer(ExifDirectory.TAG_RELATED_SOUND_FILE));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTimeOriginal", new Integer(ExifDirectory.TAG_SUBSECOND_TIME_ORIGINAL));
    //MetadataUtils.EXIF_NAME_TO_TYPE.put("Saturation", new Integer(ExifDirectory.TAG_));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubsecTime", new Integer(ExifDirectory.TAG_SUBSECOND_TIME));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("PhotometricInterpretation", new Integer(ExifDirectory.TAG_PHOTOMETRIC_INTERPRETATION));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("Compression", new Integer(ExifDirectory.TAG_COMPRESSION));
    ERMetadataUtils.EXIF_NAME_TO_TYPE.put("SubjectDistance", new Integer(ExifDirectory.TAG_SUBJECT_DISTANCE));
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
