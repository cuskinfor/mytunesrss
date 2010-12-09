;NSIS MyTunesRSS

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  !define VERSION 4.0.3

  ;Name and file
  Name "MyTunesRSS ${VERSION}"
  OutFile "..\target\mytunesrss-${VERSION}-setup.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\MyTunesRSS"

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_WELCOME

  ;!insertmacro MUI_PAGE_LICENSE "license.txt"

  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES

  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES

;--------------------------------
;Languages

  !insertmacro MUI_LANGUAGE "English"
  !insertmacro MUI_LANGUAGE "German"

;--------------------------------
;Installer Sections

Section "!MyTunesRSS" MyTunesRSS

  SetOutPath "$INSTDIR"
  File /r /x .svn ..\target\mytunesrss-${VERSION}-windows.dir\mytunesrss-${VERSION}\*

  WriteUninstaller "$INSTDIR\Uninstall.exe"

  CreateDirectory "$SMPROGRAMS\MyTunesRSS"
  CreateShortCut "$SMPROGRAMS\MyTunesRSS\MyTunesRSS.lnk" "$INSTDIR\MyTunesRSS.exe"
  CreateShortCut "$SMPROGRAMS\MyTunesRSS\Remove MyTunesRSS.lnk" "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Java Runtime Environment" Jre

  SetOutPath "$INSTDIR"
  File /oname=MyTunesRSS.exe MyTunesRSS-jre.exe

  SetOutPath "$INSTDIR\data\jre"
  File /r /x .svn jre\*

  ExecWait '"$INSTDIR\data\jre\bin\unpack200.exe" "$INSTDIR\data\jre\lib\rt.jar.gz" "$INSTDIR\data\jre\lib\rt.jar"'
  Delete "$INSTDIR\data\jre\lib\rt.jar.gz"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_MyTunesRSS ${LANG_GERMAN} "Die Programmdateien f�r MyTunesRSS."
  LangString DESC_Jre ${LANG_GERMAN} "Java Laufzeitumgebung f�r MyTunesRSS. Optional, falls sie bereits Java 1.5 oder h�her auf Ihrem Rechner installiert haben."

  LangString DESC_MyTunesRSS ${LANG_ENGLISH} "The program files for MyTunesRSS."
  LangString DESC_Jre ${LANG_ENGLISH} "Java runtime for MyTunesRSS. Optional if you already hava Java 1.5 or better installed."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${MyTunesRSS} $(DESC_MyTunesRSS)
    !insertmacro MUI_DESCRIPTION_TEXT ${Jre} $(DESC_Jre)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  RMDir /r "$INSTDIR"
  RMDir /r "$APPDATA\MyTunesRSS3"
  RMDir /r "$SMPROGRAMS\MyTunesRSS"

SectionEnd
