#import <Foundation/Foundation.h>
#import <AppKit/AppKit.h>
#import <ApplicationServices/ApplicationServices.h>
#import <Quartz/Quartz.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "er_attachment_metadata_ImageIOMetadataParser.h"

void readMetadata(JNIEnv *_env, jobject _obj, jmethodID _addMetadataEntryMethodID, jobject _metadataDirectorySet, NSDictionary *_metadata, NSString *_directoryName) {
	if (_metadata != nil) {
		jstring jDirectoryName = (*_env)->NewStringUTF(_env, [_directoryName UTF8String]); 
		NSEnumerator *metadataDictionaryKeysEnum = [_metadata keyEnumerator];
		NSString *key;
		while ((key = [metadataDictionaryKeysEnum nextObject]) != nil) {
			id value = [_metadata objectForKey:key];
			NSString *strValue = nil;
			if ([value isKindOfClass:[NSString class]]) {
				strValue = (NSString *)value;
			}
			else if ([value isKindOfClass:[NSNumber class]]) {
				strValue = [((NSNumber *)value) stringValue];
			}
			else {
				//NSLog(@"unknown %@=%@\n", key, value);
			}
			if (strValue != nil) {
				jstring jKey = (*_env)->NewStringUTF(_env, [key UTF8String]);
				jstring jValue = (*_env)->NewStringUTF(_env, [strValue UTF8String]);
				(*_env)->CallVoidMethod(_env, _obj, _addMetadataEntryMethodID, _metadataDirectorySet, jDirectoryName, jKey, jValue);
				(*_env)->DeleteLocalRef(_env, jKey);
				(*_env)->DeleteLocalRef(_env, jValue);
			}
		}
		(*_env)->DeleteLocalRef(_env, jDirectoryName); 
	}
	else {
//		NSLog(@"There is no dictionary for %@.\n", _directoryName);
	}
}

