
; MethodScript Installer
; Checks for Java, and if not present, downloads and installs AdoptOpenJDK
; then downloads the latest MethodScript jar, running the install command.
; Optionally, sample files from the 'examples' command can be installed.

Unicode true

!include "MUI2.nsh"
!include "LogicLib.nsh"
!include "nsDialogs.nsh"
!include "x64.nsh"
!include "FileFunc.nsh"

Name "MethodScript Cmdline Installer"
Caption "MethodScript Cmdline"
OutFile "MethodScriptInstaller.exe"
ShowInstDetails show

Function .onInit
	${If} ${RunningX64}
		SetRegView 64
	${EndIf}
	SetOutPath $TEMP
	File /oname=spltmp.bmp "..\..\main\resources\siteDeploy\resources\images\CommandHelper_IconHighRes_Sprite.bmp"
	advsplash::show 750 1500 1500 0xFF00FF $TEMP\spltmp
	Pop $0
	Delete $TEMP\spltmp.bmp
FunctionEnd

Function un.onInit
	${If} ${RunningX64}
		SetRegView 64
	${EndIf}
FunctionEnd

!define MUI_ICON "..\..\main\resources\siteDeploy\resources\images\favicon.ico"
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "..\..\main\resources\siteDeploy\resources\images\CommandHelper_Icon.bmp"
!define MUI_HEADERIMAGE_RIGHT


!define MUI_WELCOMEPAGE_TITLE "MethodScript Cmdline Installer"
!define MUI_WELCOMEPAGE_TEXT "This installer downloads and configures your system with everything you need to \
run MethodScript from the command line. If Java is already installed, that version will be used, but if you don't \
have Java installed, AdoptOpenJDK will be installed for you. If you wish to use another version of Java other than \
those offered, please exit the installer, install Java manually, then restart the installer."
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\..\..\LICENSE.txt"
Page Custom CheckJava LeaveJava

!insertmacro MUI_PAGE_INSTFILES


!insertmacro MUI_LANGUAGE "English"
Var JavaInstallationPath
Var Dialog
Var InstallJava
Var JavaVersion
Var JDK
Var JRE
Var UseJRE
Var Use64Bit

Function CheckJava

	Call _FindJava
	${If} $JavaInstallationPath != ""
		StrCpy $InstallJava "false"
		Abort
	${EndIf}

	StrCpy $InstallJava "true"
	!insertmacro MUI_HEADER_TEXT "Java Installation" "Select the Java version you wish to install. \
	If you wish to install another version, exit the installer, manually install Java, then run it again. \
	Only LTS versions are available through this installer."
	nsDialogs::Create 1018
	Pop $Dialog

	${If} $Dialog == error
		Abort
	${EndIf}

	${NSD_CreateGroupBox} 10% 10u 80% 62u "JDK/JRE"
	Pop $0

		${NSD_CreateLabel} 20% 25u 50% 10u "If you aren't sure which one to select, use JRE"

		${NSD_CreateRadioButton} 20% 35u 20% 10u "JRE"
		Pop $JRE

		${NSD_CreateRadioButton} 20% 45u 20% 10u "JDK"
		Pop $JDK

	${NSD_CreateGroupBox} 5% 75u 90% 34u "Java Version"
	Pop $0

		${NSD_CreateDropList} 10% 87u 80% 80u "Version"
		Pop $JavaVersion
			${NSD_CB_AddString} $JavaVersion "8 (recommended)"
			${NSD_CB_AddString} $JavaVersion "11"

	nsDialogs::Show
FunctionEnd

Function LeaveJava
	${NSD_GetState} $JRE $R0
	${NSD_GetState} $JDK $R1

	${If} $R0 == 1
		StrCpy $UseJRE "true"
	${ElseIf} $R1 == 1
		StrCpy $UseJRE "false"
	${Else}
		MessageBox MB_ICONEXCLAMATION "Please select either the JDK or JRE"
		Abort
	${EndIf}

	${NSD_GetText} $JavaVersion $R0

	${If} $R0 == ""
		MessageBox MB_ICONEXCLAMATION "Please select the Java version you wish to install"
		Abort
	${ElseIf} $R0 == "8 (recommended)"
		StrCpy $JavaVersion "8"
	${Else}
		StrCpy $JavaVersion $R0
	${EndIf}

	${If} ${RunningX64}
		StrCpy $Use64Bit "true"
	${Else}
		StrCpy $Use64Bit "false"
	${EndIf}
FunctionEnd

Section DoOverview
	${If} $InstallJava == "true"
		DetailPrint "Installing Java"
		DetailPrint "Using JRE: $UseJRE"
		DetailPrint "Java Version to install: $JavaVersion"
		DetailPrint "Install 64 bit Java: $Use64Bit"
	${Else}
		DetailPrint "Skipping Java Installation"
	${EndIf}
SectionEnd

