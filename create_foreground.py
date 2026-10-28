
from PIL import Image
from pathlib import Path

logo_path = Path("android-app/app/src/main/res/drawable/app_logo.png")
output_path = Path("android-app/app/src/main/res/drawable/app_logo_foreground.png")

logo = Image.open(logo_path).convert("RGBA")
logo.thumbnail((432, 432), Image.Resampling.LANCZOS)

canvas = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
x = (432 - logo.width) // 2
y = (432 - logo.height) // 2
canvas.alpha_composite(logo, (x, y))

canvas.save(output_path)
print("Launcher foreground image created successfully!")
