# Exposes the MethodScript cmdline executable as a powershell module.
# NOTE TO DEVELOPERS: If the code in this file changes, be sure to update 
# the version using symver principals in the Interpreter.java file.

<#
    .DESCRIPTION
    Executes a MethodScript source file, launches interpreter mode, or runs other command
    line utilities, depending on the arguments. 
    
    If the -Tool switch is set, then the 
    -Arguments are passed as is to the jar, which is used to access the cmdline
    tools.

    If no arguments are provided, MethodScript is started in interpreter mode, where you can
    run individual commands.

    Otherwise, the first argument is taken to be the path to a script file, and the remaining
    arguments are passed to the script.

    .EXAMPLE
    Invoke-MethodScript

    .EXAMPLE
    Invoke-MethodScript -Tool help

    .EXAMPLE
    Invoke-MethodScript methodscript.ms arg1
#>
function Invoke-MethodScript {
    param(
        [switch] $Tool
    )
    $JarLocation = (Get-ItemProperty -Path HKCU:\Software\MethodScript | Select-Object -Expand JarLocation)
    $JavaVersion = (Get-Command java).Version.Major

    if($JavaVersion -gt 8) {
        Add-Type -assembly "system.io.compression.filesystem"
        $zipfile = [io.compression.zipfile]::OpenRead($JarLocation)    
        $file = $zipfile.Entries | Where-Object { $_.FullName -eq "interpreter-helpers/modules" }
        $stream = $file.Open()
        $reader = New-Object IO.StreamReader($stream)
        $modules = $reader.ReadToEnd()
        $modules = $modules -replace '\r',''
        $modules = $modules -replace '(.*)\n','--add-opens $1=ALL_UNNAMED '
    } else {
        $modules = ""
    }

    if($ENV:DEBUG_MSCRIPT) {
        $debug = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9001"
    } else {
        $debug = ""
    }

    #Write-Host "modules: $modules"
    #Write-Host "JarLocation: $JarLocation"
    #Write-Host "All args: $($args)"
    if($Tool) {
        Invoke-Expression "java $modules $debug -jar $JarLocation $args"
    } elseif($args.Count -eq 0) {
        Invoke-Expression "java $modules $debug -jar $JarLocation interpreter --location----- `"$(Get-Location | Select-Object -Expand Path)`""
    } else {
        Invoke-Expression "java $modules $debug -jar $JarLocation cmdline $args"
    }
}

Export-ModuleMember -Function Invoke-MethodScript
