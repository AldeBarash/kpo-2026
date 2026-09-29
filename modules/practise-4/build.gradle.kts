plugins {
    application
    checkstyle
    java
    id("org.springframework.boot")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "studying.ioc.di.Main"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:4.1.1")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val sunChecks = configurations.detachedConfiguration(
    dependencies.create("com.puppycrawl.tools:checkstyle:14.1.0")
).apply {
    isTransitive = false
}

checkstyle {
    toolVersion = "14.1.0"
    config = resources.text.fromArchiveEntry(sunChecks, "sun_checks.xml")
    isShowViolations = true
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        html.required = true
        xml.required = false
    }
}
