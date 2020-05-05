:; "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )/scripts/bash/update-apps-api" $@; exit $?;
@ECHO OFF
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0\scripts\windows\update-apps-api.ps1" %*
