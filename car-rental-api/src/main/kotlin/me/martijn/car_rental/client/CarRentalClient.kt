package me.martijn.car_rental.client

import arrow.core.Either
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusException
import me.martijn.car_rental.Car
import me.martijn.car_rental.CarRentalGrpcKt
import me.martijn.car_rental.FindAvailableCarsRequest
import me.martijn.car_rental.RentRequest
import java.io.Closeable
import java.util.concurrent.TimeUnit

class CarRentalClient(private val channel: ManagedChannel): Closeable {
    private val stub: CarRentalGrpcKt.CarRentalCoroutineStub = CarRentalGrpcKt.CarRentalCoroutineStub(channel)

    suspend fun findAvailableCars(): List<Car> {
        val request = FindAvailableCarsRequest.newBuilder()
            .build()

        val response = stub.findAvailableCars(request)
        return response.carsList
    }

    suspend fun rent(carIdentifier: String): Either<Status, Unit> {
        val request = RentRequest.newBuilder()
            .setCarIdentifier(carIdentifier)
            .build()

        return try {
            stub.rent(request)
            Either.Right(Unit)
        } catch (e: StatusException) {
            Either.Left<Status>(e.status)
        }
    }

    override fun close() {
        channel.shutdown()
            .awaitTermination(5, TimeUnit.SECONDS)
    }
}
