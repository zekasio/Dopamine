import re

# Fix ModeratorDashboardScreen.kt
with open('app/src/main/java/com/dopamine/app/ui/screens/ModeratorDashboardScreen.kt', 'r') as f:
    mod_code = f.read()

# Fix mutableIntStateOf -> mutableStateOf
mod_code = mod_code.replace('var selectedTab by remember { mutableIntStateOf(0) }', 'var selectedTab by remember { mutableStateOf(0) }')

# Fix Icons missing imports by using fully qualified names in LiquidGlassBottomNav call
mod_code = mod_code.replace('Icons.Default.List', 'androidx.compose.material.icons.filled.List')
mod_code = mod_code.replace('Icons.Default.People', 'androidx.compose.material.icons.filled.Person')

# Add missing closing brace before the private functions
# We need to find: "@Composable\nprivate fun ReportItemCard(" and insert a "}\n" right before it, 
# IF it's currently unclosed. Wait, the `if (isRejectOpen)` block ends right before it.
# Let's just find "    }\n}\n\n@Composable\nprivate fun ReportItemCard"
# But currently it might be "    }\n}\n\n@Composable\nprivate fun ReportItemCard" 
# Actually, let's just count braces.
