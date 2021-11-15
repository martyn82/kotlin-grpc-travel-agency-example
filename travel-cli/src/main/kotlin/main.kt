import io.grpc.ConnectivityState
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import me.martijn.car_rental.client.CarRentalClient
import me.martijn.hotel.client.HotelClient
import me.martijn.trip.client.TripClient
import java.util.concurrent.TimeUnit

suspend fun main() {
    val retryPolicy: MutableMap<String, Any> = HashMap()
    retryPolicy["maxAttempts"] = 100.0
    retryPolicy["initialBackoff"] = "0.1s"
    retryPolicy["maxBackoff"] = "10s"
    retryPolicy["backoffMultiplier"] = 2.0
    retryPolicy["retryableStatusCodes"] = listOf<Any>(
        "UNAVAILABLE",
        "INTERNAL",
        "UNKNOWN"
    )

    val hotelChannel = ManagedChannelBuilder
        .forAddress("localhost", 50051)
        .usePlaintext()
        .defaultServiceConfig(mapOf(
            "retryPolicy" to retryPolicy
        ))
        .maxRetryAttempts(100)
        .enableRetry()
        .build()

    val carChannel = ManagedChannelBuilder
        .forAddress("localhost", 50052)
        .usePlaintext()
        .build()

    val tripChannel = ManagedChannelBuilder
        .forAddress("localhost", 50053)
        .usePlaintext()
        .build()

    val hotel = HotelClient(hotelChannel)
    val cars = CarRentalClient(carChannel)
    val trips = TripClient(tripChannel)

    print("Hotels:\n  Use 'findRoom x' to find rooms for at least x persons.\n  Use 'bookRoom x' to book a room with identifier x.")
    println("\n  Use 'subscribe' to subscribe to rooms coming available.")
    println("Cars:\n  Use 'findCars' to find available cars.\n  Use 'rentCar x' to rent a car with identifier x.")
    println("Trips:\n  Use 'findTrips' to find package deals.\n  Use 'bookTrip x' to book a package deal with identifier x.")

    while (true) {
        print(">> ")
        val words = readLine()!!.split(' ')

        when (words[0]) {
            "findRoom" ->
                hotel.findAvailableRooms(words[1].toInt()).forEach { room ->
                    println("Found room:\n$room")
                }

            "subscribe" -> {
                suspend fun notify(subscription: String) {
                    hotel.listen(subscription).mapLeft {
                        notify(subscription)
                    }
                }
                val subscription = hotel.subscribe().subscriptionId
                notify(subscription)
            }

            "bookRoom" ->
                hotel.book(words[1]).fold(
                    { status ->
                        println(status)
                    },
                    { _ ->
                        println("Room booked!")
                    }
                )

            "findCars" ->
                cars.findAvailableCars().forEach { car ->
                    println("Found car:\n$car")
                }

            "rentCar" ->
                cars.rent(words[1]).fold(
                    { status ->
                        println(status)
                    },
                    { _ ->
                        println("Car rented!")
                    }
                )

            "findTrips" ->
                trips.findTrips().forEach { trip ->
                    println("Found trip:\n$trip")
                }

            "bookTrip" ->
                trips.book(words[1]).fold(
                    { status ->
                        println(status)
                    },
                    { _ ->
                        println("Trip booked!")
                    }
                )

            else ->
                println("Unknown message.")
        }
    }
}
