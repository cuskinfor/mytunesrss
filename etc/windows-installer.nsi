;NSIS MyTunesRSS

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

;--------------------------------
;General

  !include ..\target\project.nsh

  ;Name and file
  Name "MyTunesRSS ${PROJECT_VERSION}"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\${PROJECT_FINAL_NAME}"

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
  File /r /x .svn ..\target\${PROJECT_FINAL_NAME}-windows\${PROJECT_FINAL_NAME}\*
  File ..\target\MyTunesRSS.exe

  SetOutPath "$INSTDIR\data\jre"
  File /r /x .svn jre\*

  WriteUninstaller "$INSTDIR\Uninstall.exe"

  CreateDirectory "$SMPROGRAMS\MyTunesRSS\"
  CreateShortCut "$SMPROGRAMS\MyTunesRSS\MyTunesRSS.lnk" "$INSTDIR\MyTunesRSS.exe"
  CreateShortCut "$SMPROGRAMS\MyTunesRSS\Remove MyTunesRSS.lnk" "$INSTDIR\Uninstall.exe"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_MyTunesRSS ${LANG_GERMAN} "Die Programmdateien fuer MyTunesRSS."

  LangString DESC_MyTunesRSS ${LANG_ENGLISH} "The program files for MyTunesRSS."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${MyTunesRSS} $(DESC_MyTunesRSS)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  RMDir /r "$INSTDIR"
  RMDir /r "$APPDATA\MyTunesRSS3"
  RMDir /r "$APPDATA\MyTunesRSS4"
  RMDir /r "$APPDATA\MyTunesRSS5"
  RMDir /r "$APPDATA\MyTunesRSS-6"
  RMDir /r "$SMPROGRAMS\MyTunesRSS"

SectionEnd
