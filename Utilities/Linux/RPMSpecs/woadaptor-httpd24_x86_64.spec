Summary: woadaptor is a Apache 2.4 module to connect to wotaskd.
Name: woadaptor-httpd24
Version: 6
Release: 2.1
Prefix: /
BuildArch: x86_64
Group: Project Wonder/Deployment
Source: master
URL: http://wiki.wocommunity.org/
Vendor: WOCommunity Association
Packager: Pascal Robert <info@wocommunity.org>
License: NetStruxr Public Software License
BuildRoot: %{_builddir}/%{name}-root
Requires: httpd >= 2.4.0
BuildPrereq: gcc, make, sed >= 4.1.4
BuildRequires: httpd-devel >= 2.4.0

%description
woadaptor is a Apache 2.4 module that act as a bridge between your
Web server (Apache) and your wotaskd instances.

%prep
%setup -q -n wonder-integration
# This tells ant to install software in a specific directory.
cat << EOF >> build.properties
base.path=%{buildroot}%{_libdir}/httpd/modules/
EOF

%build
cd Utilities/Adaptors
sed -i 's/ADAPTOR_OS = MACOS/ADAPTOR_OS = LINUX/' make.config
sed -i 's/ADAPTORS = CGI Apache2.2/ADAPTORS = CGI Apache2.4/' make.config
make CC=gcc

%install
rm -Rf %{buildroot}
mkdir -p %{buildroot}%{_libdir}/httpd/modules/
mkdir -p %{buildroot}/etc/httpd/conf.d/
mkdir -p /opt/Local/Library/WebServer/Documents/WebObjects
%{__cp} $RPM_BUILD_DIR/wonder-integration/Utilities/Adaptors/Apache2.4/mod_WebObjects.so %{buildroot}%{_libdir}/httpd/modules/
%{__cp} $RPM_BUILD_DIR/wonder-integration/Utilities/Adaptors/Apache2.4/apache.conf %{buildroot}/etc/httpd/conf.d/webobjects.conf
sed -i 's"^ScriptAlias /cgi-bin/"## ScriptAlias /cgi-bin/"' /etc/httpd/conf/httpd.conf

%clean
rm -rf %{buildroot}

%pre

%post
service httpd graceful > /dev/null 2>&1

%preun
if [ "$1" = "0" ]; then
  mv /etc/httpd/conf.d/webobjects.conf /etc/httpd/conf.d/webobjects.conf.bak
  rm %{_libdir}/httpd/modules/mod_WebObjects.so
  service httpd graceful
fi

%files
%defattr(-,root,wheel,-)
%{_libdir}/httpd/modules/mod_WebObjects.so
/etc/httpd/conf.d/webobjects.conf