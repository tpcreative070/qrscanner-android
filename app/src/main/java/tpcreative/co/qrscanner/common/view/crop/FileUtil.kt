package tpcreative.co.qrscanner.common.view.crop

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import tpcreative.co.qrscanner.common.Utils
import java.io.*


class FileUtil {
    private val EOF = -1
    private val DEFAULT_BUFFER_SIZE = 1024 * 4
    private val TAG = this::class.simpleName
    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val splitName = splitFileName(fileName)
        var tempFile: File? = null
        var out: FileOutputStream? = null
        try {
        tempFile = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
            out = FileOutputStream(tempFile)
        } catch (e: Exception) {
            e.printStackTrace()
            context.cacheDir.mkdirs()
        }
        if (inputStream != null) {
            if (out != null) {
                copy(inputStream, out)
            }
            inputStream.close()
        }
        out?.close()
        return tempFile
    }

    private fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }
        return arrayOf(name, extension)
    }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.getPath()
            val cut: Int? = result?.lastIndexOf(File.separator)
            if (cut != -1) {
                if (cut != null) {
                    result = result?.substring(cut + 1)
                }
            }
        }
        return result ?: "None"
    }

    private fun rename(file: File, newName: String): File {
        val newFile = File(file.parent, newName)
        if (newFile != file) {
            if (newFile.exists() && newFile.delete()) {
                Utils.Log(TAG, "Delete old $newName file")
            }
            if (file.renameTo(newFile)) {
                Utils.Log(TAG, "Rename file to $newName")
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream): Long {
        var count: Long = 0
        var n: Int
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (EOF != input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
            count += n.toLong()
        }
        return count
    }
}