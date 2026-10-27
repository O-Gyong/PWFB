package com.pwfb.domain.repository

/**
 * 잡힌 예외를 외부 리포팅 도구(Crashlytics 등)로 보낸다. 도메인 계층이 Firebase에 직접 의존하지 않도록 분리.
 */
fun interface ErrorReporter {
    fun report(e: Throwable)
}
