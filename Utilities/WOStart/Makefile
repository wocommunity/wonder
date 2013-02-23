include make.config

WOStart${EXESUFFIX}: java.o java_md.o manifest_info.o WOStart.o
	${CC} ${CFLAGS} -o ${WINEDITION}/WOStart${EXESUFFIX} *.o ${LIBS}
	strip ${WINEDITION}/WOStart${EXESUFFIX}

.c.o:
	${CC} ${CFLAGS} -c $<

.cpp.o:
	${CC} ${CFLAGS} -c $<

clean:
	rm *.o