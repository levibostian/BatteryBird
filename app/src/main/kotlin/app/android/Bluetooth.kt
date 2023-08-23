package app.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import app.DiGraph
import app.extensions.now
import app.log.Logger
import app.log.logger
import app.model.samples.Samples
import app.model.samples.bluetoothDeviceModels
import app.ui.type.RuntimePermission
import earth.levi.batterybird.BluetoothDeviceModel
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface Bluetooth: AndroidFeature {
    fun canGetPairedDevices(context: Context): Boolean
    // Gets list of paired devices (or empty list if none), or Error if permissions not yet accepted
    fun getPairedDevices(context: Context): Result<List<BluetoothDeviceModel>>
    suspend fun getBatteryLevel(context: Context, device: BluetoothDeviceModel): Int?
    fun isDeviceConnected(device: BluetoothDeviceModel): Boolean
    // if system bluetooth is on or not
    val isBluetoothOn: Boolean
}

val DiGraph.bluetooth: Bluetooth
    get() = override() ?: BluetoothImpl(logger, bluetoothManager)

class BluetoothImpl(private val log: Logger, private val bluetoothManager: BluetoothManager): AndroidFeatureImpl(), Bluetooth {

    // Get this later instead of in the constructor. Getting the bluetooth adapter might return null so we want to try and retrieve it only after
    // we have done checks such as getting permission.
    private val systemBluetoothAdapter
        get() = bluetoothManager.adapter

    override fun canGetPairedDevices(context: Context): Boolean = areAllPermissionsGranted(context)

    override fun isDeviceConnected(device: BluetoothDeviceModel): Boolean = device.isConnected
    
    @SuppressLint("MissingPermission") // we check if permission granted inside of the function.
    override fun getPairedDevices(context: Context): Result<List<BluetoothDeviceModel>> {
        if (!canGetPairedDevices(context)) return Result.failure(BluetoothPermissionsNotAccepted())

        // Note: If bluetooth is off on device, bondedDevices will return empty list.
        return Result.success(systemBluetoothAdapter.bondedDevices.toList().map { pairedDevice ->
            BluetoothDeviceModel(
                hardwareAddress = pairedDevice.address,
                name = pairedDevice.name,
                batteryLevel = null, // we have to update the battery level in a separate call because it's an async operation.
                isConnected = pairedDevice.isConnected,
                notificationBatteryLevel = null,
                lastTimeConnected = if (pairedDevice.isConnected) now() else null
            )
        })
    }

