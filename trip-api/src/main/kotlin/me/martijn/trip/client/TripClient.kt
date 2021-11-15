package me.martijn.trip.client

import arrow.core.Either
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusException
import me.martijn.trip.BookRequest
import me.martijn.trip.FindTripsRequest
import me.martijn.trip.PackagedTrip
import me.martijn.trip.TripGrpcKt
import java.io.Closeable
import java.util.concurrent.TimeUnit

class TripClient(private val channel: ManagedChannel): Closeable {
    private val stub: TripGrpcKt.TripCoroutineStub = TripGrpcKt.TripCoroutineStub(channel)

    suspend fun findTrips(): List<PackagedTrip> {
        val request = FindTripsRequest.newBuilder()
            .build()

        val response = stub.findTrips(request)
        return response.tripsList
    }

    suspend fun book(tripIdentifier: String): Either<Status, Unit> {
        val request = BookRequest.newBuilder()
            .setPackagedTripIdentifier(tripIdentifier)
            .build()

        return try {
            stub.book(request)
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
