include make.config

java${EXESUFFIX}: java.o java_md.o manifest_info.o WOStart.o
	${CC} ${CFLAGS} -o java${EXESUFFIX} *.o ${LIBS}


.c.o:
	${CC} ${CFLAGS} -c $<

.cpp.o:
	${CC} ${CFLAGS} -c $<