JNIEXPORT jboolean JNICALL Java_er_attachment_metadata_ImageIOMetadataParser_parseMetadata0(JNIEnv *_env, jobject _obj, jobject _metadataDirectorySet, jstring _inputFilePath) {
	NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
	
	jboolean metadataImported = 0;
	char *errorMessage = NULL;
	
	const char *inputFilePathStr = (*_env)->GetStringUTFChars(_env, _inputFilePath, JNI_FALSE);
	
	NSString *path = [NSString stringWithUTF8String:inputFilePathStr];
	NSURL *imageURL = [NSURL fileURLWithPath:path];
	//NSLog(@"File: %@\n", [NSString stringWithCString:inputFilePathStr]);
	(*_env)->ReleaseStringUTFChars(_env, _inputFilePath, inputFilePathStr);

	int imageWidth = -1;
	int imageHeight = -1;
	
	jclass metadataDirectoryClass = (*_env)->GetObjectClass(_env, _obj);
	if (metadataDirectoryClass == NULL) {
		errorMessage = "Missing MetadataDirectory class";
	}
	else {
		jmethodID addMetadataEntryMethodID = (*_env)->GetMethodID(_env, metadataDirectoryClass, "addMetadataEntry", "(Ler/attachment/metadata/ERMetadataDirectorySet;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
		if (addMetadataEntryMethodID == NULL) {
			errorMessage = "Missing addMetadataEntry method!";
		}
		else {
			NSString *extension = [path pathExtension];
			if (extension != nil && [[extension lowercaseString] isEqualToString:@"pdf"]) {
				PDFDocument *pdfDoc = [[PDFDocument alloc] initWithURL: [NSURL fileURLWithPath: path]];

				NSMutableDictionary *pdfMetadata = [[NSMutableDictionary alloc] init];

				int pageCount = [pdfDoc pageCount];
				if (pageCount > 0) {
					PDFPage *page = [pdfDoc pageAtIndex:0];
					NSRect rect = [page boundsForBox:kPDFDisplayBoxCropBox];
					imageWidth = rect.size.width;
					imageHeight = rect.size.height;

					int rotation = [page rotation];
					[pdfMetadata setObject:[NSNumber numberWithInt:rotation] forKey:@"Rotation"];
				}

				NSDictionary *documentAttributes = [pdfDoc documentAttributes];
				NSString *title = (NSString *)[documentAttributes objectForKey:@"Title"];
				if (title != nil) {
					[pdfMetadata setObject:title forKey:@"Title"];
				}
				NSString *author = (NSString *)[documentAttributes objectForKey:@"Author"];
				if (author != nil) {
					[pdfMetadata setObject:author forKey:@"Author"];
				}
				NSString *subject = (NSString *)[documentAttributes objectForKey:@"Subject"];
				if (subject != nil) {
					[pdfMetadata setObject:subject forKey:@"Subject"];
				}
				NSString *creator = (NSString *)[documentAttributes objectForKey:@"Creator"];
				if (creator != nil) {
					[pdfMetadata setObject:creator forKey:@"Creator"];
				}
				NSString *producer = (NSString *)[documentAttributes objectForKey:@"Producer"];
				if (producer != nil) {
					[pdfMetadata setObject:producer forKey:@"Producer"];
				}
				NSString *creationDate = (NSString *)[documentAttributes objectForKey:@"CreationDate"];
				if (creationDate != nil) {
					[pdfMetadata setObject:creationDate forKey:@"CreationDate"];
				}
				NSString *modificationDate = (NSString *)[documentAttributes objectForKey:@"ModDate"];
				if (modificationDate != nil) {
					[pdfMetadata setObject:modificationDate forKey:@"ModDate"];
				}
				NSArray *keywords = (NSArray *)[documentAttributes objectForKey:@"Keywords"];
				if (keywords != nil) {
					NSString *keywordsStr = [keywords componentsJoinedByString:@","]; 
					[pdfMetadata setObject:keywordsStr forKey:@"Keywords"];
				}
				readMetadata(_env, _obj, addMetadataEntryMethodID, _metadataDirectorySet, pdfMetadata, @"{PDF}");
				[pdfMetadata release];
				
				[pdfDoc release];
				metadataImported = 1;
			}
			else {
				CGImageSourceRef source = CGImageSourceCreateWithURL((CFURLRef)imageURL, NULL);
				if (source != nil) {
					NSMutableDictionary *metadata = (NSMutableDictionary *)CGImageSourceCopyPropertiesAtIndex(source, 0, NULL);
					//NSLog(@"metadata = %@\n", metadata);
					if (metadata != nil) {
						imageWidth = [[metadata objectForKey:(id)kCGImagePropertyPixelWidth] intValue];
						imageHeight = [[metadata objectForKey:(id)kCGImagePropertyPixelHeight] intValue];
						readMetadata(_env, _obj, addMetadataEntryMethodID, _metadataDirectorySet, (NSDictionary *)[metadata objectForKey:(NSString *)kCGImagePropertyExifDictionary], (NSString *)kCGImagePropertyExifDictionary);
						readMetadata(_env, _obj, addMetadataEntryMethodID, _metadataDirectorySet, (NSDictionary *)[metadata objectForKey:(NSString *)kCGImagePropertyIPTCDictionary], (NSString *)kCGImagePropertyIPTCDictionary);
						readMetadata(_env, _obj, addMetadataEntryMethodID, _metadataDirectorySet, (NSDictionary *)[metadata objectForKey:(NSString *)kCGImagePropertyTIFFDictionary], (NSString *)kCGImagePropertyTIFFDictionary);
						metadataImported = 1;
						CFRelease(metadata);
					}
					CFRelease(source);
				}
				else {
					NSLog(@"There was no file %@.\n", [NSString stringWithUTF8String:inputFilePathStr]);
				}
			}
		}
		(*_env)->DeleteLocalRef(_env, metadataDirectoryClass);
	}

	if (errorMessage == NULL) {
		jclass metadataDirectorySetClass = (*_env)->GetObjectClass(_env, _metadataDirectorySet);
		jmethodID setWidthMethodID = (*_env)->GetMethodID(_env, metadataDirectorySetClass, "setWidth", "(I)V");
		if (setWidthMethodID != NULL) {
			(*_env)->CallVoidMethod(_env, _metadataDirectorySet, setWidthMethodID, (jint)imageWidth);
		}
		else {
			errorMessage = "There was no setWidth method.";
		}
		
		jmethodID setHeightMethodID = (*_env)->GetMethodID(_env, metadataDirectorySetClass, "setHeight", "(I)V");
		if (setHeightMethodID != NULL) { 
			(*_env)->CallVoidMethod(_env, _metadataDirectorySet, setHeightMethodID, (jint)imageHeight);
		}
		else {
			errorMessage = "There was no setHeight method.";
		}
		(*_env)->DeleteLocalRef(_env, metadataDirectorySetClass);
	}
	
	if (errorMessage != NULL) {
		jclass excCls = (*_env)->FindClass(_env, "java/lang/IllegalArgumentException");
		if (excCls != 0) {
			(*_env)->ThrowNew(_env, excCls, errorMessage);
			(*_env)->DeleteLocalRef(_env, excCls);
		}
	}
	
	[pool release];
	
	return metadataImported;
}

