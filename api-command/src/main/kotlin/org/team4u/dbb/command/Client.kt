package org.team4u.dbb.command


object Client {

    @JvmStatic fun main(args: Array<String>) {
        org.team4u.dbb.command.Commands.execute(args)
    }
}