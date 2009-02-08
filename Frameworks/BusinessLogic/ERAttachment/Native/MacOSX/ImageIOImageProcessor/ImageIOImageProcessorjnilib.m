#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>
#import <ApplicationServices/ApplicationServices.h>
#import <QuartzCore/QuartzCore.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include "er_attachment_thumbnail_ImageIOImageProcessor.h"

CGColorSpaceRef createICCColorSpaceFromPathToProfile(const char *_iccProfilePath) {
	CMProfileLocation loc;
	// Specify that the location of the profile will be a POSIX path to the profile.
	loc.locType = cmPathBasedProfile;
	
	// Make sure the path is not larger then the buffer
	if(strlen(_iccProfilePath) > sizeof(loc.u.pathLoc.path)) {
		return NULL;
	}
	
	// Copy the path the profile into the CMProfileLocation structure
	strcpy(loc.u.pathLoc.path, _iccProfilePath);
	
	// Open the profile
	CMProfileRef iccProfile = (CMProfileRef) 0;
	if (CMOpenProfile(&iccProfile, &loc) != noErr) {
		iccProfile = (CMProfileRef) 0;
		return NULL;
	}
	
	// Create the ColorSpace with the open profile.
	CGColorSpaceRef iccColorSpace = CGColorSpaceCreateWithPlatformColorSpace(iccProfile);
	
	// Close the profile now that we have what we need from it.
	CMCloseProfile(iccProfile);
	
	return iccColorSpace;
}

JNIEXPORT jboolean JNICALL Java_er_attachment_thumbnail_ImageIOImageProcessor_processImage(JNIEnv *_env, jobject _obj, jint _resizeWidth, jint _resizeHeight, jint _dpi, jfloat _sharpenRadius, jfloat _sharpenIntensity, jfloat _gamma, jint _cropX, jint _cropY, jint _cropWidth, jint _cropHeight, jfloat _quality, jstring _profilePath, jstring _inputFilePath, jstring _outputFilePath, jstring _outputType) {
	return Java_er_attachment_thumbnail_ImageIOImageProcessor_processImage2(_env, _obj, _resizeWidth, _resizeHeight, _dpi, _sharpenRadius, _sharpenIntensity, _gamma, _cropX, _cropY, _cropWidth, _cropHeight, NULL, 0, _quality, _profilePath, _inputFilePath, _outputFilePath, _outputType);
}

