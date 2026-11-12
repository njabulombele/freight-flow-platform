"""
DelayPredictionModel

Trains a RandomForestClassifier (is_late) + RandomForestRegressor (delay minutes)
using synthetic historical delivery data seeded to resemble South African urban
logistics (Johannesburg, Pretoria, Rustenburg).

We generate synthetic training data here so the service starts up and returns
meaningful shaped predictions immediately. When you have real delivery history,
replace generate_training_data() with a database query or CSV import.

Model evaluation
----------------
The model is evaluated against a held-out test split (20 %) and baseline
(always-predict-majority-class). Performance is logged on startup so you can
see whether the model beats the baseline before claiming predictions are accurate.
"""

from __future__ import annotations

import logging
import os
from pathlib import Path
from typing import Dict, Tuple

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
from sklearn.metrics import (
    accuracy_score, f1_score, mean_absolute_error, roc_auc_score
)
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import LabelEncoder, StandardScaler

logger = logging.getLogger(__name__)

MODEL_DIR = Path(__file__).parent.parent / "models"
CLASSIFIER_PATH = MODEL_DIR / "delay_classifier.pkl"
REGRESSOR_PATH = MODEL_DIR / "delay_regressor.pkl"
FEATURES = [
    "distance_km",
    "planned_duration_min",
    "hour_of_day",
    "day_of_week",
    "is_weekend",
    "city_encoded",
    "historical_delay_rate",
    "previous_delay_minutes",
]

CITIES = ["Johannesburg", "Pretoria", "Rustenburg", "Midrand", "Centurion", "Sandton"]
CITY_ENCODER = LabelEncoder().fit(CITIES)


def _city_encode(city: str) -> int:
    try:
        return int(CITY_ENCODER.transform([city])[0])
    except ValueError:
        return 0


def generate_training_data(n: int = 5000, seed: int = 42) -> pd.DataFrame:
    """
    Synthetic dataset based on plausible South African urban delivery patterns.
    Replace with real data from your database as it accumulates.
    """
    rng = np.random.default_rng(seed)
    cities = rng.choice(CITIES, size=n)
    hour_of_day = rng.integers(6, 20, size=n)
    day_of_week = rng.integers(1, 8, size=n)
    distance_km = rng.gamma(shape=3.0, scale=15.0, size=n).clip(1, 300)
    planned_duration = (distance_km / 40 * 60 + rng.normal(10, 5, n)).clip(10, 480).astype(int)
    historical_delay_rate = rng.beta(2, 5, n)

    # Delay drivers: rush hour, Mondays, far Rustenburg routes, previous delays
    delay_prob = (
        0.15
        + 0.20 * ((hour_of_day >= 7) & (hour_of_day <= 9))
        + 0.15 * ((hour_of_day >= 16) & (hour_of_day <= 18))
        + 0.10 * (day_of_week == 1)
        + 0.08 * (cities == "Rustenburg")
        + 0.25 * historical_delay_rate
        + 0.05 * (distance_km > 100)
    ).clip(0, 1)
    is_late = rng.binomial(1, delay_prob).astype(bool)
    delay_minutes = np.where(is_late, rng.gamma(2, 20, n).astype(int).clip(5, 240), 0)

    return pd.DataFrame({
        "distance_km": distance_km,
        "planned_duration_min": planned_duration,
        "hour_of_day": hour_of_day,
        "day_of_week": day_of_week,
        "is_weekend": (day_of_week >= 6).astype(int),
        "city_encoded": [_city_encode(c) for c in cities],
        "historical_delay_rate": historical_delay_rate,
        "previous_delay_minutes": rng.integers(0, 60, n),
        "is_late": is_late,
        "delay_minutes": delay_minutes,
    })


