$subagent_log = "C:\Users\shrey\.gemini\antigravity\brain\081a18b5-a177-4ccc-8497-c222de459f56\.system_generated\logs\transcript.jsonl"
$lines = Get-Content -Path $subagent_log
$prompt = ""

foreach ($line in $lines) {
    $json = $line | ConvertFrom-Json
    if ($json.type -eq 'USER_INPUT') {
        $prompt = $json.content
        break
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
    Write-Host "No user input found in subagent log"
}
