#!/usr/bin/env python3
import os
import shutil
from PIL import Image

def main():
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    src_dir = os.path.join(base_dir, "assets-src")
    cf_jpg = os.path.join(src_dir, "cf.jpg")
    logo_png = os.path.join(src_dir, "itz0cat_logo_clean.png")

    if not os.path.exists(cf_jpg) or not os.path.exists(logo_png):
        raise FileNotFoundError(f"Missing source files in {src_dir}")

    # Target resource directories
    # 1. common/src/main/resources/assets/catcoinflip/
    # 2. versions/1.21.11/src/main/resources/assets/catcoinflip/
    # 3. versions/26.1.2/src/main/resources/assets/catcoinflip/
    res_dirs = [
        os.path.join(base_dir, "common", "src", "main", "resources", "assets", "catcoinflip"),
        os.path.join(base_dir, "versions", "1.21.11", "src", "main", "resources", "assets", "catcoinflip"),
        os.path.join(base_dir, "versions", "26.1.2", "src", "main", "resources", "assets", "catcoinflip")
    ]

    # Process Icon (128x128 nearest-neighbor, RGBA)
    with Image.open(cf_jpg) as img:
        img = img.convert("RGBA")
        w, h = img.size
        # Center-crop to square
        min_dim = min(w, h)
        left = (w - min_dim) // 2
        top = (h - min_dim) // 2
        right = left + min_dim
        bottom = top + min_dim
        cropped = img.crop((left, top, right, bottom))
        icon = cropped.resize((128, 128), resample=Image.Resampling.NEAREST)

    # Process Logo (copy unchanged)
    with Image.open(logo_png) as orig_logo:
        logo_orig_size = orig_logo.size

    for rd in res_dirs:
        os.makedirs(rd, exist_ok=True)
        gui_dir = os.path.join(rd, "textures", "gui")
        os.makedirs(gui_dir, exist_ok=True)

        icon_dest = os.path.join(rd, "icon.png")
        icon.save(icon_dest, format="PNG")

        logo_dest = os.path.join(gui_dir, "itz0cat_logo.png")
        shutil.copyfile(logo_png, logo_dest)

        # Verify sizes
        with Image.open(icon_dest) as c_icon:
            print(f"Asset generated: {icon_dest} -> {c_icon.size[0]}x{c_icon.size[1]} (mode={c_icon.mode})")
        with Image.open(logo_dest) as c_logo:
            print(f"Asset copied: {logo_dest} -> {c_logo.size[0]}x{c_logo.size[1]} (mode={c_logo.mode})")

    # Also keep a copy for root README
    readme_assets = os.path.join(base_dir, "assets")
    os.makedirs(readme_assets, exist_ok=True)
    shutil.copyfile(logo_png, os.path.join(readme_assets, "itz0cat_logo.png"))
    shutil.copyfile(os.path.join(res_dirs[0], "icon.png"), os.path.join(readme_assets, "icon.png"))

    print(f"\nSource logo size: {logo_orig_size[0]}x{logo_orig_size[1]}")
    print("Assets build complete.")

if __name__ == "__main__":
    main()