    @SuppressLint("MissingPermission")
    override suspend fun getBatteryLevel(context: Context, device: BluetoothDeviceModel): Int? = suspendCoroutine { continuation ->
        val gattCallback = object : BluetoothGattCallback() {
            val onDone: (BluetoothGatt?, Int?) -> Unit = { gatt, batteryLevel ->
                log.debug("battery level for device (${gatt?.device?.name}/${gatt?.device?.address}): $batteryLevel", this)

                continuation.resume(batteryLevel)
                gatt?.close()
            }

            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        log.debug("device connected: ${gatt.device.name}/${gatt.device.address}", this)
                        gatt.discoverServices() // you must discover services in order to use them. I tried to skip this step and just read the characteristic, but it didn't work. All reading was null.
                    }
                    else -> {
                        log.debug("disconnected from device ($newState): ${device.name}/${device.hardwareAddress}", this)
                        onDone(gatt, null)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val batteryService = gatt.getServiceOrNull(UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb"))
                //val deviceNameService = gatt.getServiceOrNull(UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb"))
                //val appearanceService = gatt.getServiceOrNull(UUID.fromString("00001800-0000-1000-8000-00805f9b34fb"))

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    log.debug("services for device (${gatt.device.address}): ${gatt.services.map { it.uuid }.joinToString(separator = ", ")}", this)

                    if (batteryService != null && gatt.services.map { it.uuid }.contains(batteryService.uuid)) {
                        val batteryLevelCharacteristic = batteryService.getCharacteristicOrNull(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"))

                        if (batteryLevelCharacteristic != null) {
                            gatt.readCharacteristic(batteryLevelCharacteristic)
                        } else {
                            onDone(gatt, null)
                        }
                    } else {
                        // if we have no characteristics to read, end early
                        onDone(gatt, null)
                    }

//                    if (gatt.services.map { it.uuid }.contains(deviceNameService.uuid)) {
//                        //log.debug("contains device service!", this)
//                        val deviceNameCharacteristic = deviceNameService.getCharacteristic(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"))
//                        gatt.readCharacteristic(deviceNameCharacteristic)
//                    }
//
//                    if (gatt.services.map { it.uuid }.contains(appearanceService.uuid)) {
//                        //log.debug("contains appearance service!", this)
//                        val appearanceCharacteristic = appearanceService.getCharacteristic(UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb"))
//                        gatt.readCharacteristic(appearanceCharacteristic)
//                    }
                } else {
                    onDone(gatt, null)
                }
            }

            // here for backwards compatibility. This is used for API < 33
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
               log.debug("characteristic read. ${characteristic.uuid}, status: $status. ${device.name}/${device.hardwareAddress}", this)

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (characteristic.uuid) {
                        UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") -> { // battery level
                            val batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)

                            onDone(gatt, batteryLevel)
                        }
                    }
                } else {
                    onDone(gatt, null)
                }
            }

            // Used for API >= 33
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (characteristic.uuid) {
                        UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb") -> { // battery level
                            val batteryLevel = value[0].toInt()

                            onDone(gatt, batteryLevel)
                        }
                    }
                } else {
                    onDone(gatt, null)
                }
            }
        }

        val remoteDevice = try {
            systemBluetoothAdapter.getRemoteDevice(device.hardwareAddress) ?: return@suspendCoroutine continuation.resume(null)
        } catch (IllegalArgumentException: Exception) {
            // if bluetooth address is invalid, an exception is thrown (example: `ff:ff:ff:ff:ff:ff`). We can try to add validation in the UI to prevent exceptions, but still try/catch to prevent app crashes.
            log.debug("invalid bluetooth address: ${device.hardwareAddress}", this)

            return@suspendCoroutine continuation.resume(null)
        }

        if (!canGetPairedDevices(context)) return@suspendCoroutine continuation.resume(null)

        // dont try to get battery level if device is not connected. This is a way to save battery by using the GATT code less often. I have noticed this app drains battery more when we try to connect to GATT for every device without checking first if it's connected.
        if (!remoteDevice.isConnected) {
            log.debug("device is not connected, skip getting battery level for device: ${device.name}/${device.hardwareAddress}", this)
            return@suspendCoroutine continuation.resume(null)
        }

        log.debug("getting battery level for device: ${device.name}/${device.hardwareAddress}", this)

        /*
        Added this because I had issues with some of my devices. Specifically, my Sony WH-1000XM5 headphones.
        Behavior:
        * device connected to my phone.
        * connectGatt() called on the headphones.
        * connection status is connected.
        * doesn't matter what happens in the gatt connection callback.
        * Exactly 5 seconds later, the headphones turn off which means that they disconnect from my phone.

        When I use this, the headphones have no issues.
         */
        remoteDevice.batteryLevel?.let {  batteryLevel ->
            log.debug("OS battery level: ${device.name}/${device.hardwareAddress}, $batteryLevel", this)
            return@suspendCoroutine continuation.resume(batteryLevel)
        }

        remoteDevice.connectGatt(context, false, gattCallback)
    }

    override val isBluetoothOn: Boolean
        get() = systemBluetoothAdapter.isEnabled

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Bluetooth)
}

// using systemapi function to get battery level. there is risk in using a non-public sdk function, however, logcat has not yet shown a warning from the android source code that the function is hidden and what alternative to use. Therefore, I think there is less risk involved in using at this time. Something to watch.
val BluetoothDevice.batteryLevel: Int?
    get() {
        val level = javaClass.getMethod("getBatteryLevel").invoke(this) as Int
        if (level < 0) return null
        return level
    }

val BluetoothDevice.isConnected: Boolean
    get() {
        return javaClass.getMethod("isConnected").invoke(this) as Boolean
    }

class BluetoothPermissionsNotAccepted: Throwable()

// The regular getService function might return null, but the return data structure is not an Optional. Therefore, it took a while of debugging to discover this and learn I need to check for null.
fun BluetoothGatt.getServiceOrNull(uuid: UUID): BluetoothGattService? {
    val service = getService(uuid)

    @Suppress("IfThenToSafeAccess")
    return if (service == null) null
    else service
}

fun BluetoothGattService.getCharacteristicOrNull(uuid: UUID): BluetoothGattCharacteristic? {
    val characteristic = getCharacteristic(uuid)

    @Suppress("IfThenToSafeAccess")
    return if (characteristic == null) null
    else characteristic
}