package com.phapalesai.dhanapala.domain

import java.time.LocalDate
import kotlin.random.Random

/**
 * A tongue-in-cheek daily "financial horoscope," seeded by the calendar day
 * (not the app session) so it stays the same all day and changes tomorrow —
 * same rotation trick as [MoneyJokes] but day-seeded instead of session-seeded.
 */
object MoneyHoroscope {

    private val en = listOf(
        "The stars say: avoid Swiggy today. The stars are usually wrong, but still.",
        "Mercury is in retrograde, which is scientifically why your UPI finger is itchy today.",
        "Today's lucky category: literally anything except food delivery.",
        "The universe wants you to check your bank balance before your cart, not after.",
        "Your financial aura is strong today. Do not test it at the mall.",
        "A wise investment awaits — probably not the one your friend is texting you about.",
        "Venus favors savers today. Spenders should maybe just lie low.",
        "The cosmos predicts a 73% chance of regret if you open a shopping app right now.",
        "Today favors bold budgeting decisions. Bold spending decisions, less so.",
        "Your money horoscope: chaotic neutral, leaning chaotic."
    )

    private val hi = listOf(
        "Sitare keh rahe hain: aaj Swiggy se door raho bhai. Sitare aksar galat hote hain, phir bhi.",
        "Mercury retrograde mein hai, isliye aaj UPI wali finger khujli kar rahi hai.",
        "Aaj ka lucky category: food delivery ke alawa kuch bhi.",
        "Universe chahta hai tum cart se pehle apna balance dekho, baad mein nahi.",
        "Aaj tumhara financial aura strong hai. Mall mein test mat karna.",
        "Ek samajhdaar investment intezaar kar rahi hai — shayad wo nahi jiske baare mein dost text kar raha hai.",
        "Venus aaj savers ke favor mein hai. Spenders thoda chup rahen toh better.",
        "Cosmos ka andaza: abhi shopping app khola toh 73% chance regret ka.",
        "Aaj bold budgeting decisions ka din hai. Bold spending ka nahi.",
        "Tumhara money horoscope: chaotic neutral, chaotic ki taraf jhukta hua."
    )

    private val mr = listOf(
        "Tare mhantat: aaj Swiggy pasun door raha bhava. Tare bahutda chuk astat, tari.",
        "Mercury retrograde madhe ahe, mhanunach aaj UPI wala bot khaj sutoy.",
        "Aajcha lucky category: food delivery sodun kahi pan.",
        "Universe la vatte tumhi cart adhi balance baghava, nantar nahi.",
        "Aaj tumcha financial aura strong ahe. Mall madhe test karu naka.",
        "Ek shahani investment vaat baghatey — bahutek ti nahi ji mitra text karto ahe.",
        "Venus aaj savers cha favor madhe ahe. Spenders ni jara gappa rahava.",
        "Cosmos cha andaz: ata shopping app ughadla tar 73% chance regret cha.",
        "Aaj bold budgeting decisions cha divas ahe. Bold spending cha nahi.",
        "Tumcha money horoscope: chaotic neutral, chaotic kade zuklela."
    )

    private val byLanguage = mapOf(RoastLanguage.EN to en, RoastLanguage.HI to hi, RoastLanguage.MR to mr)

    /** Same result all day for a given date+language, changes at midnight. */
    fun today(language: RoastLanguage, date: LocalDate = LocalDate.now()): String {
        val pool = byLanguage[language] ?: en
        val random = Random(date.toEpochDay())
        return pool.random(random)
    }
}
