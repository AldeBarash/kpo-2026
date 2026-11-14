plugins { base }

tasks.register("test") {
    dependsOn(":modules:practise-10-res:gateway:test", ":modules:practise-10-res:telegram-service:test")
}
tasks.register("bootJar") {
    dependsOn(":modules:practise-10-res:gateway:bootJar", ":modules:practise-10-res:telegram-service:bootJar")
}
tasks.named("check") { dependsOn("test") }
tasks.named("assemble") { dependsOn("bootJar") }
