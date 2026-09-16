from fastapi import FastAPI

from routes.vision import router as vision_router


app = FastAPI(
    title="AI Visual Assistant API",
    version="1.0.0"
)


app.include_router(vision_router)


@app.get("/")
def root():
    return {
        "message": "AI Visual Assistant API is running."
    }