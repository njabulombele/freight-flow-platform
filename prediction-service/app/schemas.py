"""
Request and response models for the prediction API.
"""
from __future__ import annotations
from pydantic import BaseModel, Field
from typing import Dict, Optional
import uuid


class PredictionRequest(BaseModel):
    deliveryId: uuid.UUID
    distanceKm: float = Field(..., gt=0)
    plannedDurationMin: int = Field(..., gt=0)
    hourOfDay: int = Field(..., ge=0, le=23)
    dayOfWeek: int = Field(..., ge=1, le=7)
    city: str
    historicalDelayRate: float = Field(default=0.0, ge=0.0, le=1.0)
    previousDelayMinutes: Optional[int] = None


class PredictionResponse(BaseModel):
    deliveryId: uuid.UUID
    lateProbability: float = Field(..., ge=0.0, le=1.0)
    estimatedDelayMinutes: int
    riskCategory: str                      # LOW | MEDIUM | HIGH
    contributingFactors: Dict[str, float]  # feature -> importance weight


class HealthResponse(BaseModel):
    status: str
    modelLoaded: bool
