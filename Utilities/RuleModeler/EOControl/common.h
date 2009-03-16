#define WO_JAVA_COMPATIBILITY 1
#define sel_eq(sela,selb) (sela==selb?YES:NO)
#define objc_calloc(c,s) NSZoneCalloc(NSDefaultMallocZone(),(c),(s))
#define objc_malloc(s) NSZoneMalloc(NSDefaultMallocZone(),(s))
#define objc_free(p) NSZoneFree(NSDefaultMallocZone(),(p))
