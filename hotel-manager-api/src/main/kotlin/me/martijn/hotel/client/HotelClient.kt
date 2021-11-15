package me.martijn.hotel.client

import arrow.core.Either
import io.grpc.*
import kotlinx.coroutines.flow.collect
import me.martijn.hotel.*
import java.io.Closeable
import java.util.concurrent.TimeUnit

class HotelClient(private val channel: ManagedChannel): Closeable {
    private val stub: HotelGrpcKt.HotelCoroutineStub = HotelGrpcKt.HotelCoroutineStub(channel)

    suspend fun findAvailableRooms(persons: Int): List<Room> {
        val request = FindAvailableRoomsRequest.newBuilder()
            .setPersons(persons)
            .build()

        val response = stub.findAvailableRooms(request)

        return response.roomsList
    }

    suspend fun subscribe(): SubscribeResponse =
        stub.subscribe(
            SubscribeRequest.getDefaultInstance()
        )

    suspend fun listen(subscriptionId: String): Either<Status, Unit> {
        val request = ListenRequest.newBuilder()
            .setSubscriptionId(subscriptionId)
            .build()

        return try {
            Either.Right(
                stub.withWaitForReady()
                    .listen(request).collect { response ->
                        response.roomsList.forEach { println(it) }
                    }
            )
        } catch (e: StatusException) {
            Either.Left(e.status)
        }
    }

    suspend fun book(roomIdentifier: String): Either<Status, Unit> {
        val request = BookRequest.newBuilder()
            .setRoomIdentifier(roomIdentifier)
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
