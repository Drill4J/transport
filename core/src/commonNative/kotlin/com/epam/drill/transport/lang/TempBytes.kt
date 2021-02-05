/**
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.transport.lang

import com.epam.drill.transport.Pool
import kotlin.native.concurrent.ThreadLocal


@ThreadLocal
internal val smallBytesPool = Pool(preallocate = 16) { ByteArray(16) }


inline fun <T, R> Pool<T>.alloc2(callback: (T) -> R): R {
    val temp = alloc()
    try {
        return callback(temp)
    } finally {
        free(temp)
    }
}


inline fun <T, R> Pool<T>.allocThis(callback: T.() -> R): R {
    val temp = alloc()
    try {
        return callback(temp)
    } finally {
        free(temp)
    }
}
