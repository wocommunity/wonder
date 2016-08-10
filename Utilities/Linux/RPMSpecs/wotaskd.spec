Summary: wotaskd manage the application instances running on your application hosts.
Name: wotaskd
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
Project Wonder Deployment uses wotaskd to manage the application instances running on your 
application hosts. Its main task is to start up instances when hosts are restarted. 
To accomplish this, wotaskd itself has to be restarted when the host starts up. 
This is done by configuring wotaskd as a service started when the computer boots. 
By default, a wotaskd process running on port 1085 is configured as a service on all 
supported platforms.

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
%{__cp} -Rip $RPM_BUILD_DIR/wotaskd.woa %{buildroot}/opt/Local/Library/WebObjects/JavaApplications
%{__cp} $RPM_BUILD_DIR/wonder-master/Utilities/Linux/StartupScripts/RedHat/wotaskd %{buildroot}/etc/init.d/wotaskd
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
/opt/Local/Library/WebObjects/JavaApplications/wotaskd.woa
%attr(755,appserver,appserveradm) /opt/Local/Library/WebObjects/JavaApplications/wotaskd.woa/wotaskd
%attr(755,appserver,appserveradm) /opt/Local/Library/WebObjects/JavaApplications/wotaskd.woa/Contents/Resources/javawoservice.sh
%attr(755,appserver,appserveradm) /opt/Local/Library/WebObjects/JavaApplications/wotaskd.woa/Contents/Resources/SpawnOfWotaskd.sh
%attr(755,root,wheel) /etc/init.d/wotaskd
