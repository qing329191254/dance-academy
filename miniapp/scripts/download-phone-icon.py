import re
import urllib.request
import ssl

import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.path import Path
from PIL import Image

OUT_PNG = r'D:\demo\dance-academy\miniapp\static\nav\phone.png'
OUT_SVG = r'D:\demo\dance-academy\miniapp\static\nav\phone.svg'

URL = 'https://cdn.jsdelivr.net/npm/lucide-static@0.460.0/icons/phone.svg'

ctx = ssl.create_default_context()
req = urllib.request.Request(URL, headers={'User-Agent': 'Mozilla/5.0'})
with urllib.request.urlopen(req, context=ctx, timeout=20) as resp:
    svg = resp.read().decode('utf-8')

with open(OUT_SVG, 'w', encoding='utf-8') as f:
    f.write(svg)

match = re.search(r'<path[^>]*d="([^"]+)"', svg)
if not match:
    raise SystemExit('path not found in svg')

path_d = match.group(1)

fig = plt.figure(figsize=(2, 2), dpi=96)
ax = fig.add_axes([0, 0, 1, 1])
ax.set_facecolor((0, 0, 0, 0))
fig.patch.set_alpha(0)

patch = mpatches.PathPatch(
    Path.from_svg(path_d),
    facecolor='none',
    edgecolor='white',
    linewidth=2.4,
    capstyle='round',
    joinstyle='round',
)
ax.add_patch(patch)
ax.set_xlim(0, 24)
ax.set_ylim(24, 0)
ax.set_aspect('equal')
ax.axis('off')

tmp = OUT_PNG.replace('.png', '-tmp.png')
fig.savefig(tmp, transparent=True, pad_inches=0.15)

img = Image.open(tmp).convert('RGBA')
img = img.resize((128, 128), Image.Resampling.LANCZOS)
img.save(OUT_PNG)
print('saved', OUT_PNG)
