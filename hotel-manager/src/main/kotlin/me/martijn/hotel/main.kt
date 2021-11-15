package me.martijn.hotel

import me.martijn.hotel.server.HotelServer

fun main() {
    val server = HotelServer(port = 50051)

    server.start()
    server.blockUntilShutdown()
}
