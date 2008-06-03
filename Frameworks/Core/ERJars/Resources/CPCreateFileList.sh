if [ -d "Sources" ]
then
        find Sources -name '*.java' | xargs egrep -H '^package(.*);' |sed -e 's/package //' |sed -e 's/;//' > CPFileList.txt
        find Sources -name '*.java' | xargs egrep -L '^package(.*);' |sed -e 's/$/:/' >> CPFileList.txt
else
        find . -name '*.java' | xargs egrep -H '^package(.*);' |sed -e 's/package //' |sed -e 's/;//' > CPFileList.txt
        find . -name '*.java' | xargs egrep -L '^package(.*);' |sed -e 's/$/:/' >> CPFileList.txt
fi
