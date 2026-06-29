import json
import re

transcript_file = r"C:\Users\shrey\.gemini\antigravity\brain\a7bd9af8-e5f6-4227-ae02-456e71da9e74\.system_generated\logs\transcript.jsonl"
prompt = ""

with open(transcript_file, "r", encoding="utf-8") as f:
    for line in f:
        if "Frontend Template Writer" in line and "invoke_subagent" in line:
            data = json.loads(line)
            tool_calls = data.get("tool_calls", [])
            for call in tool_calls:
                if call.get("name") == "default_api:invoke_subagent":
                    args = call.get("arguments", {})
                    subagents = args.get("Subagents", [])
                    for sub in subagents:
                        if sub.get("Role") == "Frontend Template Writer":
                            prompt = sub.get("Prompt", "")

if not prompt:
    print("Frontend Template Writer prompt not found!")
    exit(1)

# Extract FILE and CODE blocks
# The prompt is formatted like:
# ## FILE 1: C:\Users\shrey\...\pom.xml
# ```xml
# <code here>
# ```

blocks = re.split(r'## FILE \d+:\s*(.*?)\n', prompt)
# blocks[0] is everything before the first FILE
# blocks[1] is file path
# blocks[2] is the content block starting with ```ext

import os

for i in range(1, len(blocks), 2):
    filepath = blocks[i].strip()
    content_block = blocks[i+1]
    
    # Extract code between ``` and ```
    code_match = re.search(r'```[a-zA-Z]*\n(.*?)\n```', content_block, re.DOTALL)
    if code_match:
        code = code_match.group(1)
        
        # ensure dir exists
        os.makedirs(os.path.dirname(filepath), exist_ok=True)
        with open(filepath, "w", encoding="utf-8") as out_file:
            out_file.write(code)
        print(f"Written: {filepath}")
    else:
        print(f"Code block not found for {filepath}")

print("Done extracting frontend files.")
