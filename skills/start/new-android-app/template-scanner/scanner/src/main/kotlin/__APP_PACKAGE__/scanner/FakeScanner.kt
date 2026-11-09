package __APP_PACKAGE__.scanner

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** A scanner you drive by hand. Tests and emulators call [scan]. */
class FakeScanner : Scanner {
    private val flow = MutableSharedFlow<Scan>(extraBufferCapacity = 16)

    override val scans: Flow<Scan> = flow

    fun scan(data: String, symbology: String = "LABEL-TYPE-CODE128") {
        flow.tryEmit(Scan(data, symbology))
    }
}
