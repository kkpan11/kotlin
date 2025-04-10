/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.utils

import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import java.io.DataInputStream
import java.io.File
import java.io.InputStream
import java.util.*

class KotlinJavascriptMetadata(val version: JsMetadataVersion, val moduleName: String, val body: ByteArray)

/**
 * The version of the format in which the `js.proto` is stored. This version also includes the version of the core protobuf messages
 * (`metadata.proto`).
 *
 * This version must be bumped when:
 * - Incompatible changes are made in `js.proto`
 * - Incompatible changes are made in `metadata.proto`
 * - Incompatible changes are made in JS metadata serialization/deserialization logic
 *
 * This version must **NOT** be bumped when:
 * - Incompatible changes are made in `js-ast.proto`. `js-ast.proto` is and internal format used for incremental compilation caches
 *
 * The version bump must obey [org.jetbrains.kotlin.metadata.deserialization.BinaryVersion] rules (See `BinaryVersion` KDoc).
 */
class JsMetadataVersion(vararg numbers: Int) : BinaryVersion(*numbers) {
    override fun isCompatibleWithCurrentCompilerVersion(): Boolean =
        this.isCompatibleTo(INSTANCE)

    fun toInteger() = (patch shl 16) + (minOf(minor, 255) shl 8) + minOf(major, 255)

    companion object {
        @JvmField
        val INSTANCE = JsMetadataVersion(1, 2, 6)

        @JvmField
        val INVALID_VERSION = JsMetadataVersion()

        fun fromInteger(version: Int): JsMetadataVersion =
                JsMetadataVersion(version and 255, (version shr 8) and 255, version shr 16)

        fun readFrom(stream: InputStream): JsMetadataVersion {
            val dataInput = DataInputStream(stream)
            val size = dataInput.readInt()

            // We assume here that the version will always have 3 components. This is needed to prevent reading an unpredictable amount
            // of integers from old .kjsm files (pre-1.1) because they did not have the version in the beginning
            if (size != INSTANCE.toArray().size) return INVALID_VERSION

            return JsMetadataVersion(*(1..size).map { dataInput.readInt() }.toIntArray())
        }
    }
}

object KotlinJavascriptMetadataUtils {
    const val JS_EXT: String = ".js"
    const val META_JS_SUFFIX: String = ".meta.js"
    const val JS_MAP_EXT: String = ".js.map"
    private val KOTLIN_JAVASCRIPT_METHOD_NAME = "kotlin_module_metadata"
    private val KOTLIN_JAVASCRIPT_METHOD_NAME_PATTERN = "\\.kotlin_module_metadata\\(".toPattern()

    /**
     * Matches string like <name>.kotlin_module_metadata(<abi version>, <module name>, <base64 data>)
     */
    private val METADATA_PATTERN = "(?m)\\w+\\.$KOTLIN_JAVASCRIPT_METHOD_NAME\\((\\d+),\\s*(['\"])([^'\"]*)\\2,\\s*(['\"])([^'\"]*)\\4\\)".toPattern()

    @JvmStatic
    fun loadMetadata(file: File): List<KotlinJavascriptMetadata> {
        assert(file.exists()) { "Library $file not found" }
        val metadataList = arrayListOf<KotlinJavascriptMetadata>()
        JsLibraryUtils.traverseJsLibrary(file) { library ->
            parseMetadata(library.content, metadataList)
        }

        return metadataList
    }

    @JvmStatic
    fun loadMetadata(path: String): List<KotlinJavascriptMetadata> = loadMetadata(File(path))

    @JvmStatic
    fun parseMetadata(text: CharSequence, metadataList: MutableList<KotlinJavascriptMetadata>) {
        // Check for literal pattern first in order to reduce time for large files without metadata
        if (!KOTLIN_JAVASCRIPT_METHOD_NAME_PATTERN.matcher(text).find()) return

        val matcher = METADATA_PATTERN.matcher(text)
        while (matcher.find()) {
            val abiVersion = JsMetadataVersion.fromInteger(matcher.group(1).toInt())
            val moduleName = matcher.group(3)
            val data = matcher.group(5)
            metadataList.add(KotlinJavascriptMetadata(abiVersion, moduleName, Base64.getDecoder().decode(data)))
        }
    }
}
