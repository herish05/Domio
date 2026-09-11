import os
from dotenv import load_dotenv

load_dotenv()

APP_NAME = os.getenv("APP_NAME", "DomioApp")
APP_VERSION = os.getenv("APP_VERSION", "1.0.0")
CONTACT_EMAIL = os.getenv("CONTACT_EMAIL", "support@domio.app")
PORT = int(os.getenv("PORT", 5000))
HOST = os.getenv("HOST", "0.0.0.0")
