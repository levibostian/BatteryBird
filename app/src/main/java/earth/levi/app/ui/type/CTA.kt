package earth.levi.app.ui.type

import android.Manifest

abstract class CTA<T: Any> {
    abstract val title: String
    abstract val description: String
    abstract val actionTitle: String // more then likely will be a button text
    abstract val data: T
}

typealias AnyCTA = CTA<*>

data class RuntimePermissionCTA(
    override val title: String,
    override val description: String,
    override val actionTitle: String,
    val permission: RuntimePermission,
) : CTA<String>() {
    override val data: String
        get() = permission.string

    companion object {
        val sample = RuntimePermissionCTA(title = "Bluetooth permission required", description = "Want to view your devices in this app? App requires permission to bluetooth in order to do that. Accept bluetooth permission.", actionTitle = "Accept Bluetooth permission", permission = RuntimePermission.Bluetooth)
    }
}