Var JavaDownload
Section DoJavaInstall
	${If} $InstallJava == "false"
		goto DoJavaInstallEnd
	${EndIf}
	${If} $InstallJava == "true"
		${If} $JavaVersion == "8"
			${If} $UseJRE == "true"
				${If} $Use64Bit == "true"
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jre_x64_windows_hotspot_8u282b08.msi"
				${Else}
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jre_x86-32_windows_hotspot_8u282b08.msi"
				${EndIf}
			${Else}
				${If} $Use64Bit == "true"
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jdk_x64_windows_hotspot_8u282b08.msi"
				${Else}
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u282-b08/OpenJDK8U-jdk_x86-32_windows_hotspot_8u282b08.msi"
				${EndIf}
			${EndIf}
		${ElseIf} $JavaVersion == "11"
			${If} $UseJRE == "true"
				${If} $Use64Bit == "true"
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.10_9.msi"
				${Else}
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jre_x86-32_windows_hotspot_11.0.10_9.msi"
				${EndIf}
			${Else}
				${If} $Use64Bit == "true"
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x64_windows_hotspot_11.0.10_9.msi"
				${Else}
					StrCpy $JavaDownload "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.10%2B9/OpenJDK11U-jdk_x86-32_windows_hotspot_11.0.10_9.msi"
				${EndIf}
			${EndIf}
		${Else}
			;; Forgotten use case!
			MessageBox MB_ICONSTOP "Error, missing use case!"
		${EndIf}
	${EndIf}
	GetInstaller:
	DetailPrint "Downloading Java Installer from $JavaDownload to $TEMP\JavaInstaller.msi"
	inetc::get $JavaDownload $TEMP\JavaInstaller.msi
	Pop $R0 ;Get the return value
	${If} $R0 == "success"
		MessageBox MB_ICONSTOP "Download failed: $R0"
		Abort
	${EndIf}
	DetailPrint "Installing Java"
	ExecWait "msiexec /i $\"$TEMP\JavaInstaller.msi$\"" $0
	${If} $0 != 0
		MessageBox MB_ABORTRETRYIGNORE "Java installation failed or was cancelled" IDABORT AbortInstaller IDRETRY GetInstaller
	${EndIf}

	Delete $TEMP\JavaInstaller.msi
	Call _FindJava
	${If} $JavaInstallationPath == ""
		MessageBox MB_ABORTRETRYIGNORE "After installation, Java still cannot be found" IDABORT AbortInstaller IDRETRY GetInstaller
	${EndIf}
	goto DoJavaInstallEnd
	AbortInstaller:
		Abort
	DoJavaInstallEnd:
SectionEnd

Section DoDownloadMethodScript
	; First, create the AppData folder, which is where the jar will go
	;;;; TODO: Write out the icon in the installer, and update the DoConfigureUninstaller location
	CreateDirectory $LOCALAPPDATA\MethodScript
	DetailPrint "Downloading latest MethodScript jar to $LOCALAPPDATA\MethodScript\MethodScript.jar"
	inetc::get "https://methodscript.com/MethodScript.jar" $LOCALAPPDATA\MethodScript\MethodScript.jar
	ExecWait "java -jar $LOCALAPPDATA\MethodScript\MethodScript.jar install-cmdline"
	ExecWait "java -jar $LOCALAPPDATA\MethodScript\MethodScript.jar eval $\"exit()$\""
	SetOutPath $LOCALAPPDATA\MethodScript
	File /oname=icon.ico "..\..\main\resources\siteDeploy\resources\images\commandhelper_icon.ico"
	; This might not be working, but anyways, the icon is used in the uninstaller, so putting the icon in the folder is necessary.
	WriteINIStr "$LOCALAPPDATA\MethodScript\desktop.ini" ".ShellClassInfo" "IconResource" "$LOCALAPPDATA\MethodScript\icon.ico,0"
	WriteINIStr "$LOCALAPPDATA\MethodScript\desktop.ini" "ViewState" "FolderType" "Documents"
	SetFileAttributes "$LOCALAPPDATA\MethodScript\desktop.ini" HIDDEN|SYSTEM
SectionEnd

Section DoConfigureUninstaller
	WriteUninstaller "$LOCALAPPDATA\MethodScript\uninstall.exe"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" "DisplayName" "MethodScript"
	WriteRegExpandStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
		"UninstallString" "$\"$LOCALAPPDATA\MethodScript\uninstall.exe$\""
	WriteRegExpandStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
		"InstallLocation" "$\"$LOCALAPPDATA\MethodScript$\""
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
		"DisplayIcon" "$\"$LOCALAPPDATA\MethodScript\icon.ico$\""
	; TODO Once a silent uninstall is ready
	;WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
	;	"QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
		"NoRepair" "1"
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" \
		"NoModify" "1"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" "Publisher" "MethodScript Contributors"
	${GetSize} "$LOCALAPPDATA\MethodScript" "/S=OK" $0 $1 $2
	IntFmt $0 "0x%08X" $0
	WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" "EstimatedSize" "$0"
	nsExec::ExecToStack "java -jar $LOCALAPPDATA\MethodScript\MethodScript.jar eval $\"engine_build_date()$\""
	Pop $0 ; Return
	Pop $1 ; Output
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript" "DisplayVersion" "$1"

	DetailPrint "Cmdline MethodScript is now installed! You may need to reboot your computer to use it from the shell."
	DetailPrint "Try running `mscript` or `mscript -- help`."
SectionEnd

Section un.DoMethodScriptUninstall
	ExecWait "java -jar $LOCALAPPDATA\MethodScript\MethodScript.jar uninstall-cmdline"
SectionEnd

Section un.DoPreferencesUninstall
	MessageBox MB_YESNO "Would you like to remove your preferences and other files located at $LOCALAPPDATA\MethodScript?" IDNO DoPreferencesUninstallEnd
	RMDir /R /REBOOTOK "$LOCALAPPDATA\MethodScript"
	DoPreferencesUninstallEnd:
SectionEnd

Section un.DoJavaUninstall
	MessageBox MB_ICONINFORMATION "If you wish to uninstall Java as well, please find the uninstaller in Add/Remove Programs."
SectionEnd

Section un.DoUninstallerUninstall
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MethodScript"
SectionEnd

Function _FindJava
	ExecWait "java -version" $0
	${if} $0 == "0"
		StrCpy $JavaInstallationPath "java"
	${else}
		StrCpy $JavaInstallationPath ""
	${endif}
FunctionEnd
