package com.deuna.maven.shared

import CheckoutResponse
import ElementsResponse
import com.deuna.maven.checkout.domain.*
import com.deuna.maven.element.domain.*

typealias OnSuccess<S> = (S) -> Unit
typealias EventListener<S, E> = (E, S) -> Unit
typealias OnError<Error> = (Error) -> Unit
typealias VoidCallback = () -> Unit

open class BaseCallbacks<SuccessData, EventData, Error>{
    var onSuccess: OnSuccess<SuccessData>? = null
    var onError: OnError<Error>? = null
    var onClosed: VoidCallback? = null
    var onCanceled: VoidCallback? = null
    var eventListener: EventListener<SuccessData, EventData>? = null
}

class CheckoutCallbacks : BaseCallbacks<CheckoutResponse, CheckoutEvent, CheckoutError>() {}

class ElementsCallbacks : BaseCallbacks<ElementsResponse, ElementsEvent, ElementsError>() {}