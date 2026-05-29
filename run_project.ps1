# run_project.ps1
# Automates downloading Apache Tomcat, deploying the WAR file, and starting the server.

$ErrorActionPreference = "Stop"
$workDir = Get-Location
$tomcatDir = "$workDir\tomcat"
$tomcatZip = "$workDir\tomcat.zip"
$warPath = "$workDir\smart-animal-health.war"

# Auto-detect JAVA_HOME if not set
if (!$env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java") {
        $jdkPath = Get-ChildItem "C:\Program Files\Java" -Filter "jdk-*" | Select-Object -First 1
        if ($jdkPath) {
            $env:JAVA_HOME = $jdkPath.FullName
            Write-Output "Auto-detected JAVA_HOME at: $env:JAVA_HOME"
        }
    }
}

if (!$env:JAVA_HOME) {
    Write-Warning "JAVA_HOME environment variable is not defined. Tomcat startup might fail."
}

# 1. Check if WAR file exists, build if not
if (!(Test-Path $warPath)) {
    Write-Output "WAR file not found. Running compile_and_package.ps1 first..."
    & .\compile_and_package.ps1
}

# 2. Check if Tomcat folder exists
$tomcatHome = "$tomcatDir\apache-tomcat-9.0.89"
if (!(Test-Path $tomcatHome)) {
    Write-Output "Tomcat not found. Downloading Apache Tomcat 9.0.89..."
    $url = "https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.89/bin/apache-tomcat-9.0.89-windows-x64.zip"
    Invoke-WebRequest -Uri $url -OutFile $tomcatZip -UseBasicParsing
    
    Write-Output "Extracting Tomcat..."
    Expand-Archive -Path $tomcatZip -DestinationPath $tomcatDir
    Remove-Item $tomcatZip
} else {
    Write-Output "Tomcat is already installed."
}

# 3. Clean existing webapps inside Tomcat (remove default ROOT to deploy ours)
$webappsDir = "$tomcatHome\webapps"
Write-Output "Deploying application to Tomcat webapps as ROOT.war..."
if (Test-Path "$webappsDir\ROOT") {
    Remove-Item -Recurse -Force "$webappsDir\ROOT"
}
if (Test-Path "$webappsDir\ROOT.war") {
    Remove-Item -Force "$webappsDir\ROOT.war"
}

# Copy and rename our WAR file to ROOT.war so it runs at http://localhost:8080/
Copy-Item $warPath -Destination "$webappsDir\ROOT.war"

# 4. Start Tomcat in a new window (will inherit $env:JAVA_HOME)
Write-Output "Starting Apache Tomcat in a separate window..."
Start-Process -FilePath "$tomcatHome\bin\startup.bat" -WorkingDirectory "$tomcatHome\bin"

Write-Output "=========================================================="
Write-Output "Apache Tomcat is booting up!"
Write-Output "You can access the application at: http://localhost:8080/"
Write-Output "=========================================================="
