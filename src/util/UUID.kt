package fr.spoutnik87.util

class UUID {
    companion object {
        fun v4() = java.util.UUID.randomUUID().toString()
    }
}