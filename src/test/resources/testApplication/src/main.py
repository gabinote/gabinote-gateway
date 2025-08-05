from typing import Annotated, List
import os
from fastapi import FastAPI, Query, Header
from fastapi.responses import JSONResponse

app = FastAPI()
API_NAME = os.getenv("APP_NAME", "my_api")

@app.get("/", response_class=JSONResponse)
async def default():
    return {"name": API_NAME }

@app.get("/query", response_class=JSONResponse)
async def query(name: int = Query(default=None)):
    return {
        "message": name,
        "name": API_NAME
    }

@app.get("/path/{name}", response_class=JSONResponse)
async def query(name: str):
    return {"message": name, "name": API_NAME }

@app.get("/need-auth", response_class=JSONResponse)
async def need_auth(
        x_token_sub: Annotated[str | None, Header()] = None,
        x_token_role: Annotated[List[str] | None, Header()] = None,
):
    return {
        "sub": x_token_sub,
        "role": x_token_role,
        "name": API_NAME
    }

@app.get("/optional-auth", response_class=JSONResponse)
async def optional_auth(
        x_token_sub: Annotated[str | None, Header()] = None,
        x_token_role: Annotated[List[str] | None, Header()] = None,
):
    return {
        "sub": x_token_sub,
        "role": x_token_role,
        "name": API_NAME
    }