import sys

filename = "src/main/resources/static/poker.html"
with open(filename, "r") as f:
    lines = f.readlines()

# 0-indexed: line 10 is index 9.
css_lines = lines[9:524-1]
js_lines = lines[673:1219-1]

with open("src/main/resources/static/poker.css", "w") as f:
    f.writelines(css_lines)

with open("src/main/resources/static/poker.jvx", "w") as f:
    f.writelines(js_lines)

# Assemble new html
new_lines = lines[:9]  # 0 to 8
new_lines.append('    <link rel="stylesheet" href="poker.css">\n')
new_lines.extend(lines[524:673]) # 524 to 672
new_lines.append('        <script src="poker.jvx"></script>\n')
new_lines.extend(lines[1219:]) # 1219 to end

with open(filename, "w") as f:
    f.writelines(new_lines)
