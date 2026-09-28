from pathlib import Path
import json
import datetime
import pandas as pd

excel_file = r"C:\Users\abrig\Documents\Coding_Practice\Python\Jerseys\NHLGamePredictions2526.xlsx"

excel_data = pd.read_excel(excel_file, sheet_name=None)

predictions = []


def clean(value):
    if pd.isna(value):
        return None

    if isinstance(value, pd.Timestamp):
        return value.isoformat()

    if isinstance(value, datetime.date):
        return f"{value:%Y-%m-%d}"

    if isinstance(value, (bool, int, float, str)):
        return str(value)

    else:
        raise ValueError(f"Clean fail {value=}, {type(value)=}")

    return value


for sheet_name, sheet_df in excel_data.items():
    sheet_dict = {"sheet_name": sheet_name}
#     print(f"{sheet_name=}")
    if sheet_name == "DataRegularSeason20242025":
        df = sheet_df.iloc[1:].reset_index(drop=True)
        df.columns = map(lambda v: str(v).strip(), sheet_df.iloc[0].values.tolist())
        df = df.reset_index(drop=True)
    data = []
    for _, row in df.iterrows():
#         print(f"{_=}")
#         print(f"cols={df.columns.tolist()}")
#         print(row)
        data.append({col: clean(row[col]) for col in df.columns.tolist() if col != "nan"})
    sheet_dict["sheet_data"] = data
    predictions.append(sheet_dict)

with open("predictions.json", "w", encoding="utf-8") as f:
    json.dump(
        predictions,
        f,
        indent=2,
        ensure_ascii=False,
        allow_nan=False
    )