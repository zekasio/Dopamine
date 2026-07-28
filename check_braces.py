with open("app/src/main/java/com/dopamine/app/ui/screens/UserDashboardScreen.kt") as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    # simple counting ignoring strings and comments
    # just a rough estimate
    clean_line = line.split("//")[0]
    # We should ignore quotes... this is just rough
    count += clean_line.count('{')
    count -= clean_line.count('}')
    if count == 0 and i > 75:
        print(f"Brace count reached 0 at line {i+1}: {line.strip()}")
        break
