<#
	NOTE: This is a PowerShell version of the equivalent Bash script. If you make modifications to this file,
	you MUST make similar modifications to the Bash equivalent.
#>

$ErrorActionPreference = "Stop"

# This is the checkout which we build against for the swagger-codegen project. This can be a tag or a commit (or technically a branch)
# but is meant to be relatively stable, and regardless only intentionally updated, so that builds are generally speaking reproducable.
$CheckoutId = "a68e47087060c6f246c35a213f8e47709a4f8ab2"

[bool] $SkipBuild = $false;
[bool] $SkipUpdate = $false;
if($args.Contains("--SkipBuild")) {
	$SkipBuild = $true;
}

if($args.Contains("--SkipUpdate")) {
	$SkipUpdate = $true;
}

function Status($Message) {
	Write-Host -ForegroundColor Green -Object $Message
}

function ErrorAndQuit($Message) {
	Write-Host -ForegroundColor Red -Object $Message
	exit
}

function New-Software($Name, $Exe, $Instructions) {
	return @{Name = $Name; Exe = $Exe; Instructions = $Instructions}
}

function Test-Software {
	$softwares = (New-Software -Name "Java" -Exe "java.exe" -Instructions "https://adoptopenjdk.net/"), `
		(New-Software -Name "Git" -Exe "git.exe" -Instructions "https://git-scm.com/download/win"), `
		(New-Software -Name "Maven" -Exe "mvn.exe" -Instructions "https://maven.apache.org/guides/getting-started/windows-prerequisites.html")


	$notFound = @()

	foreach($software in $softwares) {
		if($null -eq (Get-Command -Name $software.Exe -ErrorAction SilentlyContinue)) {
			$notFound += $software
		}
	}

	if($notFound.Count -gt 0) {
		Write-Host "One or more required software packages are not installed, and must be installed and available on your path before continuing."
		Write-Host "Please install the following software before trying again:"
		foreach($software in $notFound) {
			Write-Host "$($software.Name) - $($software.Instructions)"
		}
		return $false
	}

	return $true
}

function Test-ProjectFile($Path) {
	$Exists = Test-Path $Path -PathType Container
	if(-not $exists) {
		New-Item -Path $Path -ItemType Directory -Confirm
		$Exists = Test-Path $Path -PathType Container
		if(-not $Exists) {
			exit
		}
	} else {
		Write-Host "Using $Path"
	}
}

function CloneOrCheckout($repo, $localPath, $branch) {
	$directoryInfo = Get-ChildItem $localPath | Measure-Object
	if($directoryInfo.count -eq 0) {
		#Clone
		Status "Cloning $repo"
		git clone $repo $localPath "--branch" $branch
	} else {
		Status "Updating $localPath"
		#Pull
		$status = (git status --porcelain)
		# Write-Host $status.GetType()
		if($null -eq $status) {
			# :thumbsup:
			git fetch
			git checkout $branch
		} else {
			# :thumbsdown:
			ErrorAndQuit "$localPath has local changes, refusing to continue. Please stash or otherwise revert your changes before continuing."
		}
	}
}

function CheckFileOrExit($Path) {
	$Exists = Test-Path $Path
	if(-not $Exists) {
		ErrorAndQuit "Could not find $Path, exiting"
	}
}

function Start-Main([string] $SwaggerGenerator,
		[string] $JavaRepo,
		[string] $NodeRepo,
		[string] $InputSpec,
		[string] $TemplateDir) {
	Status "Ensuring prerequisite software is installed"
	if((Test-Software) -eq $false) {
		return
	}

	Status "Creating directories if needed"
	Test-ProjectFile -Path $SwaggerGenerator
	Test-ProjectFile -Path $JavaRepo
	Test-ProjectFile -Path $NodeRepo

	$SwaggerGenerator = (Resolve-Path $SwaggerGenerator).Path
	$JavaRepo = (Resolve-Path $JavaRepo).Path
	$NodeRepo = (Resolve-Path $NodeRepo).Path
	$MethodScriptDirectory = (Resolve-Path "$PSScriptRoot\..\..").Path

	Set-Location $SwaggerGenerator
	if(-not $SkipUpdate) {
		Status "Getting Swagger Codegen"
		# Checkout SwaggerGenerator
		CloneOrCheckout -repo "https://github.com/swagger-api/swagger-codegen" -localPath $SwaggerGenerator -branch $CheckoutId
	}

	if(-not $SkipBuild) {
		Status "Building Swagger Codegen"
		mvn "-Dmaven.test.skip=true" clean package
	}

	$GeneratorJar = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath(".\modules\swagger-codegen-cli\target\swagger-codegen-cli.jar")
	CheckFileOrExit $GeneratorJar

	Status "Using $InputSpec as the input spec"

	Status "Generating Java Client"
	java -jar $GeneratorJar generate -i $InputSpec -l java -o $JavaRepo

	# Copy in src/main/java
	Status "Moving API files into MethodScript"
	Set-Location $MethodScriptDirectory
	New-Item -Path "$MethodScriptDirectory\src\main\java\io\swagger\client" -ItemType Directory -Force | Out-Null
	Remove-Item "$MethodScriptDirectory\src\main\java\io\swagger\client" -Force -Recurse
	New-Item -Path "$MethodScriptDirectory\src\main\java\io\swagger\client" -ItemType Directory -Force | Out-Null
	Copy-Item -Recurse -Path "$JavaRepo\src\main\java\io\swagger\client\*" -Destination "$MethodScriptDirectory\src\main\java\io\swagger\client"

	Status "Generating Node Server"
	java -jar $GeneratorJar generate -i $InputSpec -l nodejs-server -o $NodeRepo "--disable-examples" "--template-dir" $TemplateDir

	Status "Done!"
}

$SwaggerGeneratorDirectory = "$PSScriptRoot\..\..\..\SwaggerGenerator"
$JavaRepoDirectory = "$PSScriptRoot\..\..\..\MethodScriptAppsJavaApi"
$NodeRepoDirectory = "$PSScriptRoot\..\..\..\apps.methodscript.com"
$InputSpec = (Resolve-Path "$PSScriptRoot\..\..\src\main\resources\apps.methodscript.com.yaml").Path
$TemplateDir = Join-Path -Path $NodeRepoDirectory -ChildPath 'mustacheTemplates'

Start-Main -SwaggerGenerator $SwaggerGeneratorDirectory `
	-JavaRepo $JavaRepoDirectory `
	-NodeRepo $NodeRepoDirectory `
	-InputSpec $InputSpec `
	-TemplateDir $TemplateDir