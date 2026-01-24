package com.gabinote.image.testSupport.testConfig.container

import org.testcontainers.containers.Network
import java.net.ServerSocket
import kotlin.io.use

object ContainerNetworkHelper {
    @JvmStatic
    val testNetwork: Network = Network.newNetwork()

    fun getAvailablePort(): Int {
        return ServerSocket(0).use { socket ->
            socket.localPort
        }
    }
}