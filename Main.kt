package svcs

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun main(args: Array<String>) { ControlSystem().start(args) }

class ControlSystem {

    private val root = File(System.getProperty("user.dir"))
    private val vcs = File("vcs")
    private val commits = File("vcs\\commits")
    private val config = File("vcs\\config.txt")
    private val index = File("vcs\\index.txt")
    private val log = File("vcs\\log.txt")

    init {
        if (!vcs.exists()) vcs.mkdir()
        if (!commits.exists()) commits.mkdir()
        if (!config.exists()) config.writeText(" ")
    }

    fun start(args: Array<String>) {

        val username = config.readLines()

        if (args.isEmpty()) help() else
            when (args[0]) {
                "--help" -> help()
                "add" -> add(args)
                "checkout" -> checkout(args)
                "commit" -> commit(args, username)
                "log" -> log()
                "config" -> config(args, username)
                else -> println("'${args[0]}' is not a SVCS command.")
            }

    }

    private fun help() {
        println(
            """
                    These are SVCS commands:
                    config     Get and set a username.
                    add        Add a file to the index.
                    log        Show commit logs.
                    commit     Save changes.
                    checkout   Restore a file.
                """.trimIndent()
        )
    }

    private fun add(args: Array<String>) {
        when (args.size) {
            1 -> if (!index.exists()) println("Add a file to the index.") else println("Tracked files:\n" + index.readText())
            2 -> {
                if (!File(args[1]).exists()) {
                    println("Can't find '${args[1]}'.")
                } else if (!index.exists() || index.length() == 0L) {
                    index.writeText(args[1])
                    println("The file '${args[1]}' is tracked.")
                } else {
                    index.appendText("\n" + args[1])
                    println("The file '${args[1]}' is tracked.")
                }
            }
        }
    }

    private fun checkout(args: Array<String>) {
        when (args.size) {
            1 -> println("Commit id was not passed.")
            2 -> {
                val co = commits.list()!!.toList()
                if (args[1] in co) {
                    for (file in commits.resolve(args[1]).listFiles()!!) root.resolve(
                        file.name
                    ).writeText(file.readText())
                    println("Switched to commit ${args[1]}.")
                } else println("Commit does not exist.")
            }
        }
    }

    private fun commit(args: Array<String>, username: List<String>) {
        when (args.size) {
            1 -> println("Message was not passed.")
            2 -> {
                var text = ""
                val shifr = MessageDigest.getInstance("SHA-256")
                for (s in index.readLines()) text += File(s).readText()
                val hash = shifr.digest(text.toByteArray())
                val hashtext = BigInteger(1, hash).toString(16)
                if (!commits.resolve(hashtext).exists() && index.length() != 0L) {
                    commits.resolve(hashtext).mkdir()
                    index.forEachLine { commits.resolve("$hashtext\\$it").writeText(File(it).readText()) }
                    if (!log.exists()) {
                        log.writeText("commit $hashtext" + "\nAuthor: ${username[0]}" + "\n${args[1]}\n")
                    } else {
                        val logtext = log.readText()
                        log.writeText("commit $hashtext" + "\nAuthor: ${username[0]}" + "\n${args[1]}\n")
                        log.appendText("\n" + logtext)
                    }
                    println("Changes are committed.")
                } else println("Nothing to commit.")
            }
        }
    }

    private fun log() {
        if (commits.list()!!.isEmpty()) {
            println("No commits yet.")
        } else {
            log.forEachLine { println(it) }
        }
    }

    private fun config(args: Array<String>, username: List<String>) {
        when (args.size) {
            1 -> if (username[0] == " ") println("Please, tell me who you are.") else println("The username is ${username[0]}.")
            2 -> {
                println("The username is ${args[1]}.")
                config.writeText(args[1])
            }
        }
    }

