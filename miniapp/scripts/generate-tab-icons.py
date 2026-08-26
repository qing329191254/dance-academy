"""Regenerate tab bar icons with solid background and exact tabBar colors."""

from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1] / 'static' / 'tab'
SIZE = 81
BG = (0x11, 0x11, 0x11, 255)
INACTIVE = (0x8A, 0x8A, 0x8A, 255)
ACTIVE = (0x8A, 0x74, 0xE5, 255)

PAIRS = [
    ('home', 'home-active'),
    ('book', 'book-active'),
    ('growth', 'growth-active'),
    ('mine', 'mine-active'),
]


def recolor(src_path: Path, dst_path: Path, color: tuple[int, int, int, int]) -> None:
    src = Image.open(src_path).convert('RGBA')
    out = Image.new('RGBA', (SIZE, SIZE), BG)
    pixels = src.load()
    dst = out.load()
    for y in range(SIZE):
        for x in range(SIZE):
            r, g, b, a = pixels[x, y]
            if a > 64:
                dst[x, y] = color
    out.save(dst_path, format='PNG')


def main() -> None:
    for inactive_name, active_name in PAIRS:
        inactive_src = ROOT / f'{inactive_name}.png'
        active_src = ROOT / f'{active_name}.png'
        recolor(inactive_src, inactive_src, INACTIVE)
        recolor(active_src, active_src, ACTIVE)
        print(f'updated {inactive_name}.png / {active_name}.png')


if __name__ == '__main__':
    main()
