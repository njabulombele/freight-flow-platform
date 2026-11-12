FROM python:3.12-slim

WORKDIR /app

RUN addgroup --system freightflow && adduser --system --group freightflow

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app ./app

RUN mkdir -p /app/models && chown -R freightflow:freightflow /app/models

USER freightflow

EXPOSE 8000
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]
