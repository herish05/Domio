package com.domio.app.core.domain

import java.util.concurrent.TimeUnit

enum class WarrantyStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    NONE
}

data class AssetCategoryInfo(
    val name: String,
    val emoji: String,
    val subcategories: List<String>
)

object AssetCategories {

    val CATEGORIES = listOf(
        AssetCategoryInfo(
            name = "Vehicles",
            emoji = "🚗",
            subcategories = listOf("Cars", "Motorcycles", "Scooters", "Bicycles", "Commercial Vehicles", "Other Vehicle")
        ),
        AssetCategoryInfo(
            name = "Electronics",
            emoji = "📺",
            subcategories = listOf("TVs", "Cameras", "Audio & Speakers", "Gaming Consoles", "Smart Home Devices", "Other Electronics")
        ),
        AssetCategoryInfo(
            name = "Home Appliances",
            emoji = "❄️",
            subcategories = listOf("Refrigerator", "Washing Machine", "Air Conditioner", "Dishwasher", "Vacuum Cleaner", "Water Heater", "Other Appliance")
        ),
        AssetCategoryInfo(
            name = "Kitchen Appliances",
            emoji = "🍳",
            subcategories = listOf("Microwave", "Air Fryer", "Blender & Mixer", "Coffee Maker", "Toaster", "Other Kitchen Appliance")
        ),
        AssetCategoryInfo(
            name = "Computers",
            emoji = "💻",
            subcategories = listOf("Laptops", "Desktops", "Monitors", "Printers", "Storage & Hard Drives", "Computer Accessories")
        ),
        AssetCategoryInfo(
            name = "Mobile Devices",
            emoji = "📱",
            subcategories = listOf("Phones", "Tablets", "E-Readers", "Mobile Accessories")
        ),
        AssetCategoryInfo(
            name = "Wearables",
            emoji = "⌚",
            subcategories = listOf("Smartwatches", "Fitness Bands", "Wireless Earbuds", "Headphones")
        ),
        AssetCategoryInfo(
            name = "Furniture",
            emoji = "🪑",
            subcategories = listOf("Sofa & Seating", "Tables & Desks", "Beds & Mattresses", "Chairs", "Cabinets & Storage")
        ),
        AssetCategoryInfo(
            name = "Tools & Equipment",
            emoji = "🛠️",
            subcategories = listOf("Power Tools", "Hand Tools", "Lawn & Garden Tools", "Ladders", "Hardware")
        ),
        AssetCategoryInfo(
            name = "Sports & Outdoor",
            emoji = "⚽",
            subcategories = listOf("Gym Equipment", "Bicycles & Gear", "Camping Gear", "Sports Goods")
        ),
        AssetCategoryInfo(
            name = "Office & Business",
            emoji = "🏢",
            subcategories = listOf("Office Chairs", "Desks", "Paper Shredders", "Projectors")
        ),
        AssetCategoryInfo(
            name = "Other Belongings",
            emoji = "📦",
            subcategories = listOf("General", "Personal Items", "Valuables")
        )
    )

    fun getCategoryInfo(categoryName: String?): AssetCategoryInfo {
        if (categoryName.isNullOrBlank()) return CATEGORIES.last()
        return CATEGORIES.firstOrNull { it.name.equals(categoryName, ignoreCase = true) }
            ?: CATEGORIES.last()
    }

    fun getCategoryEmoji(categoryName: String?): String {
        return getCategoryInfo(categoryName).emoji
    }

    fun calculateWarrantyStatus(endDateMillis: Long?): WarrantyStatus {
        if (endDateMillis == null || endDateMillis <= 0) return WarrantyStatus.NONE
        val now = System.currentTimeMillis()
        if (endDateMillis < now) return WarrantyStatus.EXPIRED

        val diffMillis = endDateMillis - now
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)

        return if (daysRemaining <= 30) {
            WarrantyStatus.EXPIRING_SOON
        } else {
            WarrantyStatus.ACTIVE
        }
    }

    fun getDaysRemaining(endDateMillis: Long?): Long? {
        if (endDateMillis == null || endDateMillis <= 0) return null
        val diff = endDateMillis - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(diff)
    }
}
