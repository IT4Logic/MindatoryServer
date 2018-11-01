/*
    Copyright (c) 2017, IT4Logic.

    This file is part of Mindatory solution by IT4Logic.

    Mindatory is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Mindatory is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <https://www.gnu.org/licenses/>.

 */

package com.it4logic.mindatory.helpers

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Utility class for compression management
 */
interface ZipManager {

    companion object {

        /**
         * Compressing string using GZIP
         *
         * @param content String to be compressed
         * @return Compressed string in byte array format
         */
        fun gzip(content: String): ByteArray {
            val bos = ByteArrayOutputStream()
            GZIPOutputStream(bos).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
            return bos.toByteArray()
        }

        /**
         * Uncompressing byte array into string
         *
         * @param content Compressed byte array
         * @return Uncompressed byte array in string format
         */
        fun ungzip(content: ByteArray?): String {
            return GZIPInputStream(content?.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }
}