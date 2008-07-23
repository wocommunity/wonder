
all:
	rm -f make.preamble make.postamble
	@echo "COMMON_SRCFILES = \\" >> make.preamble
	for file in *.c; do \
		echo "$${file}: ../Adaptor/$${file}" | sed s/\\.c/.o/ >> make.postamble ; \
		echo "	$$""{CC} $$""{CFLAGS} -c ../Adaptor/$${file}" >> make.postamble ; \
		echo "		$${file} \\" >> make.preamble ; \
	done
	@echo "" >> make.preamble
	@echo "COMMON_OBJFILES = \\" >> make.preamble
	for file in *.c; do \
		echo "		$${file} \\" | sed s/\\.c/.o/ >> make.preamble ; \
	done
	@echo "" >> make.preamble

clean:
	rm -f make.preamble make.postamble
	rm -f *.o *.lo *.slo
	rm -rf .libs

