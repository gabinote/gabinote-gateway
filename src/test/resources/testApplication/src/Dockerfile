### 🛠 Build Stage ###
FROM python:3.13-slim AS builder
WORKDIR /app

COPY requirements.txt .

RUN apt-get update \
  && apt-get install -y --no-install-recommends gcc \
  && pip wheel --no-cache-dir --wheel-dir /wheels -r requirements.txt \
  && apt-get purge -y --auto-remove gcc \
  && rm -rf /var/lib/apt/lists/*

FROM python:3.13-slim AS runner
WORKDIR /app

COPY --from=builder /wheels /wheels
RUN pip install --no-cache-dir /wheels/*

COPY main.py requirements.txt ./

# 비루트 사용자 설정 (보안 강화)
RUN addgroup --system appgrp && adduser --system --ingroup appgrp appusr \
  && chown -R appusr:appgrp /app
USER appusr

ENV UVICORN_HOST=0.0.0.0
ENV UVICORN_PORT=8000
ENV APP_NAME=None
EXPOSE ${UVICORN_PORT}

CMD ["sh", "-c", "uvicorn main:app --host $UVICORN_HOST --port $UVICORN_PORT"]