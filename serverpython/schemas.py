from pydantic import BaseModel, Field
from typing import Optional

class ProductDto(BaseModel):
    id: Optional[int] = None
    barcode: str
    name: Optional[str] = None
    brand: Optional[str] = None
    model: Optional[str] = None
    category: Optional[str] = None
    description: Optional[str] = None
    quantity: Optional[str] = None
    imageUrl: Optional[str] = Field(None, alias="imageUrl")
    source: Optional[str] = None
    confidence: Optional[str] = None
    createdAt: Optional[str] = Field(None, alias="createdAt")
    updatedAt: Optional[str] = Field(None, alias="updatedAt")

    class Config:
        populate_by_name = True

class ProductResponse(BaseModel):
    success: bool
    found: bool
    product: Optional[ProductDto] = None
    barcode: Optional[str] = None
    reason: Optional[str] = None

class HealthResponse(BaseModel):
    success: bool
    service: str
    status: str
    framework: str
