package com.nullinnix.clippr

class JVMPlatform {
    val name: String = "Java ${System.getProperty("java.version")}"
}

fun getPlatform() = JVMPlatform()