def _evaluate(clf, reg, X_test, y_cls_test, y_reg_test) -> None:
    """Logs classifier and regressor performance vs. a majority-class baseline."""
    majority = int(y_cls_test.mode()[0])
    baseline_acc = (y_cls_test == majority).mean()

    y_pred_cls = clf.predict(X_test)
    y_pred_proba = clf.predict_proba(X_test)[:, 1]
    model_acc = accuracy_score(y_cls_test, y_pred_cls)
    model_f1 = f1_score(y_cls_test, y_pred_cls)
    model_auc = roc_auc_score(y_cls_test, y_pred_proba)

    y_pred_reg = reg.predict(X_test[y_cls_test == 1])
    baseline_mae = y_reg_test[y_cls_test == 1].mean()
    model_mae = mean_absolute_error(y_reg_test[y_cls_test == 1], y_pred_reg) if len(y_pred_reg) else float("nan")

    logger.info("=== Delay Classifier (test split 20%%) ===")
    logger.info("  Baseline accuracy (majority):  %.3f", baseline_acc)
    logger.info("  Model accuracy:                %.3f", model_acc)
    logger.info("  Model F1:                      %.3f", model_f1)
    logger.info("  Model ROC-AUC:                 %.3f", model_auc)
    logger.info("=== Delay Regressor (late deliveries only) ===")
    logger.info("  Baseline MAE (mean):           %.1f min", baseline_mae)
    logger.info("  Model MAE:                     %.1f min", model_mae)

    if model_acc <= baseline_acc:
        logger.warning("⚠  Classifier does not beat baseline. Do not claim predictions are accurate.")
    else:
        logger.info("✓  Classifier beats baseline.")


class DelayPredictionModel:
    """Encapsulates loading, training, and inference for the delay model."""

    def __init__(self):
        self._classifier: RandomForestClassifier | None = None
        self._regressor: RandomForestRegressor | None = None
        self._loaded = False

    def load_or_train(self) -> None:
        MODEL_DIR.mkdir(parents=True, exist_ok=True)
        if CLASSIFIER_PATH.exists() and REGRESSOR_PATH.exists():
            logger.info("Loading existing models from %s", MODEL_DIR)
            self._classifier = joblib.load(CLASSIFIER_PATH)
            self._regressor = joblib.load(REGRESSOR_PATH)
        else:
            logger.info("No saved models found — training on synthetic data")
            self._train()
        self._loaded = True

    def _train(self) -> None:
        df = generate_training_data()
        X = df[FEATURES]
        y_cls = df["is_late"]
        y_reg = df["delay_minutes"]

        X_train, X_test, y_cls_train, y_cls_test = train_test_split(
            X, y_cls, test_size=0.2, random_state=42, stratify=y_cls
        )
        _, _, y_reg_train, y_reg_test = train_test_split(
            X, y_reg, test_size=0.2, random_state=42
        )

        self._classifier = RandomForestClassifier(
            n_estimators=200, max_depth=10, class_weight="balanced", random_state=42, n_jobs=-1
        )
        self._classifier.fit(X_train, y_cls_train)

        # Regressor trained only on deliveries that were late
        late_mask = y_reg_train > 0
        self._regressor = RandomForestRegressor(
            n_estimators=200, max_depth=10, random_state=42, n_jobs=-1
        )
        self._regressor.fit(X_train[late_mask], y_reg_train[late_mask])

        _evaluate(self._classifier, self._regressor, X_test, y_cls_test, y_reg_test)

        joblib.dump(self._classifier, CLASSIFIER_PATH)
        joblib.dump(self._regressor, REGRESSOR_PATH)
        logger.info("Models saved to %s", MODEL_DIR)

    def predict(self, features: Dict) -> Tuple[float, int, str, Dict[str, float]]:
        """
        Returns (late_probability, estimated_delay_min, risk_category, contributing_factors).
        """
        if not self._loaded:
            raise RuntimeError("Model not loaded. Call load_or_train() first.")

        row = pd.DataFrame([{
            "distance_km": features["distanceKm"],
            "planned_duration_min": features["plannedDurationMin"],
            "hour_of_day": features["hourOfDay"],
            "day_of_week": features["dayOfWeek"],
            "is_weekend": 1 if features["dayOfWeek"] >= 6 else 0,
            "city_encoded": _city_encode(features.get("city", "")),
            "historical_delay_rate": features.get("historicalDelayRate", 0.0),
            "previous_delay_minutes": features.get("previousDelayMinutes") or 0,
        }])

        late_prob = float(self._classifier.predict_proba(row[FEATURES])[0][1])
        est_delay = int(self._regressor.predict(row[FEATURES])[0]) if late_prob >= 0.4 else 0

        if late_prob < 0.3:
            risk = "LOW"
        elif late_prob < 0.6:
            risk = "MEDIUM"
        else:
            risk = "HIGH"

        # Feature importances as contributing factors
        importances = self._classifier.feature_importances_
        factors = {FEATURES[i]: round(float(importances[i]), 4) for i in range(len(FEATURES))}

        return late_prob, est_delay, risk, factors

    @property
    def is_loaded(self) -> bool:
        return self._loaded


# Singleton instance used by the FastAPI app
prediction_model = DelayPredictionModel()
