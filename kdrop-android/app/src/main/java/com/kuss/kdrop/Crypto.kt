package com.kuss.kdrop

import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 实现了加密和解密的工具类，包括了加密单个 ByteArray、加密文件流到 OutputStream，
 * 以及解密文件流到 FileOutputStream 的相关方法。
 */
class Crypto {
    companion object {
        /**
         * 对给定的 ByteArray 数据进行解密操作。
         *
         * @param data 需要解密的 ByteArray 数据。
         * @param key 解密所需的密钥，需要保证其长度为 16 个字符，如果少于 16 个字符，则会用字符 a 填充。
         * @return 解密后的 ByteArray 数据，如果解密失败，返回 null。
         */
        fun decrypt(data: ByteArray, key: String): ByteArray? {
            // 获取 AES/CBC/PKCS5Padding 算法的 Cipher 实例。
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            // 创建一个仅包含 0 的 ByteArray，用来作为向量。
            val iv = IvParameterSpec(ByteArray(16))
            // 通过给定的字符串 key 和算法名称实例化一个 SecretKeySpec 对象，用来生成一个 AES 密钥。
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            // 初始化 Cipher 对象，使用密钥和向量以解密模式进行初始化操作。
            cipher.init(Cipher.DECRYPT_MODE, k, iv)
            return try {
                // 使用 Cipher.doFinal() 方法进行解密操作。
                cipher.doFinal(data)
            } catch (e: java.lang.Exception) {
                // 如果解密过程中发生异常，将异常信息打印出来，并返回 null。
                e.printStackTrace()
                null
            }
        }

        /**
         * 对给定的 ByteArray 数据进行加密操作。
         *
         * @param data 需要加密的 ByteArray 数据。
         * @param key 解密所需的密钥，需要保证其长度为 16 个字符，如果少于 16 个字符，则会用字符 a 填充。
         * @return 加密后的 ByteArray 数据。
         */
        fun encrypt(data: ByteArray, key: String): ByteArray {
            // 获取 AES/CBC/PKCS5Padding 算法的 Cipher 实例。
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            // 创建一个仅包含 0 的 ByteArray，用来作为向量。
            val iv = IvParameterSpec(ByteArray(16))
            // 通过给定的字符串 key 和算法名称实例化一个 SecretKeySpec 对象，用来生成一个 AES 密钥。
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            // 初始化 Cipher 对象，使用密钥和向量以加密模式进行初始化操作。
            cipher.init(Cipher.ENCRYPT_MODE, k, iv)
            // 使用 Cipher.doFinal() 方法进行加密操作。
            return cipher.doFinal(data)
        }

        /**
         * 加密输入流数据，并将加密后的数据写入到输出流中。
         *
         * @param iss 需要加密的输入流。
         * @param fos 加密后的数据将会写入到这个输出流中。
         * @param key 加密所使用的密钥，需要保证其长度为 16 个字符，如果少于 16 个字符，则会用字符 a 填充。
         * @return 加密后的数据的 MD5 值，以字符串形式返回。
         */
        fun encrypt(iss: InputStream, fos: OutputStream, key: String): String {
            // 定义一个用于计算文件 MD5 值的 MessageDigest 对象。
            val digest = MessageDigest.getInstance("MD5")
            // 获取 AES/CBC/PKCS5Padding 算法的 Cipher 实例。
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            // 创建一个仅包含 0 的 ByteArray，用来作为向量。
            val iv = IvParameterSpec(ByteArray(16))
            // 通过给定的字符串 key 和算法名称实例化一个 SecretKeySpec 对象，用来生成一个 AES 密钥。
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            // 初始化 Cipher 对象，使用密钥和向量以加密模式进行初始化操作。
            cipher.init(Cipher.ENCRYPT_MODE, k, iv)
            // 创建一个与输入流关联的 CipherInputStream 对象，用于输入流加密操作。
            val cis = CipherInputStream(iss, cipher)
            // 创建一个大小为 1024 * 2 字节的 byte 数组，用来保存加密后的数据。
            val buffer = ByteArray(1024 * 2)
            var length: Int
            // 从输入流中读取数据，并将加密后的数据写入到输出流中。
            while (cis.read(buffer).also { length = it } != -1) {
                fos.write(buffer, 0, length)
                digest.update(buffer, 0, length);
            }
            // 关闭输入流和输出流。
            fos.close()
            cis.close()
            // 计算加密后的数据的 MD5 值，并将其以字符串形式返回。
            val md5Bytes = digest.digest()
            return convertHashToString(md5Bytes)
        }

        /**
         * 对给定的输入流进行解密操作，并将解密后的数据写入到给定的 FileOutputStream 对象中。
         *
         * @param iss 需要解密的输入流。
         * @param fos 解密后的数据将会写入到指定的 FileOutputStream 中。
         * @param key 解密所需的密钥，需要保证其长度为 16 个字符，如果少于 16 个字符，则会用字符 a 填充。
         */
        fun decrypt(iss: InputStream, fos: FileOutputStream, key: String) {
            // 获取 AES/CBC/PKCS5Padding 算法的 Cipher 实例。
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            // 创建一个仅包含 0 的 ByteArray，用来作为向量。
            val iv = IvParameterSpec(ByteArray(16))
            // 通过给定的字符串 key 和算法名称实例化一个 SecretKeySpec 对象，用来生成一个 AES 密钥。
            val k = SecretKeySpec(key.padEnd(16, 'a').toByteArray(), "AES")
            // 初始化 Cipher 对象，使用密钥和向量以解密模式进行初始化操作。
            cipher.init(Cipher.DECRYPT_MODE, k, iv)
            // 创建一个使用给定的 Cipher 对象进行解密的 CipherOutputStream 输出流。
            val cos = CipherOutputStream(fos, cipher)
            // 创建一个大小为 1024 字节的 byte 数组，用来保存解密后的数据。
            val buffer = ByteArray(1024)
            var i: Int
            // 从输入流中读取数据，并对其进行解密操作，并将解密后的数据写入到输出流中。
            while (iss.read(buffer).also { i = it } != -1) {
                cos.write(buffer, 0, i)
            }
            // 关闭输入流和输出流。
            fos.close()
            cos.close()
        }

        /**
         * 根据给定的 byte 数组，将其转换成十六进制字符串，并将其以字符串形式返回。
         *
         * @param md5Bytes 需要转换成字符串的 byte 数组。
         * @return 给定的 byte 数组转换后的十六进制字符串（大写字母形式）。
         */
        private fun convertHashToString(md5Bytes: ByteArray): String {
            var returnVal = ""
            for (i in md5Bytes.indices) {
                returnVal += ((md5Bytes[i].toInt() and 0xff) + 0x100).toString(16)
                    .substring(1)
            }
            return returnVal.uppercase(Locale.getDefault())
        }
    }
}

