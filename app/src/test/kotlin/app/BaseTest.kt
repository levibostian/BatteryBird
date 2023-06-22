package app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.testing.WorkManagerTestInitHelper
import app.store.KeyValueStorage
import app.store.keyValueStorage
import io.mockk.MockK
import io.mockk.MockKVerificationScope
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import kotlin.reflect.KClass

abstract class BaseTest {

    protected val context: Context
        get() = ApplicationProvider.getApplicationContext()

    protected val di: DiGraph
        get() = DiGraph.instance

    protected lateinit var keyValueStorage: KeyValueStorage

    @Before
    open fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        DiGraph.initialize(context)

        keyValueStorage = di.keyValueStorage.also { it.deleteAll() }
    }

}

inline fun <reified T : Any> mock(
    relaxed: Boolean = true,
    block: T.() -> Unit = {}
): T = mockk(relaxed = relaxed, block = block)

fun verifyDidNotHappen(block: MockKVerificationScope.() -> Unit) {
    verify(inverse = true, verifyBlock = block)
}