package com.deuna.maven.wallets

data class WalletsError(val code: String, val message: String) {
    companion object {
        fun fetchFailed(cause: String) = WalletsError(
            code = "WALLETS_FETCH_FAILED",
            message = cause,
        )
    }
}
