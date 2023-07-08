package earth.levi.batterybird.store

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.datetime.Instant

expect class DriverFactory {
    fun createDriver(): SqlDriver
}

val instantAdapter = object : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}

fun createDatabase(driverFactory: DriverFactory): Database {
    val driver = driverFactory.createDriver()

    return Database(driver, BluetoothDeviceModel.Adapter(lastTimeConnectedAdapter = instantAdapter))
}
