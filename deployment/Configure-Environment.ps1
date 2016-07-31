Param(
    [Parameter(Mandatory=$true)]
	[string]$connectionString,

    [Parameter(Mandatory=$true)]
	[string]$handshakeKey,

    [Parameter(Mandatory=$true)]
	[string]$configTemplate,
    
    [Parameter(Mandatory=$true)]
	[string]$configFile,

	[int]$port=4567,
    
    [string]$url="http://localhost:4567/",
            
	[bool]$simulated
)

Import-Module $PSScriptRoot\Merge-Tokens.psm1

    $content=(Get-Content $configTemplate | Merge-Tokens -tokens @{'AzureStorageConnectionString'=$connectionString; 'Url'=$url; 'Port'=$port; 'HandshakeKey'=$handshakeKey});

if($simulated){
    Write-Output $content;
}
else {
    Out-File -FilePath $configFile -InputObject $content -Encoding "ascii"
}
