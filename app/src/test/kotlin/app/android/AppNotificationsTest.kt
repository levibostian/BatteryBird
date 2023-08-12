package app.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.BaseTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import app.mock
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.notifications.AppNotifications
import app.verifyDidNotHappen
import io.mockk.verify

@RunWith(AndroidJUnit4::class)
class AppNotificationsTest: BaseTest() {

    val androidNotificationsMock = mock<AndroidNotifications>()

    lateinit var notifications: AppNotifications

    @Before
    override fun setup() {
        super.setup()

        notifications = AppNotifications(keyValueStorage, androidNotificationsMock)
    }

    @Test
    fun getBatteryLowNotification_givenNotificationShownPreviously_expectDoNotShow() {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]
        keyValueStorage.setLowBatteryAlertIgnoredForDevice(givenBluetoothDevice, shouldIgnore = true)

        notifications.getBatteryLowNotification(context, givenBluetoothDevice, show = true)

        verifyDidNotHappen { androidNotificationsMock.showNotification(any()) }
    }

    @Test
    fun getBatteryLowNotification_givenNotificationNotShownPreviously_expectShow() {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]

        val notificationShown = notifications.getBatteryLowNotification(context, givenBluetoothDevice, show = true)

        verify { androidNotificationsMock.showNotification(notificationShown) }
    }

    @Test
    fun getBatteryLowNotification_givenDismiss_expectShowNextTimeWeWantToShowIt() {
        val givenBluetoothDevice = Samples.bluetoothDevices[0]

        notifications.getBatteryLowNotification(context, givenBluetoothDevice, show = true) // show it
        verify(exactly = 1) { androidNotificationsMock.showNotification(any()) }
        notifications.dismissBatteryLowNotification(context, givenBluetoothDevice) // dismiss it

        notifications.getBatteryLowNotification(context, givenBluetoothDevice, show = true)
        verify(exactly = 2) { androidNotificationsMock.showNotification(any()) }
    }

}