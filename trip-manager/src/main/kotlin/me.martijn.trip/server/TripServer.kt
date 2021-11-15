package me.martijn.trip.server

import arrow.core.getOrElse
import com.google.protobuf.Empty
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import me.martijn.car_rental.client.CarRentalClient
import me.martijn.hotel.client.HotelClient
import me.martijn.trip.*

class TripServer(private val port: Int, private val hotel: HotelClient, private val car: CarRentalClient) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(TripManager(hotel, car))
        .build()

    fun start() {
        server.start()
        println("Server started, listening on port $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("Shutting down gRPC server")
                this@TripServer.stop()
                println("Shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class TripManager(private val hotel: HotelClient, private val car: CarRentalClient): TripGrpcKt.TripCoroutineImplBase() {
        private val trips: Map<String, PackagedTrip> = mapOf(
            "2109210" to PackagedTrip.newBuilder()
                .setIdentifier("2109210")
                .setName("Trip with unavailable room")
                .setCarIdentifier("2014780234")
                .setRoomIdentifier("347802")
                .build(),
            "2903271" to PackagedTrip.newBuilder()
                .setIdentifier("2903271")
                .setName("Trip with unavailable car")
                .setCarIdentifier("1092182")
                .setRoomIdentifier("4378923")
                .build(),
            "4w498032" to PackagedTrip.newBuilder()
                .setIdentifier("4w498032")
                .setName("Available trip")
                .setCarIdentifier("2391023910")
                .setRoomIdentifier("4589242")
                .build()
        )

        override suspend fun findTrips(request: FindTripsRequest): FindTripsResponse =
            FindTripsResponse.newBuilder()
                .addAllTrips(trips.values)
                .build()

        override suspend fun book(request: BookRequest): Empty {
            val tripID = request.packagedTripIdentifier
            val trip = trips[tripID] ?:
                throw Status.NOT_FOUND
                    .augmentDescription("Trip '$tripID' does not exist")
                    .asException()

            val roomID = trip.roomIdentifier
            val carID = trip.carIdentifier

            hotel.book(roomID).mapLeft { status ->
                throw status.asException()
            }

            car.rent(carID).mapLeft { status ->
                throw status.asException()
            }

            return Empty.getDefaultInstance()
        }
    }
}
