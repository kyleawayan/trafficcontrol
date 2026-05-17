#!/usr/bin/env python3
"""Regenerate blockstates with 4-way facing variants for all non-animated blocks."""
import json, os

SRC_BS = "src/main/resources/assets/trafficcontrol/blockstates"
DST_BS = "port/src/main/resources/assets/trafficcontrol/blockstates"

# Blocks driven by a BlockEntityRenderer keep their 16-way rotation property.
BE_ROTATION = {"street_light_single", "street_light_double", "crossing_gate_gate", "wig_wag"}

ALL = [f[:-5] for f in os.listdir(DST_BS) if f.endswith(".json")]

for name in ALL:
    if name in BE_ROTATION:
        continue
    src = json.load(open(os.path.join(SRC_BS, name + ".json")))
    model = src.get("defaults", {}).get("model", "trafficcontrol:" + name)
    base = "trafficcontrol:block/" + model.split(":", 1)[1]
    variants = {
        "facing=north": {"model": base},
        "facing=east": {"model": base, "y": 90},
        "facing=south": {"model": base, "y": 180},
        "facing=west": {"model": base, "y": 270},
    }
    p = os.path.join(DST_BS, name + ".json")
    json.dump({"variants": variants}, open(p, "w"), indent=2)
    open(p, "a").write("\n")

print("added facing variants to", len(ALL) - len(BE_ROTATION), "blockstates")
