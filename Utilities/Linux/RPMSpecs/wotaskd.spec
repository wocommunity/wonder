Summary: wotaskd manage the application instances running on your application hosts.
Name: wotaskd
Version: 6
Release: 378
Copyright: 2006 - 2007 Apple Computer, Inc. All rights reserved.
Prefix: /opt/Local/Library/WebObjects/JavaApplications
BuildArchitectures: noarch
Group: Project Wonder/Deployment
Source: https://github.com/projectwonder/wonder/tree/master/Applications/wotaskd
URL: http://wiki.wocommunity.org/
Vendor: WOCommunity Association
Packager: Pascal Robert <info@wocommunity.org>

%description
Project Wonder Deployment uses wotaskd to manage the application instances running on your 
application hosts. Its main task is to start up instances when hosts are restarted. 
To accomplish this, wotaskd itself has to be restarted when the host starts up. 
This is done by configuring wotaskd as a service started when the computer boots. 
By default, a wotaskd process running on port 1085 is configured as a service on all 
supported platforms.

%prep

%setup -q -n %{name}-%{version}-src
# This tells ant to install software in a specific directory.
cat << EOF >> build.properties
base.path=%{buildroot}/opt/Local/Library/WebObjects/JavaApplications
EOF

%build

%install
rm -Rf %{buildroot}
mkdir -p %{buildroot}/opt/Local/Library/WebObjects/JavaApplications
mkdir -p %{buildroot}/etc/init.d/
%{__cp} -Rip ./output/build/{bin,conf,lib,logs,temp,webapps} %{buildroot}/opt/Local/Library/WebObjects/JavaApplications
%{__cp} %{_sourcedir}/Utilities/Linux/StartupScripts/RedHat/wotaskd %{buildroot}/etc/init.d/wotaskd

%clean
rm -rf %{buildroot}

%pre
getent group appserveradm > /dev/null || groupadd -r appserveradm
getent passwd appserver > /dev/null || useradd -r -g appserveradm appserver

%post
chkconfig --add %{name}

%preun
if [ "$1" = "0" ] ; then
service %{name} stop > /dev/null 2>&1
chkconfig --del %{name}
fi
