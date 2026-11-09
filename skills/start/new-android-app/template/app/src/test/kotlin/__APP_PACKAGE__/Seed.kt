package __APP_PACKAGE__

import __APP_PACKAGE__.data.AppDatabase
import __APP_PACKAGE__.data.Item

private const val BASE_TIME = 1_700_000_000_000L

/**
 * Sample data for snapshots and tests. Grow it with the app: long and short
 * titles, every status, and only what a person can create in the app.
 * The first item is the newest, so it is the first row on the All page.
 */
val seedItems: List<Item> = listOf(
    "Plan the week" to false,
    "Buy oat milk, bread, and the good coffee beans from the market" to true,
    "Call Sam" to false,
    "Renew passport" to true,
    "Book dentist" to false,
    "Read chapter 4 of the design book and write down three ideas for the next release" to false,
    "Water plants" to false,
    "Pay electricity bill" to true,
    "Back up photos" to false,
    "Fix the bike light" to false,
    "Ideas for the weekend trip" to false,
    "Return library books" to false,
).mapIndexed { index, (title, starred) ->
    Item(id = index + 1L, title = title, starred = starred, createdAt = BASE_TIME - index * 60_000L)
}

suspend fun AppDatabase.seed() {
    itemDao().insertAll(seedItems)
}
