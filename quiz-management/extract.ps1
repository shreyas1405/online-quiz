$lines = Get-Content -Path "C:\Users\shrey\.gemini\antigravity\brain\a7bd9af8-e5f6-4227-ae02-456e71da9e74\.system_generated\logs\transcript.jsonl"
$prompt = ""
foreach ($line in $lines) {
    if ($line -match '"Frontend Template Writer"') {
        $json = $line | ConvertFrom-Json
        if ($json.tool_calls -ne $null) {
            foreach ($call in $json.tool_calls) {
                if ($call.name -eq 'default_api:invoke_subagent') {
                    foreach ($sub in $call.arguments.Subagents) {
                        if ($sub.Role -eq 'Frontend Template Writer') {
                            $prompt = $sub.Prompt
                        }
                    }
                }
            }
        }
    }
}

if ($prompt) {
    $parts = $prompt -split '## FILE \d+:\s*'
    for ($i = 1; $i -lt $parts.Length; $i++) {
        $part = $parts[$i]
        $idx = $part.IndexOf("`n")
        if ($idx -lt 0) { continue }
        $filepath = $part.Substring(0, $idx).Trim()
        
        $remainder = $part.Substring($idx)
        $codeMatch = [regex]::Match($remainder, '(?s)```[a-zA-Z]*\r?\n(.*?)\r?\n```')
        if ($codeMatch.Success) {
            $code = $codeMatch.Groups[1].Value
            $dir = Split-Path $filepath
            if (-not (Test-Path $dir)) {
                New-Item -ItemType Directory -Force -Path $dir | Out-Null
            }
            [System.IO.File]::WriteAllText($filepath, $code, [System.Text.Encoding]::UTF8)
            Write-Host "Written: $filepath"
        }
    }
} else {
    Write-Host "No prompt found"
}
