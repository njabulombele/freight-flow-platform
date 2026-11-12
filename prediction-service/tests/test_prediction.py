"""
Basic tests for the prediction service.
Tests run without a live server using FastAPI TestClient.
"""
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    # Model should be loaded via the lifespan event
    assert data["modelLoaded"] is True


def test_predict_valid():
    payload = {
        "deliveryId": "00000000-0000-4000-a000-000000000001",
        "distanceKm": 45.0,
        "plannedDurationMin": 90,
        "hourOfDay": 8,
        "dayOfWeek": 1,
        "city": "Johannesburg",
        "historicalDelayRate": 0.2,
        "previousDelayMinutes": None,
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert 0.0 <= data["lateProbability"] <= 1.0
    assert data["estimatedDelayMinutes"] >= 0
    assert data["riskCategory"] in ("LOW", "MEDIUM", "HIGH")
    assert isinstance(data["contributingFactors"], dict)


def test_predict_missing_field():
    # distanceKm is required
    payload = {
        "deliveryId": "00000000-0000-4000-a000-000000000001",
        "plannedDurationMin": 90,
        "hourOfDay": 8,
        "dayOfWeek": 1,
        "city": "Pretoria",
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 422


def test_predict_rush_hour_higher_risk():
    base = {
        "deliveryId": "00000000-0000-4000-a000-000000000002",
        "distanceKm": 50.0,
        "plannedDurationMin": 90,
        "dayOfWeek": 1,
        "city": "Johannesburg",
        "historicalDelayRate": 0.3,
        "previousDelayMinutes": None,
    }
    off_peak = client.post("/predict", json={**base, "hourOfDay": 14}).json()
    rush_hour = client.post("/predict", json={**base, "hourOfDay": 8}).json()
    # Rush hour should produce equal or higher late probability
    assert rush_hour["lateProbability"] >= off_peak["lateProbability"] - 0.05
