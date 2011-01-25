VERSION=`svn --version | grep -c 1.6`
if [ $VERSION == "0" ]; then
	echo "You must be running SVN 1.6 to do a trunk merge."
	exit 0
fi
svn merge https://wonder.svn.sourceforge.net/svnroot/wonder/branches/Wonder_5_0_0_WebObjects_5_4_Branch/Wonder
