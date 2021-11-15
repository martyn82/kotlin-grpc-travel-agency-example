package me.martijn.car_rental.server

import com.google.protobuf.Empty
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import me.martijn.car_rental.*

class CarRentalServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(CarRental())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on port $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("Shutting down gRPC server")
                this@CarRentalServer.stop()
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

    private class CarRental(): CarRentalGrpcKt.CarRentalCoroutineImplBase() {
        private val cars: Map<String, Car> = mapOf(
            "2014780234" to Car.newBuilder()
                .setIdentifier("2014780234")
                .setMake("Volkswagen")
                .setModel("Polo")
                .setType("GTI")
                .build(),
            "2391023910" to Car.newBuilder()
                .setIdentifier("2391023910")
                .setMake("Mercedes-Benz")
                .setModel("C3")
                .build(),
            "1092182" to Car.newBuilder()
                .setIdentifier("1092182")
                .setMake("Tesla")
                .setModel("X")
                .build()
        )

        override suspend fun findAvailableCars(request: FindAvailableCarsRequest): FindAvailableCarsResponse =
            FindAvailableCarsResponse.newBuilder()
                .addAllCars(cars.values)
                .build()

        override suspend fun rent(request: RentRequest): Empty {
            val carID = request.carIdentifier
            cars[carID] ?:
                throw Status.NOT_FOUND
                    .augmentDescription("Car '$carID' does not exist")
                    .asException()

            return when (carID) {
                "1092182" ->
                    throw Status.FAILED_PRECONDITION
                        .augmentDescription("Car $carID is not available")
                        .asException()

                else ->
                    Empty.getDefaultInstance()
            }
        }
    }
}
