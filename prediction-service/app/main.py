"""
FreightFlow Prediction Service

FastAPI microservice that serves delay predictions for active deliveries.
On startup it loads a trained RandomForest model (or trains one on synthetic data
if no saved model exists).
"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, status
from fastapi.responses import JSONResponse

from .model import prediction_model
from .schemas import HealthResponse, PredictionRequest, PredictionResponse

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Load (or train) the model once on startup."""
    logger.info("FreightFlow Prediction Service starting up …")
    prediction_model.load_or_train()
    logger.info("Model ready.")
    yield
    logger.info("Shutting down.")


app = FastAPI(
    title="FreightFlow Prediction Service",
    description="ML-powered delay prediction for logistics deliveries",
    version="0.1.0",
    lifespan=lifespan,
)


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health() -> HealthResponse:
    return HealthResponse(status="ok", modelLoaded=prediction_model.is_loaded)


@app.post("/predict", response_model=PredictionResponse, tags=["Prediction"])
async def predict(request: PredictionRequest) -> PredictionResponse:
    """
    Accepts delivery features and returns:
    - Probability of late delivery (0–1)
    - Estimated delay in minutes
    - Risk category: LOW | MEDIUM | HIGH
    - Feature importances as contributing factors
    """
    if not prediction_model.is_loaded:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Model is not loaded. Try again shortly.",
        )
    try:
        late_prob, est_delay, risk, factors = prediction_model.predict(request.model_dump())
        return PredictionResponse(
            deliveryId=request.deliveryId,
            lateProbability=round(late_prob, 4),
            estimatedDelayMinutes=est_delay,
            riskCategory=risk,
            contributingFactors=factors,
        )
    except Exception as exc:
        logger.exception("Prediction failed: %s", exc)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Prediction failed. Check service logs.",
        ) from exc


@app.exception_handler(Exception)
async def generic_handler(request, exc: Exception):
    logger.exception("Unhandled error: %s", exc)
    return JSONResponse(
        status_code=500,
        content={"detail": "An unexpected error occurred"},
    )
