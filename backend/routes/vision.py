from fastapi import APIRouter, File, UploadFile, HTTPException

from app.services.vision_service import analyze_read_image


router = APIRouter(
    prefix="/vision",
    tags=["Vision"]
)


@router.post("/read")
async def read_image(
    image: UploadFile = File(...)
):
    try:

        if not image.content_type:
            raise HTTPException(
                status_code=400,
                detail="Image content type is missing."
            )

        if not image.content_type.startswith("image/"):
            raise HTTPException(
                status_code=400,
                detail="Only image files are allowed."
            )

        image_bytes = await image.read()

        if not image_bytes:
            raise HTTPException(
                status_code=400,
                detail="Uploaded image is empty."
            )

        summary = analyze_read_image(image_bytes)

        return {
            "summary": summary
        }

    except HTTPException:
        raise

    except Exception as exception:

        raise HTTPException(
            status_code=500,
            detail=f"Vision processing failed: {str(exception)}"
        )