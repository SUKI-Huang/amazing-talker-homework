val ktlint by configurations.creating
val ktlintCheck by tasks.creating(JavaExec::class) {
    group = "verification"
    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("src/**/*.kt", "--reporter=plain", "--reporter=checkstyle,output=${buildDir}/ktlint.xml")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    group = "formatting"
    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"
    args = listOf("-F", "src/**/*.kt")
}

dependencies { ktlint("com.pinterest:ktlint:0.42.1") }

tasks.getByName("check") { dependsOn(ktlintCheck) }