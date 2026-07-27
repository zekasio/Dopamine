import pexpect
import sys
import time
import os

if os.path.exists("auth.txt"):
    os.remove("auth.txt")

child = pexpect.spawn("npx", ["-y", "firebase-tools@latest", "login", "--no-localhost"], encoding="utf-8")
child.logfile = sys.stdout

# Gemini prompt
child.expect("Enable Gemini in Firebase features\\? \\(Y/n\\)")
child.sendline("n")

# Collect info prompt
child.expect("Allow Firebase to collect CLI and Emulator Suite usage and error reporting")
child.sendline("n")

# URL prompt
child.expect("Enter authorization code:")

print("\n\nWAITING FOR auth.txt")
while not os.path.exists("auth.txt"):
    time.sleep(2)

with open("auth.txt", "r") as f:
    code = f.read().strip()

child.sendline(code)
child.expect(pexpect.EOF, timeout=120)
