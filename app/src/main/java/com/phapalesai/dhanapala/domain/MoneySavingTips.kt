package com.phapalesai.dhanapala.domain

import com.phapalesai.dhanapala.data.local.Category
import kotlin.random.Random

/**
 * Practical, non-judgmental frugal tips — rotates like a "tip of the day."
 * Picks from a category-specific pool matching the user's most recent debit
 * (e.g. just spent on Fuel -> fuel-saving tips), falling back to general tips.
 */
object MoneySavingTips {

    private val general = listOf(
        "Instead of a ₹200 restaurant lunch, try 2 Maggi packets at home for about ₹30 — same hunger, way less cost.",
        "Eating at home instead of outside can easily cut your food spending in half.",
        "Some days, a ₹20–30 snack at home beats a full restaurant breakfast — and your wallet won't notice.",
        "Cook once, eat twice — leftovers save both time and money on your next meal.",
        "Carry a water bottle instead of buying one every time you're out — small, but it adds up.",
        "Avoid Swiggy, Zomato, Blinkit and Uber Eats when you can — cutting the 'one tap order' habit removes a lot of tempting spending.",
        "Before any purchase, wait 24 hours — most impulse buys don't survive a day of thinking.",
        "Set a small weekly cash limit for 'fun money' so you don't have to think twice about every little spend.",
        "Review your subscriptions once a month — most people are quietly paying for at least one they forgot about.",
        "Compare prices across two apps before a big purchase — the difference is often more than you'd expect.",
        "Pay yourself first — move a fixed amount to savings the day you get paid, before you spend anything.",
        "Track every expense for one week — awareness alone usually cuts spending by 10-15%.",
        "Use cash for discretionary spending — physically running out of notes is a better brake than a card limit.",
        "Unsubscribe from shopping app notifications — fewer 'sale' alerts means fewer unplanned purchases.",
        "A simple rule: if it's not on your list, sleep on it before buying."
    )

    private val food = listOf(
        "That was a food delivery order. Swiggy/Zomato/Blinkit make it too easy to spend — try cooking at home next time.",
        "Delivery + platform fees + tip add up fast. A home-cooked meal is almost always cheaper.",
        "Try meal-prepping for 2–3 days so you're not tempted to order in when you're hungry and tired.",
        "Keep instant options like Maggi or eggs at home for when you're too tired to cook — still cheaper than delivery.",
        "Order in bulk with friends/roommates to split delivery fees instead of ordering solo.",
        "Skip the add-ons (extra cheese, drinks, desserts) — they're where delivery bills quietly balloon.",
        "Cook a big batch on Sunday — 3-4 meals sorted with zero extra delivery temptation all week.",
        "Set a 'no delivery after 9pm' rule — late-night orders are usually the most impulsive ones.",
        "Uninstall food delivery apps for a week and see how much you save without even trying.",
        "A tiffin service is often cheaper per meal than delivery, and healthier too.",
        "Restaurant prices plus delivery markup can be 40-60% more than the same dish cooked at home.",
        "Keep fruit or nuts at your desk — a lot of 'hunger orders' are really just snack cravings."
    )

    private val fuel = listOf(
        "Combine errands into one trip instead of multiple short drives — saves fuel and time.",
        "Maintaining correct tyre pressure can improve mileage by up to 3-4%.",
        "Carpool or share rides for regular commutes to split fuel costs.",
        "Compare fuel prices at nearby stations — even ₹1-2/litre difference adds up over a month.",
        "Smooth acceleration and braking uses noticeably less fuel than aggressive driving.",
        "Consider public transport or a bike for short distances instead of always driving.",
        "Get your vehicle serviced on schedule — a poorly tuned engine burns more fuel.",
        "Switch off the engine at long signals instead of idling.",
        "Plan your route in advance to avoid unnecessary detours and traffic-heavy roads.",
        "Walking or cycling for trips under 2km saves fuel and is free exercise."
    )

