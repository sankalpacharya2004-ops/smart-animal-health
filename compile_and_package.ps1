# compile_and_package.ps1
# Automates downloading servlet/database dependencies, compiling source files, and packaging into a WAR file.

$ErrorActionPreference = "Stop"

# Define directories
$workDir = Get-Location
$libDir = "$workDir\lib"
$buildDir = "$workDir\build_temp"
$classesDir = "$buildDir\WEB-INF\classes"
$webLibDir = "$buildDir\WEB-INF\lib"
$srcDir = "$workDir\src\main\java"
$webappDir = "$workDir\src\main\webapp"

Write-Output "=== Starting Build Process ==="

# 1. Create directory structure
Write-Output "Creating build directories..."
if (Test-Path $buildDir) { Remove-Item -Recurse -Force $buildDir }
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path $webLibDir | Out-Null
if (!(Test-Path $libDir)) { New-Item -ItemType Directory -Force -Path $libDir | Out-Null }

# 2. Download dependencies if not present
$dependencies = @(
    @{
        Url = "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar"
        Name = "javax.servlet-api-4.0.1.jar"
        IncludeInWar = $false # Provided by servlet container
    },
    @{
        Url = "https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar"
        Name = "mysql-connector-java-8.0.28.jar"
        IncludeInWar = $true # Needs to be packaged in WEB-INF/lib
    },
    @{
        Url = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar"
        Name = "gson-2.9.0.jar"
        IncludeInWar = $true # Needs to be packaged in WEB-INF/lib
    }
)

Write-Output "Checking dependencies..."
foreach ($dep in $dependencies) {
    $filePath = "$libDir\$($dep.Name)"
    if (!(Test-Path $filePath)) {
        Write-Output "Downloading $($dep.Name) from Maven Central..."
        Invoke-WebRequest -Uri $dep.Url -OutFile $filePath -UseBasicParsing
    } else {
        Write-Output "$($dep.Name) already exists."
    }
    
    if ($dep.IncludeInWar) {
        Copy-Item $filePath -Destination $webLibDir
    }
}

# 3. Find and compile all Java source files
Write-Output "Compiling Java source files..."
$javaFiles = Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
if ($javaFiles.Count -eq 0) {
    Write-Error "No Java source files found in $srcDir"
}

# Construct Classpath
$classpath = "$libDir\javax.servlet-api-4.0.1.jar;$libDir\mysql-connector-java-8.0.28.jar;$libDir\gson-2.9.0.jar"

# Run javac compilation
Write-Output "Executing compilation..."
& javac -d $classesDir -cp $classpath $javaFiles

# 4. Copy Webapp assets
Write-Output "Copying web assets..."
if (Test-Path $webappDir) {
    Copy-Item -Path "$webappDir\*" -Destination $buildDir -Recurse -Force
} else {
    Write-Error "Webapp assets directory $webappDir not found!"
}

# 5. Package as WAR
$warName = "smart-animal-health.war"
$warPath = "$workDir\$warName"
if (Test-Path $warPath) { Remove-Item $warPath }

Write-Output "Archiving into $warName..."
# Prefer 'jar' tool if available, fallback to Compress-Archive
if (Get-Command "jar" -ErrorAction SilentlyContinue) {
    # Run jar inside build temp to prevent path nesting
    Push-Location $buildDir
    & jar cvf "$warPath" *
    Pop-Location
} else {
    # Compress-Archive fallback
    # Create temporary zip, rename to .war
    $tempZip = "$workDir\temp.zip"
    if (Test-Path $tempZip) { Remove-Item $tempZip }
    
    # We must zip contents of buildDir, not the buildDir folder itself
    Push-Location $buildDir
    Compress-Archive -Path * -DestinationPath $tempZip
    Pop-Location
    
    Move-Item $tempZip $warPath -Force
}

# Clean up build temp
Write-Output "Cleaning up temporary files..."
Remove-Item -Recurse -Force $buildDir

Write-Output "=== Build Complete! WAR generated at $warPath ==="
