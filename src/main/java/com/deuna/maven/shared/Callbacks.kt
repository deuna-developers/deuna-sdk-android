package com.deuna.maven.shared

import com.deuna.maven.checkout.domain.*
import com.deuna.maven.element.domain.*

typealias OnSuccess<S> = (S) -> Unit
typealias OnEventDispatch<S, E> = (E, S) -> Unit
typealias OnError<Error> = (Error) -> Unit
typealias VoidCallback = () -> Unit

open class BaseCallbacks<SuccessData, EventData, Error>{
    var onSuccess: OnSuccess<SuccessData>? = null
    var onError: OnError<Error>? = null
    var onClosed: VoidCallback? = null
    var onCanceled: VoidCallback? = null

    @Deprecated(
        message = "This property will be removed in the future. Use onEventDispatch instead",
        replaceWith = ReplaceWith("onEventDispatch")
    )
    var eventListener: OnEventDispatch<SuccessData, EventData>? = null

    var onEventDispatch: OnEventDispatch<SuccessData, EventData>? = null
}

class CheckoutCallbacks : BaseCallbacks<Json, CheckoutEvent, PaymentsError>() {}

class ElementsCallbacks : BaseCallbacks<Json, ElementsEvent, ElementsError>() {}