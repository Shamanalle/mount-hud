# Pixel-art mod icon on a 32x32 grid: a horseshoe over a heart and a hunger drumstick, with a jump bar below, on a
# warm field-at-dusk background.
# Usage: python3 docs/make-icon.py icon.png common/src/main/resources/assets/mounthud/icon.png (needs Pillow).
# Writes the 512px store icon and the 128px icon packed into the mod.
import sys
from PIL import Image

N = 32
TOP, BOTTOM, BORDER = (74, 49, 30), (36, 26, 19), (110, 78, 48)
IRON, IRON_LIGHT, IRON_DARK = (196, 186, 172), (236, 228, 214), (120, 110, 100)
RED, RED_LIGHT, RED_DARK = (220, 40, 46), (255, 120, 120), (120, 14, 20)
MEAT, MEAT_LIGHT, MEAT_DARK, BONE = (186, 98, 52), (226, 146, 86), (118, 58, 30), (240, 232, 214)
BAR_BG, BAR_A, BAR_B = (40, 30, 22), (110, 196, 72), (240, 200, 70)

img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
px = img.load()

def inside(x, y, inset):
    r = 5 - inset
    lo, hi = inset, N - 1 - inset
    if not (lo <= x <= hi and lo <= y <= hi):
        return False
    cx = min(max(x, lo + r), hi - r)
    cy = min(max(y, lo + r), hi - r)
    return (x - cx) ** 2 + (y - cy) ** 2 <= r * r + 1

def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))

for y in range(N):
    for x in range(N):
        if inside(x, y, 0):
            px[x, y] = BORDER + (255,)
        if inside(x, y, 1):
            px[x, y] = mix(TOP, BOTTOM, y / (N - 1)) + (255,)

def put(x, y, c):
    px[x, y] = c + (255,)

def sprite(x0, y0, rows, colors):
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in colors:
                put(x0 + dx, y0 + dy, colors[ch])

# Horseshoe, opening down, with nail holes
shoe = [
    "........########........",
    "......############......",
    "....####LLLLLLLL####....",
    "...##LLL########LLL##...",
    "..##LL####....####LL##..",
    ".##L###..........###L##.",
    ".#L##..............##L#.",
    "##L#o..............o#L##",
    "#L##................##L#",
    "#L##................##L#",
    "#L#o................o#L#",
    "#L##................##L#",
    "#L##................##L#",
    "#D##................##D#",
    "DD##................##DD",
    "DDD..................DDD",
]
sprite(4, 3, shoe, {"#": IRON, "L": IRON_LIGHT, "D": IRON_DARK, "o": IRON_DARK})

# Heart (the HUD's shape) and a drumstick, under the arch
heart = [
    ".KK.KK.",
    "KrRKRRK",
    "KRRRRRK",
    "KRRRRRK",
    ".KRRRK.",
    "..KRK..",
    "...K...",
]
sprite(8, 11, heart, {"K": RED_DARK, "R": RED, "r": RED_LIGHT})
drum = [
    "...mMMm",
    "..mMlMMm",
    "..MlMMMd",
    "..MMMMMd",
    "...MMMd.",
    "..Bdd...",
    ".BB.....",
    "BB......",
]
sprite(16, 10, drum, {"m": MEAT_DARK, "M": MEAT, "l": MEAT_LIGHT, "d": MEAT_DARK, "B": BONE})

# Jump bar: dark frame, filled two thirds from green to gold
for x in range(5, 27):
    for y in range(22, 26):
        put(x, y, BAR_BG)
for x in range(6, 26):
    for y in range(23, 25):
        t = (x - 6) / 19
        if x <= 19:
            put(x, y, mix(BAR_A, BAR_B, t))
        else:
            put(x, y, (70, 56, 42))

big = img.resize((512, 512), Image.NEAREST)
big.save(sys.argv[1] if len(sys.argv) > 1 else "icon.png")
small = img.resize((128, 128), Image.NEAREST)
small.save(sys.argv[2] if len(sys.argv) > 2 else "icon-128.png")
