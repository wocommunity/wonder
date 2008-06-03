#!/bin/sh
# add this file as a build phase in your main target, right after the Frameworks & Libraries phase.
# 1) Set the current target to your app
# 2) Click on "Files & Build Phases" tab
# 3) Choose "Project/New Build Phase/New Shell Script Build Phase" from the Menu 
#    and drag the build phase right before the "Frameworks & Libraries"
# 4) Insert the code 
#       sh ~/Roots/ERExtensions.framework/Resources/InstallCompilerProxySupport.sh
# NOTE: I'm not sure about the real workings of the build process in the new Project Builder.
#       All I can say is that this works for me...

. "${BUILD_FILES_DIR}/DefineFileLocations.sh"


if [ -d "../Sources" ]
then
	find ../Sources -name '*.java' | xargs egrep -H '^package(.*);' |sed -e 's/package //' |sed -e 's/;//' > CPFileList.txt
	find ../Sources -name '*.java' | xargs egrep -L '^package(.*);' |sed -e 's/$/:/' >> CPFileList.txt
else
	find . -name '*.java' | xargs egrep -H '^package(.*);' |sed -e 's/package //' |sed -e 's/;//' > CPFileList.txt
	find . -name '*.java' | xargs egrep -L '^package(.*);' |sed -e 's/$/:/' >> CPFileList.txt
fi

if [ -f "${RESOURCES_JAVA_DIR}" ]
then
    echo "Compiler Proxy: Deleting left-over class files in the Java Directory"
    rm -Rf "${RESOURCES_JAVA_DIR}"
    mkdir "'${RESOURCES_JAVA_DIR}'"
fi

if [ -f "${SERVER_BUILD_DIR}/Classpath" ]
then
    echo "Compiler Proxy: Fixing Classpath entry"
    mv "${SERVER_BUILD_DIR}/Classpath" "${SERVER_BUILD_DIR}/Classpath.tmp"
    echo -n "APPROOT/Resources/Java:" >"${SERVER_BUILD_DIR}/Classpath"
    cat "${SERVER_BUILD_DIR}/Classpath.tmp" >> "${SERVER_BUILD_DIR}/Classpath"
    rm "${SERVER_BUILD_DIR}/Classpath.tmp"
fi
