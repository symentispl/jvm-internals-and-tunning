tasks.register<Exec>("concurrencyPrimitives") {
    workingDir("modules/concurrency-primitives")
    commandLine = listOf("./mvnw", "-B", "verify")
}

tasks.register<Exec>("voyeursInJvmLand") {
    workingDir("modules/voyeurs-in-jvm-land")
    commandLine = listOf("./mvnw", "-B", "verify")
}

tasks.register("buildModules") {
    dependsOn("concurrencyPrimitives", "voyeursInJvmLand")
}