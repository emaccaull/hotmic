package io.github.emaccaull.hotmic

import android.media.AudioDeviceInfo

val AudioDeviceInfo.typeString: String
    get() {
        return when (type) {
            AudioDeviceInfo.TYPE_AUX_LINE -> "auxiliary line-level connectors"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth device supporting the A2DP profile"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth device typically used for telephony"
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "built-in earphone speaker"
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> "built-in microphone"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "built-in speaker"
            AudioDeviceInfo.TYPE_BUS -> "BUS"
            AudioDeviceInfo.TYPE_DOCK -> "DOCK"
            AudioDeviceInfo.TYPE_FM -> "FM"
            AudioDeviceInfo.TYPE_FM_TUNER -> "FM tuner"
            AudioDeviceInfo.TYPE_HDMI -> "HDMI"
            AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI audio return channel"
            AudioDeviceInfo.TYPE_IP -> "IP"
            AudioDeviceInfo.TYPE_LINE_ANALOG -> "line analog"
            AudioDeviceInfo.TYPE_LINE_DIGITAL -> "line digital"
            AudioDeviceInfo.TYPE_TELEPHONY -> "telephony"
            AudioDeviceInfo.TYPE_TV_TUNER -> "TV tuner"
            AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB accessory"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "wired headphones"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "wired headset"
            AudioDeviceInfo.TYPE_UNKNOWN -> "unknown"
            else -> "unknown"
        }
    }

val AudioDeviceInfo.friendlyName: String
    get() = "$productName $typeString"
