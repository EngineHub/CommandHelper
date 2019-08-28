@ECHO OFF
REM This minimal file simply launches the powershell script, after checking for its existence first.

where /q powershell.exe
IF ERRORLEVEL 1 (
    ECHO In order to run this from the command line, powershell must be installed.
    EXIT /B
) ELSE (
	REM This doesn't work on PowerShell 2.0 (and perhaps others before 5.0) because the module is not in PSModulePath
	REM It also may require setting the execution policy to Unrestricted first
    powershell.exe -NoLogo -NoProfile -NonInteractive -Command "Import-Module MethodScript; Invoke-MethodScript %*%"
)