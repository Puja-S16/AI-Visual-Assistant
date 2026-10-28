
from PIL import Image, ImageDraw
from pathlib import Path

root = Path("android-app/app/src/main/res")
logo = Image.open("app_logo.png").convert("RGBA")

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in sizes.items():
    output_dir = root / folder
    output_dir.mkdir(parents=True, exist_ok=True)

    # Resize logo to fit a square icon
    icon = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    resized = logo.copy()
    resized.thumbnail((size, size), Image.Resampling.LANCZOS)

    x = (size - resized.width) // 2
    y = (size - resized.height) // 2
    icon.alpha_composite(resized, (x, y))

    icon.save(output_dir / "ic_launcher.webp", "WEBP", quality=95)
    icon.save(output_dir / "ic_launcher_round.webp", "WEBP", quality=95)  

# Any-density fallback
output_dir = root / "mipmap-anydpi"
output_dir.mkdir(parents=True, exist_ok=True)

print("Launcher icon PNGs generated successfully.")
