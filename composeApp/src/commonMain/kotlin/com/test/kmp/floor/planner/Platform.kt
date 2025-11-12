package com.test.kmp.floor.planner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform