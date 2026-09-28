from pathlib import Path
import json
import pandas as pd

excel_file = r"C:\Users\abrig\Documents\Coding_Practice\Python\Jerseys\Jerseys_20260903.xlsx"
image_root = Path(r"D:\NHL jerseys\Jerseys 20250927")

df = pd.read_excel(excel_file)

collection = []


def clean(value):
    if pd.isna(value):
        return None

    if isinstance(value, pd.Timestamp):
        return value.isoformat()

    return value


for _, row in df.iterrows():
    jersey_id = int(row["ID"])

    jersey_folder = image_root / f"J_{jersey_id:03d}"

    images = []

    if jersey_folder.exists():
        images = sorted(
            file.name
            for file in jersey_folder.iterdir()
            if file.suffix.lower() in {".jpg", ".jpeg", ".png", ".webp"}
        )

    data = {col: clean(row[col]) for col in df.columns}
    data.update({"images": images})
    collection.append(data)
    collection.sort(key=lambda d: d["ID"])

with open("collections.json", "w", encoding="utf-8") as f:
    json.dump(
        collection,
        f,
        indent=2,
        ensure_ascii=False,
        allow_nan=False
    )