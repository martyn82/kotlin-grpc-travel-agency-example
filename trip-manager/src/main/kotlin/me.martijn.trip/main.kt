package me.martijn.trip

import io.grpc.ManagedChannelBuilder
import me.martijn.car_rental.client.CarRentalClient
import me.martijn.hotel.client.HotelClient
import me.martijn.trip.server.TripServer

fun main() {
    val hotelChannel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .build()

    val carChannel = ManagedChannelBuilder
        .forAddress("localhost", 50052)
        .usePlaintext()
        .build()

    val hotel = HotelClient(hotelChannel)
    val car = CarRentalClient(carChannel)

    val server = TripServer(port = 50053, hotel, car)

    server.start()
    server.blockUntilShutdown()
}
