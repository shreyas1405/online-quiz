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

Write-Host ("Prompt length: " + $prompt.Length)
Write-Host ("Matches: " + ($prompt -match '## FILE 1:'))

$parts = $prompt -split '## FILE \d+:\s*'
Write-Host ("Parts: " + $parts.Length)

if ($parts.Length -gt 1) {
    $part = $parts[1]
    Write-Host "Part 1 preview: " $part.Substring(0, [math]::Min(100, $part.Length))
    
    $idx = $part.IndexOf("`n")
    Write-Host "Index of newline: " $idx
    
    $filepath = $part.Substring(0, $idx).Trim()
    Write-Host "Filepath: $filepath"
    
    $remainder = $part.Substring($idx)
    $codeMatch = [regex]::Match($remainder, '(?s)```[a-zA-Z]*\r?\n(.*?)\r?\n```')
    Write-Host ("Regex matched: " + $codeMatch.Success)
}
