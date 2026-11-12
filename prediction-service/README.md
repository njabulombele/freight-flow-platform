# FreightFlow Prediction Service

Python + FastAPI + scikit-learn delay prediction service.

## Running locally

```bash
cd prediction-service
python -m venv .venv
.venv\Scripts\activate      # Windows
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

API docs at `http://localhost:8000/docs`

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /health | Service and model status |
| POST | /predict | Predict delivery delay |

## Model

A `RandomForestClassifier` predicts whether a delivery will be late, and a
`RandomForestRegressor` estimates how many minutes late.

Features used:
- `distance_km` — planned route distance
- `planned_duration_min` — planned trip duration
- `hour_of_day` — departure hour (rush hour signal)
- `day_of_week` — 1=Monday … 7=Sunday
- `is_weekend` — derived binary flag
- `city_encoded` — delivery city (label-encoded)
- `historical_delay_rate` — tenant-level historical delay proportion
- `previous_delay_minutes` — delay on the previous leg if available

On first startup the service trains on synthetic data and saves the models to
`models/`. When real delivery history is available, replace `generate_training_data()`
in `model.py` with a database query.

## Evaluation

Model performance is logged at startup against a majority-class baseline.
Do not claim predictions are accurate until the model beats the baseline on
real data.

## Running tests

```bash
pytest tests/ -v
```
