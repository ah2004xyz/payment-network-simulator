param(
    [switch]$SkipInfrastructureCheck
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$logDirectory = Join-Path $projectRoot '.logs'
New-Item -ItemType Directory -Force -Path $logDirectory | Out-Null

function Test-PortListening {
    param([int]$Port)

    return $null -ne (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue)
}

function Start-ServiceModule {
    param(
        [string]$Name,
        [string]$Module,
        [int]$Port,
        [string]$JavaHome
    )

    if (Test-PortListening -Port $Port) {
        Write-Host "$Name is already listening on port $Port."
        return
    }

    $logFile = Join-Path $logDirectory "$Name.log"
    $command = @"
Set-Location -LiteralPath '$projectRoot'
`$env:JAVA_HOME = '$JavaHome'
mvn -pl $Module spring-boot:run *>> '$logFile'
"@

    $startParameters = @{
        FilePath = 'powershell.exe'
        ArgumentList = '-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $command
        WindowStyle = 'Hidden'
        PassThru = $true
    }
    $process = Start-Process @startParameters

    Write-Host "Started $Name on port $Port. Process ID: $($process.Id)"
    Write-Host "Log file: $logFile"
}

if (-not $SkipInfrastructureCheck) {
    $requiredPorts = 3306, 27017, 5672
    $unavailablePorts = $requiredPorts | Where-Object { -not (Test-PortListening -Port $_) }

    if ($unavailablePorts) {
        throw "Required infrastructure is not listening on port(s): $($unavailablePorts -join ', '). Start Docker first, or run this script with -SkipInfrastructureCheck."
    }
}

$javaExecutable = (Get-Command java -ErrorAction Stop).Source
$javaHome = Split-Path -Parent (Split-Path -Parent $javaExecutable)

Start-ServiceModule -Name 'bank-a' -Module 'BankA' -Port 8080 -JavaHome $javaHome
Start-ServiceModule -Name 'bank-b' -Module 'BankB' -Port 8081 -JavaHome $javaHome
Start-ServiceModule -Name 'shaparak' -Module 'Shaparak' -Port 8082 -JavaHome $javaHome
Start-ServiceModule -Name 'psp' -Module 'PSP' -Port 8084 -JavaHome $javaHome

Write-Host ''
Write-Host "Use Get-Content -Wait '$logDirectory\\bank-b.log' to follow a service log."
