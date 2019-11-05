package com.loenzo.serialtest2.util

import java.util.concurrent.Executors


private val IO_EXECUTOR = Executors.newSingleThreadExecutor()
private val DAO_EXECUTOR = Executors.newSingleThreadExecutor()

fun ioThread(f : () -> Unit) {
    IO_EXECUTOR.execute(f)
}

fun daoThread(f : () -> Unit) {
    DAO_EXECUTOR.execute(f)
}