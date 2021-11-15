package me.martijn.hotel.server

import com.google.protobuf.Empty
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.martijn.hotel.*

class HotelServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(HotelManager())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on port $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("Shutting down gRPC server")
                this@HotelServer.stop()
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

    private class HotelManager(): HotelGrpcKt.HotelCoroutineImplBase() {
        private val rooms: Map<String, Room> = mapOf(
            "4378923" to Room.newBuilder()
                .setIdentifier("4378923")
                .setPersons(1)
                .build(),
            "347802" to Room.newBuilder()
                .setIdentifier("347802")
                .setPersons(2)
                .build(),
            "4589242" to Room.newBuilder()
                .setIdentifier("4589242")
                .setPersons(2)
                .build()
        )

        override suspend fun findAvailableRooms(request: FindAvailableRoomsRequest): FindAvailableRoomsResponse =
            FindAvailableRoomsResponse.newBuilder()
                .addAllRooms(
                    rooms.filter { (_, value) ->
                        value.persons >= request.persons
                    }.values
                )
                .build()

        override suspend fun subscribe(request: SubscribeRequest): SubscribeResponse =
            SubscribeResponse.newBuilder()
                .setSubscriptionId("abcdef")
                .build()

        override fun listen(request: ListenRequest): Flow<ListenResponse> =
            rooms.values.asFlow().map {
                Thread.sleep(3000)
                ListenResponse.newBuilder()
                    .addRooms(it)
                    .build()
            }

        override suspend fun book(request: BookRequest): Empty {
            val roomID = request.roomIdentifier
            rooms[roomID] ?:
                throw Status.NOT_FOUND
                    .augmentDescription("Room '$roomID' does not exist")
                    .asException()

            return when (roomID) {
                "347802" ->
                    throw Status.FAILED_PRECONDITION
                        .augmentDescription("Room $roomID is not available")
                        .asException()

                else ->
                    Empty.getDefaultInstance()
            }
        }
    }
}
