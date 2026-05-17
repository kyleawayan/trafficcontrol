#!/usr/bin/env python3
"""One-shot asset converter: 1.12.2 Forge Traffic Control -> Fabric 1.20.4."""
import json, os, shutil

SRC = "src/main/resources/assets/trafficcontrol"
DST = "port/src/main/resources/assets/trafficcontrol"

BLOCKS = [
    "crossing_gate_base","stand","crossing_gate_gate","crossing_gate_lamps",
    "crossing_gate_pole","crossing_gate_crossbuck","safetran_type_3",
    "crossing_relay_se","crossing_relay_sw","crossing_relay_nw","crossing_relay_ne",
    "crossing_relay_top_sw","crossing_relay_top_se","crossing_relay_top_nw",
    "crossing_relay_top_ne","overhead_pole","overhead","overhead_lamps",
    "overhead_crossbuck","safetran_mechanical","sign","cone","channelizer","drum",
    "street_light_single","light_source","street_light_double","traffic_light",
    "traffic_light_control_box","wig_wag","vertical_wig_wag","shunt_border",
    "shunt_island","type_3_barrier","type_3_barrier_right","traffic_rail",
    "concrete_barrier","horizontal_pole","wch_bell","wch_mechanical_bell",
    "traffic_sensor_left","traffic_sensor_straight","street_sign","traffic_light_5",
    "traffic_light_5_upper","traffic_light_doghouse","traffic_light_1",
    "traffic_light_2","traffic_light_4","traffic_light_6","pedestrian_button",
    "traffic_sensor_right",
]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def fix_tex(value):
    """Rewrite 1.12 texture ids to the 1.20.4 directory convention."""
    if not isinstance(value, str):
        return value
    v = value.lower()
    if v in ("blocks/log_oak", "minecraft:blocks/log_oak"):
        return "minecraft:block/oak_log"
    return (v.replace("trafficcontrol:blocks/", "trafficcontrol:block/")
             .replace("trafficcontrol:items/", "trafficcontrol:item/"))

# --- copy sounds verbatim ------------------------------------------------
s = os.path.join(SRC, "sounds")
d = os.path.join(DST, "sounds")
if os.path.isdir(s):
    if os.path.isdir(d):
        shutil.rmtree(d)
    shutil.copytree(s, d)

# --- copy textures, renaming blocks/->block/ and items/->item/ -----------
tex_dst = os.path.join(DST, "textures")
if os.path.isdir(tex_dst):
    shutil.rmtree(tex_dst)
for sub in os.listdir(os.path.join(SRC, "textures")):
    src_sub = os.path.join(SRC, "textures", sub)
    dst_name = {"blocks": "block", "items": "item"}.get(sub, sub)
    if os.path.isdir(src_sub):
        shutil.copytree(src_sub, os.path.join(tex_dst, dst_name))
    else:
        os.makedirs(tex_dst, exist_ok=True)
        shutil.copy(src_sub, os.path.join(tex_dst, dst_name))

# Texture file names must be lowercase (resource-pack path requirement).
for root, _, files in os.walk(tex_dst):
    for fn in files:
        if fn != fn.lower():
            os.rename(os.path.join(root, fn), os.path.join(root, fn.lower()))
if os.path.exists(os.path.join(SRC, "sounds.json")):
    shutil.copy(os.path.join(SRC, "sounds.json"), os.path.join(DST, "sounds.json"))

# --- copy models verbatim, fixing concrete_barrier -----------------------
for sub in ("block", "item"):
    sd = os.path.join(SRC, "models", sub)
    dd = os.path.join(DST, "models", sub)
    os.makedirs(dd, exist_ok=True)
    for fn in os.listdir(sd):
        if not fn.endswith(".json"):
            continue
        model = json.load(open(os.path.join(sd, fn)))
        model.pop("credit", None)
        # Resource locations must be lowercase and use the 1.20.4 dir layout.
        if isinstance(model.get("textures"), dict):
            model["textures"] = {k: fix_tex(v) for k, v in model["textures"].items()}
        if isinstance(model.get("parent"), str):
            model["parent"] = fix_tex(model["parent"])
        if fn == "concrete_barrier.json":
            model.setdefault("textures", {})["color"] = "minecraft:block/white_concrete"
        w(os.path.join(dd, fn), model)

# --- blockstates ---------------------------------------------------------
bs_dir = os.path.join(SRC, "blockstates")
for name in BLOCKS:
    src_bs = json.load(open(os.path.join(bs_dir, name + ".json")))
    model = src_bs.get("defaults", {}).get("model", "trafficcontrol:" + name)
    base = model.split(":", 1)[1]
    w(os.path.join(DST, "blockstates", name + ".json"),
      {"variants": {"": {"model": "trafficcontrol:block/" + base}}})
    # block item model
    item_path = os.path.join(DST, "models", "item", name + ".json")
    if not os.path.exists(item_path):
        w(item_path, {"parent": "trafficcontrol:block/" + base})

print("converted", len(BLOCKS), "blocks; assets copied")
