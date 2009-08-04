;NSIS MyTunesRSS

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  ;Name and file
  Name "MyTunesRSS 3.7.3"
  OutFile "MyTunesRSS-3.7.3-Setup.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\MyTunesRSS"
  
  ;Request application privileges for Windows Vista
  RequestExecutionLevel user

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------
;Installer Sections

Section "MyTunesRSS" MyTunesRSS

  SetOutPath "$INSTDIR"
  File /r /x .svn ..\..\target\mytunesrss-3.7.4-SNAPSHOT-windows.dir\mytunesrss-3.7.4-SNAPSHOT\*
  File /oname=MyTunesRSS.exe ..\MyTunesRSS-jre.exe
  
  SetOutPath "$INSTDIR\data\jre"
  File /r /x .svn ..\jre\*

  SetOutPath "$APPDATA\MyTunesRSS3"
  File MyTunesRSS.key
  
  ; UNPACK JRE
  ExecWait '"$INSTDIR\data\jre\bin\unpack200.exe" "$INSTDIR\data\jre\lib\rt.jar.gz" "$INSTDIR\data\jre\lib\rt.jar"'
  
  ; Start Menu shortcut
  CreateDirectory "$SMPROGRAMS\MyTunesRSS"
  CreateShortCut "$SMPROGRAMS\MyTunesRSS\MyTunesRSS.lnk" "$INSTDIR\MyTunesRSS.exe"
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  RMDir /r "$INSTDIR"
  RMDir /r "$APPDATA\MyTunesRSS3"
  RMDir /r "$SMPROGRAMS\MyTunesRSS"
  
SectionEnd
