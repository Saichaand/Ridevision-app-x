tasks.register<Exec>("npmBuild") {
    commandLine("npm", "run", "build")
}

tasks.register("preBuild") {
    doLast {
        println("preBuild completed.")
    }
}

tasks.register("assembleDebug") {
    dependsOn("npmBuild")
    doLast {
        println("RideVision web bundle and assembleDebug completed successfully.")
    }
}

tasks.register("assemble") {
    dependsOn("assembleDebug")
}

tasks.register("build") {
    dependsOn("assemble")
}
