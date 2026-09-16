import base64

from openai import OpenAI

from app.config import settings


client = OpenAI(
    api_key=settings.openai_api_key
)


def analyze_read_image(
    image_bytes: bytes
) -> str:

    image_base64 = base64.b64encode(
        image_bytes
    ).decode("utf-8")

    response = client.responses.create(
        model="gpt-5.6-luna",
        input=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "input_text",
                        "text": (
                            "You are the vision assistant for a blind user. "
                            "Analyze the provided image carefully.\n\n"
                            "If the image contains text, understand the text "
                            "and its meaning rather than merely listing OCR text.\n\n"
                            "Give a concise 2 to 3 sentence spoken summary "
                            "of the most important information.\n\n"
                            "Do not mention that you are an AI. "
                            "Do not describe irrelevant visual details."
                        ),
                    },
                    {
                        "type": "input_image",
                        "image_url": (
                            f"data:image/jpeg;base64,{image_base64}"
                        ),
                    },
                ],
            }
        ],
    )

    return response.output_text