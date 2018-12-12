package com.exoscale.api

interface Command<T: Result> {
    val command: String
    val resultType: Class<T>
}

interface Result