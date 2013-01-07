Summary: womonitor manage your wotaskd instances.
Name: womonitor
Version: 6
Release: 0
Prefix: /opt/Local/Library/WebObjects/JavaApplications
BuildArch: noarch
Group: Project Wonder/Deployment
Source: master
URL: http://wiki.wocommunity.org/
Vendor: WOCommunity Association
Packager: Pascal Robert <info@wocommunity.org>
License: NetStruxr Public Software License
BuildRoot: %{_builddir}/%{name}-root

%description
Project Wonder Deployment uses womonitor to manage your wotaskd instances.
The management is done by a Web interface, running on port 56789. 

%prep
%setup -q -n wonder-master
# This tells ant to install software in a specific directory.
cat << EOF >> build.properties
base.path=%{buildroot}/opt/Local/Library/WebObjects/JavaApplications
EOF

%build
ant frameworks deployment.tools -Ddeployment.standalone=true -Dwo.external.root=$RPM_BUILD_DIR

%install
rm -Rf %{buildroot}
mkdir -p %{buildroot}/opt/Local/Library/WebObjects/JavaApplications
mkdir -p %{buildroot}/etc/init.d/
%{__cp} -Rip $RPM_BUILD_DIR/JavaMonitor.woa %{buildroot}/opt/Local/Library/WebObjects/JavaApplications
%{__cp} $RPM_BUILD_DIR/wonder-master/Utilities/Linux/StartupScripts/RedHat/womonitor %{buildroot}/etc/init.d/womonitor
mkdir %{buildroot}/opt/Local/Library/WebObjects/Applications
mkdir %{buildroot}/opt/Local/Library/WebObjects/Configuration
mkdir %{buildroot}/opt/Local/Library/WebObjects/Logs

%clean
rm -rf %{buildroot}

%pre
getent group appserveradm > /dev/null || groupadd -r appserveradm
getent passwd appserver > /dev/null || useradd -r -g appserveradm appserver

%post
chkconfig --add %{name}
chkconfig %{name} on
service %{name} start > /dev/null 2>&1

%preun
if [ "$1" = "0" ] ; then
service %{name} stop > /dev/null 2>&1
chkconfig --del %{name}
fi

%files
%defattr(-,appserver,appserveradm,-)
%dir /opt/Local/Library/WebObjects/Applications
%dir /opt/Local/Library/WebObjects/Logs
%dir /opt/Local/Library/WebObjects/Configuration
/opt/Local/Library/WebObjects/JavaApplications/JavaMonitor.woa
%attr(755,appserver,appserveradm) /opt/Local/Library/WebObjects/JavaApplications/JavaMonitor.woa/JavaMonitor
%attr(755,appserver,appserveradm) /opt/Local/Library/WebObjects/JavaApplications/JavaMonitor.woa/Contents/MacOS/JavaMonitor
%attr(755,root,wheel) /etc/init.d/womonitor
