plugins {
    application
    checkstyle
    java
    jacoco
    id("org.springframework.boot")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("main.Main")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter:4.1.1")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("net.bytebuddy.experimental", "true")
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
        xml.required = true
    }
}

// Критерий задания: покрытие тестами не ниже 60 %.
// Проверка запускается в составе gradle check / build.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

val sunChecks = configurations.detachedConfiguration(
    dependencies.create("com.puppycrawl.tools:checkstyle:14.1.0")
).apply {
    isTransitive = false
}

checkstyle {
    toolVersion = "14.1.0"
    config = resources.text.fromArchiveEntry(
        sunChecks,
        "sun_checks.xml"
    )
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

tasks.named("checkstyleTest") {
    enabled = false
}

tasks.named<JavaExec>("bootRun") {
    standardInput = System.`in`
    jvmArgs(
        "-Dfile.encoding=UTF-8",
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8"
    )
}
