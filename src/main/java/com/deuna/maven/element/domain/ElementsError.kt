package com.deuna.maven.element.domain

import ElementsResponse
import com.deuna.maven.shared.*

data class ElementsError(
    var type: ElementsErrorType,
    var user: ElementsResponse.User?,
)

