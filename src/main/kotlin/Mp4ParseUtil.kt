import java.io.*
import java.lang.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.also


object Mp4ParseUtil {

    val ffmpegPath = "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/library/ffmpeg"

    fun mergeVideo(list: List<String>, outputDir: String, outputFile: String): String? {
        return try {
            val format1 = "%s -y -i %s -c copy -bsf:v h264_mp4toannexb -f mpegts %s"
            val commandList: ArrayList<String> = ArrayList(6)
            val inputList: ArrayList<String> = ArrayList(6)

            for (i in list.indices) {
                val input = String.format("input%d.ts", i + 1)
                val command = java.lang.String.format(format1, ffmpegPath, list[i], outputDir + input)
                println(command)
                commandList.add(command)
                inputList.add(input)
            }
            val command: String = getCommand(outputDir, outputFile, inputList)
            println(command)
            commandList.add(command)
            var falg = Boolean.FALSE
            for (i in 0 until commandList.size) {
                if (execCommand(commandList[i]) > 0) falg = true
            }
            if (falg) {
                for (i in 0 until inputList.size) {
                    if (i != commandList.size - 1) {
                        val file = File(outputDir + inputList[i])
                        file.delete()
                    }
                }
                outputFile
            } else {
                "fail"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "fail"
        }
    }

    /**
     * 拼接执行新的videoUrl command命令
     * @param outputFile
     * @param newfilePath
     * @param inputList
     * @date: 2021/4/17 11:29
     * @return: java.lang.StringBuffer
     */
    private fun getCommand(outputFile: String, newfilePath: String, inputList: List<String>): String {
        val tsPath = StringBuffer()
        tsPath.append(ffmpegPath)
        tsPath.append(" -y ")
        tsPath.append(" -i ")
        tsPath.append("\"")
        tsPath.append("concat:")
        for (t in inputList.indices) {
            tsPath.append(outputFile)
            if (t != inputList.size - 1) {
                tsPath.append(inputList[t] + "|")
            } else {
                tsPath.append(inputList[t])
            }
        }
        tsPath.append("\"")
        tsPath.append(" -c copy -bsf:a aac_adtstoasc -movflags +faststart ")
        tsPath.append(newfilePath)
        return tsPath.toString()
    }

    private fun execCommand(command: String): Int {
        return try {
            val process = Runtime.getRuntime().exec(command)
            //获取进程的标准输入流
            val is1: InputStream = process.inputStream
            //获取进城的错误流
            val is2: InputStream = process.errorStream
            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
            readInputStream(is1)
            readInputStream(is2)
            process.waitFor()
            process.destroy()
            1
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            println("-----操作失败$command")
            -1
        }
    }


    private fun readInputStream(inputStream: InputStream) {
        Thread {
            val br1 = BufferedReader(InputStreamReader(inputStream))
            try {
                var line1: String?
                while (br1.readLine().also { line1 = it } != null) {
                    if (line1 != null) {
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }

}