import os, subprocess, shutil, tempfile

EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
base    = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res")
designs = os.path.dirname(__file__)
full_svg    = os.path.join(designs, "concept-2-bold-modern.svg")
inset_fg_svg = os.path.join(designs, "concept-2-foreground-inset.svg")

# Mipmap icons use full composite; adaptive foreground uses the inset card design.
mipmap_sizes = {
    "mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192,
}
foreground_sizes = {
    "mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432,
}

def render(svg_path, out_path, size):
    svg_url = "file:///" + os.path.abspath(svg_path).replace("\\", "/")
    html = f"""<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>*{{margin:0;padding:0}}html,body{{width:{size}px;height:{size}px;overflow:hidden}}
img{{width:{size}px;height:{size}px;display:block}}</style>
</head><body><img src="{svg_url}"></body></html>"""

    tmpdir = tempfile.mkdtemp()
    html_file = os.path.join(tmpdir, "render.html")
    png_file  = os.path.join(tmpdir, "screenshot.png")

    with open(html_file, "w") as f:
        f.write(html)

    html_url = "file:///" + html_file.replace("\\", "/")
    subprocess.run([
        EDGE, "--headless=new",
        f"--screenshot={png_file}",
        f"--window-size={size},{size}",
        "--hide-scrollbars", "--disable-gpu",
        "--force-device-scale-factor=1",
        html_url
    ], capture_output=True, timeout=30, check=True)

    shutil.copy(png_file, out_path)
    shutil.rmtree(tmpdir)
    print(f"  {size}x{size}  ->  {os.path.relpath(out_path)}")

for density, size in mipmap_sizes.items():
    for name in ("ic_launcher.png", "ic_launcher_round.png"):
        render(full_svg, os.path.join(base, f"mipmap-{density}", name), size)

for density, size in foreground_sizes.items():
    render(full_svg, os.path.join(base, f"drawable-{density}", "ic_launcher_foreground.png"), size)

print("Done.")
