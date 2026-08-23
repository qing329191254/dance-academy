from PIL import Image, ImageDraw

SCALE = 8
OUT = 96


def make_handset_layer():
    w, h = 28 * SCALE, 84 * SCALE
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    white = (255, 255, 255, 255)

    cx = w // 2
    bar_half = 5 * SCALE
    cap_half_w = 6 * SCALE
    cap_half_h = 11 * SCALE

    # Vertical handset: slim bar + elongated rounded caps
    draw.ellipse(
        (cx - cap_half_w, 0, cx + cap_half_w, cap_half_h * 2),
        fill=white,
    )
    draw.ellipse(
        (cx - cap_half_w, h - cap_half_h * 2, cx + cap_half_w, h),
        fill=white,
    )
    draw.rounded_rectangle(
        (cx - bar_half, cap_half_h - SCALE, cx + bar_half, h - cap_half_h + SCALE),
        radius=bar_half,
        fill=white,
    )

    return img.rotate(-45, resample=Image.Resampling.BICUBIC, expand=True)


def trim_alpha(img, pad=2):
    bbox = img.getbbox()
    if not bbox:
        return img
    return img.crop(
        (
            max(0, bbox[0] - pad),
            max(0, bbox[1] - pad),
            bbox[2] + pad,
            bbox[3] + pad,
        )
    )


def main():
    handset = trim_alpha(make_handset_layer())

    canvas = Image.new('RGBA', (OUT * SCALE, OUT * SCALE), (0, 0, 0, 0))
    ratio = 0.68
    target = int(OUT * SCALE * ratio)
    scale = target / max(handset.width, handset.height)
    nw = max(1, int(handset.width * scale))
    nh = max(1, int(handset.height * scale))
    resized = handset.resize((nw, nh), Image.Resampling.LANCZOS)

    ox = (canvas.width - nw) // 2
    oy = (canvas.height - nh) // 2
    canvas.paste(resized, (ox, oy), resized)

    final = canvas.resize((OUT, OUT), Image.Resampling.LANCZOS)
    out_path = r'D:\demo\dance-academy\miniapp\static\nav\phone.png'
    final.save(out_path)
    print('saved', out_path)


if __name__ == '__main__':
    main()
