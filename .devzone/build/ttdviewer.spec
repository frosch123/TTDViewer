
Name:           %{dz_repo}
Version:        %{dz_version}
Release:        %{dz_release}.%{_vendor}%{?suse_version}
Summary:        TTDViewer to view png or pcx sprites with the ttd 8bit color palette

Group:          Development/Tools
License:        GPLv2
URL:            http://dev.openttdcoop.org/projects/ttdviewer
Source0:        %{name}-%{version}.tar

BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-buildroot

BuildArch:			i586

BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  libxml2 
BuildRequires:  mercurial zip

%description
  TTDViewer is a tool written in Java to display images related to
  Transport Tycoon Deluxe (Chris Sawyer, Microprose), TTDPatch (http://www.ttdpatch.net)
  and/or OpenTTD (http://www.openttd.org). It allows viewing of sprites in .png or .pcx format
  using the games' 8 bit palette including the handling of the games' palette animation
  and common recolorings.

  TTDViewer is licensed under the GNU General Public License version 2.0. For
  more information, see the file 'COPYING'.

%prep
%setup -qn %{name}

%build
make %{?_smp_mflags}
# additional zip bundle:
make release

%install
make jar
install -D -m0644 release/TTDViewer.jar %{buildroot}/%{_javadir}/TTDViewer.jar

cat >TTDViewer <<EOF
#!/bin/sh
java -jar %{_javadir}/TTDViewer.jar
EOF
install -D -m755 TTDViewer %{buildroot}/%{_bindir}/TTDViewer

%clean

%files
%defattr(-,root,root)
%doc COPYING readme.txt 
%{_bindir}/TTDViewer
%{_javadir}/TTDViewer.jar

%changelog