    private val shopping = listOf(
        "Add items to your cart and wait a day before checking out — a lot of 'must-haves' don't survive the wait.",
        "Check for coupon codes or cashback offers before completing any online purchase.",
        "Unfollow shopping influencers/pages if they're driving impulse purchases.",
        "Buy off-season for clothes and electronics — prices drop significantly outside launch/festive periods.",
        "Make a list before shopping and stick to it — unplanned aisle browsing is where budgets leak.",
        "Compare price-per-unit, not just the sticker price, especially for groceries.",
        "Ask yourself: would I still buy this if it wasn't on sale? If no, skip it.",
        "Resell or return items you bought but haven't used in a month.",
        "Try a 30-day rule for anything over ₹1000 — if you still want it in a month, then buy it.",
        "Shop with a fixed budget in cash for non-essentials to avoid overspending on cards."
    )

    private val bills = listOf(
        "Switch off appliances at the plug instead of standby — phantom load adds up on your electricity bill.",
        "Compare recharge/broadband plans every few months — providers often have better deals for new vs existing customers.",
        "Bundle your mobile, DTH and broadband where possible — combo plans are usually cheaper than separate ones.",
        "Set reminders for bill due dates to avoid late fees, which are pure wasted money.",
        "Use LED bulbs — they cost more upfront but cut electricity bills significantly over time.",
        "Review your data/OTT subscriptions — most people pay for at least one they barely use.",
        "Negotiate your broadband/DTH renewal — providers often have retention discounts they don't advertise.",
        "Set your AC to 24-26°C instead of lower — every degree colder increases power usage noticeably.",
        "Pay bills via autopay to avoid late fees, but check the amount monthly so it doesn't creep up unnoticed.",
        "Switch to an annual/prepaid plan where possible — they're often cheaper than paying monthly."
    )

    private val entertainment = listOf(
        "Share OTT subscriptions with family instead of everyone paying separately.",
        "Rotate which streaming service you're subscribed to — pause Netflix, watch Prime, then switch back.",
        "Check if your bank/card offers free OTT subscriptions before paying separately.",
        "Look for matinee or weekday movie shows — tickets are often 30-40% cheaper.",
        "Home movie nights with snacks you made are a fraction of the cost of a theatre outing.",
        "Cancel subscriptions you haven't opened in the last month — you probably won't miss them.",
        "Use your gym/club membership fully, or consider pausing it if you're not going regularly.",
        "Look for free local events — most cities have free concerts, exhibitions, or meetups.",
        "Bundle entertainment subscriptions through telecom offers when available — often cheaper combined.",
        "Set a monthly 'fun budget' so entertainment spending has a ceiling without feeling restrictive."
    )

    private val travel = listOf(
        "Book cab rides in advance during non-surge hours when possible — prices spike a lot during peak times.",
        "Compare cab aggregators before booking — prices for the same route can vary significantly.",
        "Use metro/local trains instead of cabs for routes they cover — usually a fraction of the cost.",
        "Book train/flight tickets early — last-minute fares are almost always higher.",
        "Share cabs with coworkers or friends heading the same way.",
        "Check for travel card/pass options if you commute daily — they're often cheaper than per-ride fares.",
        "Walk short distances instead of booking a cab for very close destinations.",
        "Compare train vs flight costs including baggage/convenience fees for short routes — trains often win."
    )

    private val cashWithdrawal = listOf(
        "Frequent small ATM withdrawals often lead to more cash spending — withdraw a planned weekly amount instead.",
        "Use UPI for most payments and keep cash withdrawals for occasional need — easier to track spending digitally.",
        "Check if your bank charges fees for ATM usage beyond a free limit — those charges add up.",
        "Set a weekly cash budget and stick to that single withdrawal instead of multiple top-ups.",
        "Cash in hand is spent faster than money that stays in the account — be mindful after withdrawing.",
        "Use your own bank's ATM to avoid third-party withdrawal charges.",
        "Keep withdrawn cash in a separate 'spending' pocket so you can see it running low.",
        "If you're withdrawing cash often for small purchases, UPI might genuinely save you money on ATM fees."
    )

    /** @param recentCategory the most recent debit's category, or null if there's nothing to key off yet. */
    fun random(recentCategory: String?, random: Random = Random.Default): String {
        val pool = when (recentCategory) {
            Category.FOOD -> food
            Category.FUEL -> fuel
            Category.SHOPPING -> shopping
            Category.BILLS -> bills
            Category.ENTERTAINMENT -> entertainment
            Category.TRAVEL -> travel
            Category.CASH_WITHDRAWAL -> cashWithdrawal
            else -> general
        }
        return pool.random(random)
    }
}