JNIEXPORT jboolean JNICALL Java_er_attachment_thumbnail_ImageIOImageProcessor_processImage2(JNIEnv *_env, jobject _obj, jint _resizeWidth, jint _resizeHeight, jint _dpi, jfloat _sharpenRadius, jfloat _sharpenIntensity, jfloat _gamma, jint _cropX, jint _cropY, jint _cropWidth, jint _cropHeight, jstring _watermarkPath, jboolean _tileWatermark, jfloat _quality, jstring _profilePath, jstring _inputFilePath, jstring _outputFilePath, jstring _outputType) {
	jboolean thumbnailCreated = 1;
	NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
	
	CGColorSpaceRef colorSpaceRef = NULL;
	if (_profilePath != NULL) {
		const char *profilePathStr = (*_env)->GetStringUTFChars(_env, _profilePath, JNI_FALSE);
		colorSpaceRef = createICCColorSpaceFromPathToProfile(profilePathStr);
		(*_env)->ReleaseStringUTFChars(_env, _profilePath, profilePathStr);
	}
	
	const char *inputFilePathStr = (*_env)->GetStringUTFChars(_env, _inputFilePath, JNI_FALSE);
	NSString *path = [NSString stringWithUTF8String:inputFilePathStr];
	(*_env)->ReleaseStringUTFChars(_env, _inputFilePath, inputFilePathStr);
	
	// NTS: This is really lame, but we have to convert a PS to a PDF to properly thumbnail it.  I was never able to find
	// the proper API's to do this, but every Mac OS X box comes with pstopdf installed, so we just use that.
	NSString *tempPath = nil;
	NSString *extension = [[path pathExtension] lowercaseString];
	if (extension != nil && ([extension isEqualToString:@"ps"] || [extension isEqualToString:@"eps"])) {
		const char *buffer = [[NSString stringWithFormat:@"%@/%@", NSTemporaryDirectory(), @"AssetVaultPS2DF-XXXXXX.pdf"] cStringUsingEncoding:[NSString defaultCStringEncoding]];
		char *mutableBuffer = (char *)malloc(strlen(buffer) + 1);
		strcpy(mutableBuffer, buffer);
		int tempFileFD = mkstemps(mutableBuffer, 4);
		if (tempFileFD == -1) {
			NSLog(@"Failed to create temp file %s\n", buffer);
			thumbnailCreated = 0;
		}
		else {
			close(tempFileFD);
			tempPath = [NSString stringWithFormat:@"%s", mutableBuffer];
			free(mutableBuffer);
			
			NSTask *task = [[NSTask alloc] init];
			[task setLaunchPath:@"/usr/bin/pstopdf"];
			[task setArguments:[NSArray arrayWithObjects:path, @"-o", tempPath, nil]];
			[task launch];
			[task waitUntilExit];
			int status = [task terminationStatus];
			if (status != 0) {
				NSLog(@"Failed to convert EPS to PDF.\n");
				thumbnailCreated = 0;
			}
			[task release];
			path = tempPath;
		}
	}
	
	if (thumbnailCreated) {
		NSURL *imageURL = [NSURL fileURLWithPath:path];
		CGImageSourceRef source = CGImageSourceCreateWithURL((CFURLRef)imageURL, NULL);
		if (source != nil) {
			int maxPixelSize;
			if (_resizeWidth == -1 && _resizeHeight == -1) {
				maxPixelSize = -1;
			}
			else if (_resizeWidth > _resizeHeight) {
				maxPixelSize = _resizeWidth;
			}
			else {
				maxPixelSize = _resizeHeight;
			}
			
			CGImageRef originalImage;
			NSMutableDictionary *thumbnailOpts = [NSMutableDictionary dictionary];
			[thumbnailOpts setObject:(id)kCFBooleanTrue forKey:(id)kCGImageSourceCreateThumbnailWithTransform];
			[thumbnailOpts setObject:(id)kCFBooleanTrue forKey:(id)kCGImageSourceCreateThumbnailFromImageAlways];
			if (_dpi != -1) {
				NSNumber *dpiNumber = [NSNumber numberWithInt:_dpi];
				[thumbnailOpts setObject:dpiNumber forKey:(id)kCGImagePropertyDPIWidth];
				[thumbnailOpts setObject:dpiNumber forKey:(id)kCGImagePropertyDPIHeight];
			}
			
			if (maxPixelSize != -1) {
				[thumbnailOpts setObject:[NSNumber numberWithInt:maxPixelSize] forKey:(id)kCGImageSourceThumbnailMaxPixelSize];
				originalImage = CGImageSourceCreateThumbnailAtIndex(source, 0, (CFDictionaryRef)thumbnailOpts);
			}
			else {
				originalImage = CGImageSourceCreateImageAtIndex(source, 0, (CFDictionaryRef)thumbnailOpts);
				// This can happen if you have a PDF, for instance, which doesn't have an inherent width/height.  So we
				// have to just pick a width/height and go with it.
				if (originalImage == NULL) {
					[thumbnailOpts setObject:[NSNumber numberWithInt:1024] forKey:(id)kCGImageSourceThumbnailMaxPixelSize];
					originalImage = CGImageSourceCreateThumbnailAtIndex(source, 0, (CFDictionaryRef)thumbnailOpts);
				}
			}

			CFDictionaryRef metadata = CGImageSourceCopyPropertiesAtIndex(source, 0, NULL);
			CFRelease(source);
			source = NULL;

			if (originalImage == nil) {
				NSLog(@"Failed to load original image.\n");
				thumbnailCreated = 0;
			}
			else {
				const char *outputFilePathStr = (*_env)->GetStringUTFChars(_env, _outputFilePath, JNI_FALSE);
				NSURL *thumbnailURL = [NSURL fileURLWithPath:[NSString stringWithUTF8String:outputFilePathStr]];
				(*_env)->ReleaseStringUTFChars(_env, _outputFilePath, outputFilePathStr);
				const char *outputTypeStr = (*_env)->GetStringUTFChars(_env, _outputType, JNI_FALSE);
				NSString *destUTI = [NSString stringWithUTF8String:outputTypeStr];
				(*_env)->ReleaseStringUTFChars(_env, _outputType, outputTypeStr);
				
				CGImageRef processedImage = NULL;
				BOOL sharpen = (_sharpenRadius > 0.0 || _sharpenIntensity > 0.0);
				BOOL crop = (_cropWidth > 0 && _cropHeight > 0);
				BOOL gamma = (_gamma != 0.0);
				BOOL watermark = (_watermarkPath != NULL);
				BOOL processImage = sharpen || crop || gamma || watermark;
				BOOL processImageOrColorSpace = colorSpaceRef != nil || processImage;
				BOOL justProcessColorSpace = colorSpaceRef != nil && !processImage;
				if (!processImageOrColorSpace) {
					processedImage = originalImage;
				}
				else {
					if (justProcessColorSpace) {
						processedImage = CGImageCreateCopyWithColorSpace(originalImage, colorSpaceRef);
						if (processedImage != NULL) {
							CGImageRelease(originalImage);
							originalImage = NULL;
						}
					}
					
					if (processedImage == NULL) {
						//NSLog(@"Default colorspace conversion failed for %@.  Doing it the hard way ...\n", imageURL);
						size_t width;
						if (crop) {
							width = _cropWidth;
						}
						else {
							width = CGImageGetWidth(originalImage);
						}
						size_t height;
						if (crop) {
							height = _cropHeight;
						}
						else {
							height = CGImageGetHeight(originalImage);
						}
						size_t bitsPerComponent = CGImageGetBitsPerComponent(originalImage);
						size_t bytesPerRow = CGImageGetBytesPerRow(originalImage);
						size_t minimumBytesPerRow = 4 * width;
						if (bytesPerRow < minimumBytesPerRow) {
							NSLog(@"%@ reports a bytesPerRow of %d which is less than the minimum of %d.  Using the minimum instead.\n", imageURL, bytesPerRow, minimumBytesPerRow);
							bytesPerRow = minimumBytesPerRow;
						}
						
						if (colorSpaceRef == NULL) {
							colorSpaceRef = CGColorSpaceCreateWithName(kCGColorSpaceGenericRGB);
						}
						
						void *data = calloc(bytesPerRow, height);
						CGContextRef cgContext = CGBitmapContextCreate(data, width, height, bitsPerComponent, bytesPerRow, colorSpaceRef, kCGImageAlphaPremultipliedFirst);
						if (cgContext == nil) {
							NSLog(@"Failed to create bitmap context (width = %d, height = %d, bitsPerComponent = %d, bytesPerRow = %d).\n", width, height, bitsPerComponent, bytesPerRow);
							thumbnailCreated = 0;
						}
						else {
							// ... but sometimes it fails if the image has bogus info in it, which is really lame
							NSDictionary *ciContextOptions = [NSDictionary dictionaryWithObjectsAndKeys:
								[NSNumber numberWithBool: NO], kCIContextUseSoftwareRenderer,
								nil];
							CIContext *ciContext = [CIContext contextWithCGContext:cgContext options:ciContextOptions];
							if (ciContext == nil) {
								thumbnailCreated = 0;
								NSLog(@"Failed to create a CIContext!\n");
							}
							else {
								CIImage *inputImage = [CIImage imageWithCGImage:originalImage];
								CIImage *outputImage = inputImage;
								if (crop) {
									CIFilter *cropFilter = [CIFilter filterWithName:@"CICrop"];
									[cropFilter setValue:outputImage forKey:@"inputImage"];  
									int flippedCropY = (CGImageGetHeight(originalImage) - _cropY - _cropHeight);
									[cropFilter setValue:[CIVector vectorWithX:_cropX Y:flippedCropY Z:_cropWidth W:_cropHeight] forKey:@"inputRectangle"];
									outputImage = [cropFilter valueForKey:@"outputImage"];
									
										CIFilter *cropTransformFilter = [CIFilter filterWithName:@"CIAffineTransform"];
										NSAffineTransform *transform = [NSAffineTransform transform];
										[transform translateXBy:-_cropX yBy:-flippedCropY];
										[cropTransformFilter setValue:outputImage forKey:@"inputImage"];  
										[cropTransformFilter setValue:transform forKey:@"inputTransform"];
										outputImage = [cropTransformFilter valueForKey:@"outputImage"];
								}
								
								if (sharpen) {
									// If we need to sharpen, then use CoreImage ..
									CIFilter *unsharkMask = [CIFilter filterWithName:@"CIUnsharpMask"];
									[unsharkMask setValue:outputImage forKey:@"inputImage"];  
									[unsharkMask setValue:[NSNumber numberWithDouble:_sharpenIntensity] forKey:@"inputIntensity"];  
									[unsharkMask setValue:[NSNumber numberWithDouble:_sharpenRadius] forKey:@"inputRadius"];  
									outputImage = [unsharkMask valueForKey:@"outputImage"]; 
								}
								
								if (gamma) {
									CIFilter *gammaFilter = [CIFilter filterWithName:@"CIGammaAdjust"];
									[gammaFilter setValue:outputImage forKey:@"inputImage"];
									[gammaFilter setValue:[NSNumber numberWithDouble:_gamma] forKey:@"inputPower"];
									outputImage = [gammaFilter valueForKey:@"outputImage"]; 
								}
								
								if (watermark) {
									const char *watermarkPathStr = (*_env)->GetStringUTFChars(_env, _watermarkPath, JNI_FALSE);
									NSString *watermarkPath = [NSString stringWithUTF8String:watermarkPathStr];
									(*_env)->ReleaseStringUTFChars(_env, _watermarkPath, watermarkPathStr);
									CIImage *watermarkImage = [CIImage imageWithContentsOfURL:[NSURL fileURLWithPath:watermarkPath]];

									if (watermarkImage != nil) {
										if (_tileWatermark) {
											CIFilter *tileFilter = [CIFilter filterWithName:@"CIAffineTile"];
											[tileFilter setValue:watermarkImage forKey:@"inputImage"];
											NSAffineTransform *tileTransform = [NSAffineTransform transform];
											[tileTransform scaleXBy: 1.0 yBy: 1.0];
											[tileFilter setValue:tileTransform forKey:@"inputTransform"];
											watermarkImage = [tileFilter valueForKey:@"outputImage"]; 
										}
										else {
											CIFilter *centerFilter = [CIFilter filterWithName:@"CIAffineTransform"];
											[centerFilter setValue:watermarkImage forKey:@"inputImage"];
											NSAffineTransform *centerTransform = [NSAffineTransform transform];
											[centerTransform translateXBy: ([outputImage extent].size.width - [watermarkImage extent].size.width) / 2.0 yBy: ([outputImage extent].size.height - [watermarkImage extent].size.height) / 2.0];
											[centerFilter setValue:centerTransform forKey:@"inputTransform"];
											watermarkImage = [centerFilter valueForKey:@"outputImage"]; 
										}
										
										//CIFilter *dissolveFilter = [CIFilter filterWithName:@"CIMultiplyBlendMode"];
										CIFilter *dissolveFilter = [CIFilter filterWithName:@"CIScreenBlendMode"];
										[dissolveFilter setValue:outputImage forKey:@"inputImage"];
										[dissolveFilter setValue:watermarkImage forKey:@"inputBackgroundImage"];
										//									[dissolveFilter setValue:[NSNumber numberWithDouble:0.50] forKey:@"inputTime"];
										outputImage = [dissolveFilter valueForKey:@"outputImage"]; 
									}
									else {
										NSLog(@"Missing watermark image: %@", watermarkPath);
									}
								}
								
								CGRect imageRect = CGRectMake(0, 0, width, height);
								processedImage = [ciContext createCGImage:outputImage fromRect:imageRect];
								CGImageRelease(originalImage);
								originalImage = NULL;
							}
							CGContextRelease(cgContext);
							cgContext = NULL;
						}
						free(data);
						data = NULL;
					}
				}
				
				if (processedImage != NULL) {
					CGImageDestinationRef dest = CGImageDestinationCreateWithURL((CFURLRef)thumbnailURL, (CFStringRef)destUTI, 1, NULL);
					if (dest == nil) {
						NSLog(@"Failed to create destination image.");
						thumbnailCreated = 0;
					}
					else {
						NSMutableDictionary *metadataOpts = [NSMutableDictionary dictionary];
						if (_dpi != -1) {
							NSNumber *dpiNumber = [NSNumber numberWithInt:_dpi];
							[metadataOpts setObject:dpiNumber forKey:(id)kCGImagePropertyDPIWidth];
							[metadataOpts setObject:dpiNumber forKey:(id)kCGImagePropertyDPIHeight];
						}
						if (_quality >= 0.0) {
							[metadataOpts setObject:[NSNumber numberWithFloat:_quality] forKey:(id)kCGImageDestinationLossyCompressionQuality];
						}
						if (metadata != NULL) {
							[metadataOpts addEntriesFromDictionary:(NSDictionary *)metadata];
							CFRelease(metadata);
							metadata = NULL;
						}
						//CFMutableDictionaryRef metadataOpts = CFDictionaryCreateMutable(nil, 0,
						//	&kCFTypeDictionaryKeyCallBacks,  &kCFTypeDictionaryValueCallBacks);
						CGImageDestinationAddImage(dest, processedImage, (CFDictionaryRef)metadataOpts);
						
						BOOL status = (BOOL) CGImageDestinationFinalize(dest);
						if (!status) {
							thumbnailCreated = 0;
						}
						CFRelease(dest);
						dest = NULL;
					}

					if (processedImage != NULL && processedImage != originalImage) {
						CGImageRelease(processedImage);
						processedImage = NULL;
					}
				}
			}
		
			if (metadata != NULL) {
				CFRelease(metadata);
				metadata = NULL;
			}
			
			if (originalImage != NULL) {
				CGImageRelease(originalImage);
				originalImage = NULL;
			}
		}
	}

	if (colorSpaceRef != NULL) {
		CGColorSpaceRelease(colorSpaceRef);
		colorSpaceRef = NULL;
	}

	if (tempPath != nil) {
		BOOL tempFileRemoved = [[NSFileManager defaultManager] removeFileAtPath:tempPath handler:nil];
		if (!tempFileRemoved) {
			NSLog(@"Failed to remove temp file %@.\n", tempPath);
		}
	}

	[pool release];
	return thumbnailCreated;
}