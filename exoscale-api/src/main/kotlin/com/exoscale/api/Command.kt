package com.exoscale.api

interface Command<T: Result> {
    val commandId: String
    val resultType: Class<T>
}

interface Result