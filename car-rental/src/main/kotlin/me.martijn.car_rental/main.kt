package me.martijn.car_rental

import me.martijn.car_rental.server.CarRentalServer

fun main() {
    val server = CarRentalServer(port = 50052)

    server.start()
    server.blockUntilShutdown()
}
