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

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.1.1")
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
    config = resources.text.fromFile(
        layout.buildDirectory.file("checkstyle/sun_checks.xml").get().asFile
    )
    isShowViolations = true
    isIgnoreFailures = false
    maxWarnings = 0
}

val checkstyleConfig = layout.buildDirectory.file("checkstyle/sun_checks.xml")

val prepareCheckstyleConfig = tasks.register("prepareCheckstyleConfig") {
    inputs.files(sunChecks)
    outputs.file(checkstyleConfig)
    doLast {
        val standardConfig = resources.text
            .fromArchiveEntry(sunChecks, "sun_checks.xml")
            .asString()
        val configWithoutPackageInfoRule = standardConfig
            .replace("<module name=\"JavadocPackage\"/>", "")
            .replace("<module name=\"JavadocVariable\"/>", "")
        checkstyleConfig.get().asFile.apply {
            parentFile.mkdirs()
            writeText(configWithoutPackageInfoRule)
        }
    }
}

tasks.withType<Checkstyle>().configureEach {
    dependsOn(prepareCheckstyleConfig)
    reports {
        html.required = true
        xml.required = false
    }
}
