@ECHO OFF
REM This minimal file simply launches the powershell script, after checking for its existence first.

where /q powershell.exe
IF ERRORLEVEL 1 (
    ECHO In order to run this from the command line, powershell must be installed.
    EXIT /B
) ELSE (
    powershell.exe -NoLogo -NoProfile -NonInteractive -Command "Import-Module MethodScript; Invoke-MethodScript %*%"
)