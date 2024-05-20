package models.enums

enum class DeviceType constructor(private val openConnectionName: String) {

    SAFELET("Safelet(conn.)"), SMART_BAND("sband(conn.)");

    fun getOpenConnectionName(): String {
        return openConnectionName
    }

    companion object {
        fun fromConnectionName(name: String?): DeviceType {
            return values().single { deviceType -> deviceType.getOpenConnectionName() == name }
        }
    }
}
