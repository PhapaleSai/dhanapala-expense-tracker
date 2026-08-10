package com.phapalesai.dhanapala.data.parser

import com.phapalesai.dhanapala.data.local.Category
import com.phapalesai.dhanapala.data.local.TransactionType

/**
 * Merchant/keyword dictionary categorizer, the same approach apps like
 * Walnut/Fold Money/Money View use for SMS-based expense tracking — no ML,
 * just a keyword→category lookup that's easy to extend. The user can always
 * override the category by hand, so this only needs to be a reasonable
 * starting point, not perfect classification.
 */
object CategoryGuesser {

    private val foodKeywords = listOf(
        "swiggy", "zomato", "blinkit", "zepto", "instamart", "eatsure",
        "dominos", "mcdonald", "kfc", "burger king", "starbucks", "restaurant",
        "cafe", "dhaba", "hotel food", "food court"
    )

    private val fuelKeywords = listOf(
        "petrol", "diesel", "fuel", "hpcl", "iocl", "bpcl", "indianoil",
        "petroleum", "gas station", "fuel station"
    )

    private val billsKeywords = listOf(
        "electricity", "elec bill", "light bill", "power bill", "bses",
        "mseb", "water bill", "gas bill", "lpg", "broadband", "wifi bill",
        "dth", "recharge", "postpaid", "prepaid", "mobile bill", "airtel",
        "jio", "vodafone", "vi bill", "insurance premium", "emi"
    )

    private val shoppingKeywords = listOf(
        "amazon", "flipkart", "myntra", "ajio", "meesho", "nykaa",
        "big bazaar", "dmart", "reliance retail", "shopping"
    )

    private val entertainmentKeywords = listOf(
        "netflix", "prime video", "hotstar", "spotify", "bookmyshow",
        "pvr", "inox", "movie", "cinema", "gaana", "youtube premium"
    )

    private val travelKeywords = listOf(
        "uber", "ola", "rapido", "irctc", "indigo", "spicejet", "makemytrip",
        "goibibo", "redbus", "flight", "train ticket", "metro card", "cab fare"
    )

    fun guess(body: String, type: TransactionType): String {
        val lower = body.lowercase()
        return when {
            lower.contains("salary") -> Category.SALARY
            lower.contains("refund") || lower.contains("cashback") -> Category.REFUND
            lower.contains("atm") || lower.contains("withdraw") -> Category.CASH_WITHDRAWAL
            foodKeywords.any { lower.contains(it) } -> Category.FOOD
            fuelKeywords.any { lower.contains(it) } -> Category.FUEL
            billsKeywords.any { lower.contains(it) } -> Category.BILLS
            shoppingKeywords.any { lower.contains(it) } -> Category.SHOPPING
            entertainmentKeywords.any { lower.contains(it) } -> Category.ENTERTAINMENT
            travelKeywords.any { lower.contains(it) } -> Category.TRAVEL
            lower.contains("upi") -> Category.UPI
            type == TransactionType.CREDIT -> Category.OTHER
            else -> Category.UNCATEGORIZED
        }
    }

    /** True if the SMS mentions a food-delivery app specifically (used for money-saving nudges). */
    fun isFoodDelivery(body: String): Boolean {
        val lower = body.lowercase()
        return listOf("swiggy", "zomato", "blinkit", "zepto", "instamart", "eatsure").any { lower.contains(it) }
    }
}
