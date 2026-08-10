package com.phapalesai.dhanapala.domain

import kotlin.random.Random

/** Practical, non-judgmental frugal tips — rotates like a "tip of the day," food-delivery-aware. */
object MoneySavingTips {

    private val general = listOf(
        "Instead of a ₹200 restaurant lunch, try 2 Maggi packets at home for about ₹30 — same hunger, way less cost.",
        "Eating at home instead of outside can easily cut your food spending in half.",
        "Some days, a ₹20–30 snack at home beats a full restaurant breakfast — and your wallet won't notice.",
        "Cook once, eat twice — leftovers save both time and money on your next meal.",
        "Carry a water bottle instead of buying one every time you're out — small, but it adds up.",
        "Avoid Swiggy, Zomato, Blinkit and Uber Eats when you can — cutting the 'one tap order' habit removes a lot of tempting spending."
    )

    private val foodDeliverySpecific = listOf(
        "That was a food delivery order. Swiggy/Zomato/Blinkit make it too easy to spend — try cooking at home next time.",
        "Delivery + platform fees + tip add up fast. A home-cooked meal is almost always cheaper.",
        "Try meal-prepping for 2–3 days so you're not tempted to order in when you're hungry and tired."
    )

    fun random(isFoodDeliveryContext: Boolean, random: Random = Random.Default): String =
        (if (isFoodDeliveryContext) foodDeliverySpecific else general).random(random)
}
