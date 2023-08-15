package app.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.BaseTest
import app.android.AndroidNotifications
import app.android.Bluetooth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import app.mock
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.notifications.AppNotifications
import app.store.bluetoothDevicesStore
import app.verifyDidNotHappen
import earth.levi.batterybird.BluetoothDeviceModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BluetoothDevicesRepositoryTest: BaseTest() {

    val bluetoothMock = mock<Bluetooth>()
    val notificationsMock = mock<AppNotifications>()

    lateinit var repository: BluetoothDevicesRepository

    @Before
    override fun setup() {
        super.setup()
        
        repository = BluetoothDevicesRepositoryImpl(bluetoothMock, di.bluetoothDevicesStore, notificationsMock, keyValueStorage)
    }

    // updateBatteryLevel

    @Test
    fun updateBatteryLevel_givenNotificationIgnoredForDevice_givenBatteryLevelLow_expectDoNotShow() = runBlocking {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]
        setNewBatteryLevelOfDevice(givenBluetoothDevice, 1)

        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = true)

        repository.updateBatteryLevel(context, givenBluetoothDevice, updateNotifications = true)

        assertNotificationNotShown()
    }

    @Test
    fun updateBatteryLevel_givenNotificationNotIgnored_givenBatteryLevelLow_expectShow() = runBlocking {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]
        setNewBatteryLevelOfDevice(givenBluetoothDevice, 1)
        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = false)

        repository.updateBatteryLevel(context, givenBluetoothDevice, updateNotifications = true)

        assertNotificationShown()
    }

    @Test
    fun updateBatteryLevel_givenBatteryLevelNotLow_expectResetIfNotificationIgnored() = runBlocking {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]
        setNewBatteryLevelOfDevice(givenBluetoothDevice, 100)

        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = true)
        assertTrue { keyValueStorage.isLowBatteryAlertIgnoredForDevice(givenBluetoothDevice) }

        repository.updateBatteryLevel(context, givenBluetoothDevice, updateNotifications = true)

        assertNotificationNotShown()
        assertIgnoredStateReset(givenBluetoothDevice)
    }

    @Test
    fun updateBatteryLevel_givenDeviceNotConnected_givenBatteryLow_expectUseCachedBatteryLevel_expectShowNotification() = runBlocking {
        val givenBluetoothDevice = Samples.bluetoothDevices[0].copy(batteryLevel = 1)
        setNewBatteryLevelOfDevice(givenBluetoothDevice, null)

        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = false)

        repository.updateBatteryLevel(context, givenBluetoothDevice, updateNotifications = true)

        assertNotificationShown()
    }

    @Test
    fun updateBatteryLevel_givenDeviceNotConnected_givenBatteryNotLow_expectUseCachedBatteryLevel_expectDoNotShowNotification() = runBlocking {
        val givenBluetoothDevice = Samples.bluetoothDevices[0].copy(batteryLevel = 100)
        setNewBatteryLevelOfDevice(givenBluetoothDevice, null)

        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = false)

        repository.updateBatteryLevel(context, givenBluetoothDevice, updateNotifications = true)

        assertNotificationNotShown()
        assertIgnoredStateReset(givenBluetoothDevice)
    }

    // util functions

    private fun setNewBatteryLevelOfDevice(device: BluetoothDeviceModel, batteryLevel: Int?) {
        coEvery { bluetoothMock.getBatteryLevel(context, device) } returns batteryLevel
    }

    private fun assertNotificationShown() {
        verify { notificationsMock.show(any()) }
        verifyDidNotHappen { notificationsMock.dismissBatteryLowNotification(any(), any()) }
    }

    private fun assertNotificationNotShown() {
        verifyDidNotHappen { notificationsMock.show(any()) }
    }

    private fun assertIgnoredStateReset(givenBluetoothDevice: BluetoothDeviceModel) {
        verify { notificationsMock.dismissBatteryLowNotification(context, givenBluetoothDevice) }
        assertFalse { keyValueStorage.isLowBatteryAlertIgnoredForDevice(givenBluetoothDevice) }
    }

}