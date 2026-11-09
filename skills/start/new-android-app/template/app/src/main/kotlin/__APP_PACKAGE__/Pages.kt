package __APP_PACKAGE__

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * The top-level pages. Snapshots and the self-test visit every entry, so a new
 * page goes here first.
 */
enum class Page(
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val tag: String,
) {
    All(R.string.page_all, R.drawable.ic_list, "nav-all"),
    Starred(R.string.page_starred, R.drawable.ic_star, "nav-starred"),
    Settings(R.string.page_settings, R.drawable.ic_settings, "nav-settings"),
}
