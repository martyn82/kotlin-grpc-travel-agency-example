
rootProject.name = "travel-agency"

plugins {
    kotlin("jvm") version "1.5.31" apply false
    id("com.google.protobuf") version "0.8.17" apply false
}

include("hotel-manager-api")
include("hotel-manager")

include("car-rental-api")
include("car-rental")

include("trip-api")
include("trip-manager")

include("travel-cli")
