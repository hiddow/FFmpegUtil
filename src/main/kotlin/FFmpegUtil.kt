@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import data_object.SortedExcelData
import ij.IJ
import net.bramp.ffmpeg.FFprobe
import java.awt.Color
import java.awt.Font
import java.io.File
import java.io.InputStreamReader
import javax.imageio.ImageIO
import kotlin.system.exitProcess


fun main(args: Array<String>) {
//    FFmpegUtil.test()
    FFmpegUtil.main()
}

object FFmpegUtil {

    var isDebug = false
    var isEnglish = false
    var workingDir = ""
    var workingTempDir = ""

    var functionIndex = 2
    var repeatCount = 3
    var repeatGapTime = 3
    var sentenceGapTime = 4
    var startSortIndex = 301
    var endSortIndex = 451

    var inputAudioDirPath = "/Users/lingodeer-yxg/Downloads/YT-Ref-compressed"
    var inputAudioImgDirPath = "/Users/lingodeer-yxg/Downloads/002-ENJP"
    var inputAudioSortExcelPath = "/Users/lingodeer-yxg/Downloads/Youtube句子301-1077.xlsx"

    init {
        val protocol = javaClass.getResource("")?.protocol ?: ""
        if (protocol == "file") {
            isDebug = true
        }
        println("protocol = $protocol")
        workingDir =
            if (isDebug) {
                "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil"
            } else {
                File(
                    FFmpegUtil::class.java.protectionDomain.codeSource.location
                        .toURI()
                ).parent
            }
        workingTempDir = "$workingDir/temp"
        println("workingDir = $workingDir")
        if (workingDir.contains(" ")) {
            println("workingDir不能包含空格")
            exitProcess(0)
        }
    }

    fun test() {
        val ffprobe = FFprobe("$workingDir/library/ffprobe")
        ffprobe.probe("/Users/lingodeer-yxg/Downloads/片头.mp4").format.apply {
            println("this.size = " + this.size)
            println("this.bit_rate = " + this.bit_rate)
            println("this.format_long_name = " + this.format_long_name)
            println("this.format_name = " + this.format_name)
        }

        ffprobe.probe("/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/temp/output/03.mp4").format.apply {
            println("this.size = " + this.size)
            println("this.bit_rate = " + this.bit_rate)
            println("this.format_long_name = " + this.format_long_name)
            println("this.format_name = " + this.format_name)
        }
    }

    fun main() {
//        val scanner = Scanner(System.`in`)
//        println("1. 多个音频文件合并成一个音频文件")
//        println("2. 多个音频文件和图片合并视频文件")
//        println("请选择使用要使用的功能：")
//
//        functionIndex = scanner.nextLine().trim().toInt()
//
//        println("请输入待处理的音频文件夹目录完整路径（路径内不能包含空格以及特殊字符，例如：/Users/lingodeer-yxg/Downloads/YT-input-audio）:")
//        inputAudioDirPath = scanner.nextLine().trim()
//        println("inputAudioDirPath = ${inputAudioDirPath}")
//
//        if (functionIndex == 2) {
//            println("\n请输入待处理的图片文件夹目录完整路径（路径内不能包含空格以及特殊字符，例如：/Users/lingodeer-yxg/Downloads/YT-input-img）:")
//            inputAudioImgDirPath = scanner.nextLine().trim()
//            println("inputAudioDirPath = ${inputAudioImgDirPath}")
//        }
//
//        println("\n请输入待处理的音频排序Excel的完整路径（表格名称固定sorted-list，路径内不能包含空格以及特殊字符，例如：/Users/lingodeer-yxg/Downloads/Youtube句子301-1077.xlsx，如果没有直接回车，默认按照文件名称ID大小进行排序）:")
//        inputAudioSortExcelPath = scanner.nextLine().trim()
//        if (inputAudioSortExcelPath.isEmpty()) {
//            println("排序路径为空，默认按照文件名称ID大小进行排序")
//        } else {
//            println("inputAudioSortExcelPath = $inputAudioSortExcelPath")
//        }
//
//        println("\n请输入每个句子重复的次数：")
//        repeatCount = scanner.nextLine().toInt()
//        println("repeatCount = $repeatCount")
//
//        println("\n请输入句子每遍重复的时间间隔：")
//        repeatGapTime = scanner.nextLine().toInt()
//        println("repeatGapTime = ${repeatGapTime}s")
//
//        println("\n请输入每个句子之间的时间间隔：")
//        sentenceGapTime = scanner.nextLine().toInt()
//        println("sentenceGapTime = ${sentenceGapTime}s")

        combineMedia()
    }

    /**
     * 合成音频
     */
    fun combineMedia() {
        if (inputAudioSortExcelPath.isNotEmpty()) {
            EasyExcel.read(
                File(inputAudioSortExcelPath),
                SortedExcelData::class.java,
                object : ReadListener<SortedExcelData> {
                    val sortedIds = ArrayList<String>()
                    override fun invoke(data: SortedExcelData, context: AnalysisContext) {
                        sortedIds.add(data.ID)
                    }

                    override fun doAfterAllAnalysed(context: AnalysisContext) {
                        if (functionIndex == 2) {
                            combineVideoWithSortedList(sortedIds)
                        } else {
                            combineAudioWithSortedList(sortedIds)
                        }
                    }

                }).sheet("sorted-list").doRead()
        } else {
            if (functionIndex == 2) {
                combineVideoWithSortedList(ArrayList())
            } else {
                combineAudioWithSortedList(ArrayList())
            }
        }
    }


    fun combineAudioWithSortedList(sortedIds: ArrayList<String>) {
        val cmdPath = "$workingDir/library/ffmpeg"

        println("step: 开始压缩音频")
        File("$workingTempDir/compressed_audio").apply {
            if (this.exists()) {
                for (listFile in this.listFiles()) {
                    listFile.delete()
                }
            }
        }
        for (listFile in File(inputAudioDirPath).listFiles()) {
            val outputFile =
                File("$workingTempDir/compressed_audio/${listFile.name}").apply {
                    if (!this.parentFile.exists())
                        this.parentFile.mkdirs()
                }
            val cmd = "$cmdPath -y -i ${listFile.path} ${outputFile.path}"
            Runtime.getRuntime().exec(cmd).apply {
                waitFor()
            }
        }
        println("step: 结束压缩音频")

        println("step: 开始生成间隔音频文件")
        val repeatGameTimeFile = File("$workingTempDir/gap_time/$repeatGapTime.mp3")
        val sentenceGapTimeFile = File("$workingTempDir/gap_time/$sentenceGapTime.mp3")

        for (file in arrayOf(repeatGameTimeFile, sentenceGapTimeFile)) {
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
        }

        Runtime.getRuntime()
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=44100:cl=mono -t $repeatGapTime ${repeatGameTimeFile.path}")
            .apply {
                println(InputStreamReader(this.errorStream).readText())
                waitFor()
            }

        Runtime.getRuntime()
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=44100:cl=mono -t $sentenceGapTime ${sentenceGapTimeFile.path}")
            .apply {
                println(InputStreamReader(this.errorStream).readText())
                waitFor()
            }
        println("step: 结束生成间隔音频文件")

        println("step: 开始合并音频")

        val audioFileList = File("$workingTempDir/compressed_audio").listFiles().filter {
            if (sortedIds.isNotEmpty()) {
                sortedIds.contains(it.name.split(".")[0])
            } else {
                true
            }
        }.sortedBy {
            it.name
        }.sortedWith { t1, t2 ->
            sortedIds.indexOf(t1.name.split(".")[0]) - sortedIds.indexOf(t2.name.split(".")[0])
        }

        val inputAudioList = StringBuilder()
        for (audioFile in audioFileList) {

            for (i in 0 until (repeatCount - 1)) {
                inputAudioList.append("file '${audioFile.path}'")
                inputAudioList.append("\n")
                inputAudioList.append("file '${repeatGameTimeFile.path}'")
                inputAudioList.append("\n")
            }
            inputAudioList.append("file '${audioFile.absolutePath}'")
            inputAudioList.append("\n")
            inputAudioList.append("file '${sentenceGapTimeFile.absolutePath}'")
            inputAudioList.append("\n")
        }
        val inputAudioListFile = File("$workingTempDir/inputAudioList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputAudioList.toString().substring(0, inputAudioList.toString().length - 1)
            )
        }

        val outputAudioFile = File("$workingDir/output/output.mp3").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        Runtime.getRuntime().exec(
            "$cmdPath -y -f concat -safe 0 -i ${inputAudioListFile.path} -c copy ${outputAudioFile.path}"
        ).apply {
            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }
        println("step: 结束合并音频")
        println("输出文件路径：${outputAudioFile.path}")
    }

    /**
     * 合成视频
     */
    fun combineVideoWithSortedList(sortedIds: ArrayList<String>) {

        val ffprobe = FFprobe("$workingDir/library/ffprobe")

        val cmdPath = "$workingDir/library/ffmpeg"

        println("step: 开始压缩音频")
//        File("$workingTempDir/compressed_audio").apply {
//            if (this.exists()) {
//                for (listFile in this.listFiles()) {
//                    listFile.delete()
//                }
//            }
//        }
//        for (listFile in File(inputAudioDirPath).listFiles()) {
//            val outputFile =
//                File("$workingTempDir/compressed_audio/${listFile.name}").apply {
//                    if (!this.parentFile.exists())
//                        this.parentFile.mkdirs()
//                }
//            // ffmpeg -i left.aac -ac 2 output.m4a
//            val cmd = "$cmdPath -y -i ${listFile.path} -ac 2 ${outputFile.path}"
//            Runtime.getRuntime().exec(cmd).apply {
//                waitFor()
//            }
//        }

        println("step: 结束压缩音频")

        println("step: 开始生成间隔音频文件")
        val repeatGameTimeFile = File("$workingTempDir/gap_time/$repeatGapTime.mp3")
        val sentenceGapTimeFile = File("$workingTempDir/gap_time/$sentenceGapTime.mp3")

        for (file in arrayOf(repeatGameTimeFile, sentenceGapTimeFile)) {
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
        }

        Runtime.getRuntime()
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=44100 -t $repeatGapTime ${repeatGameTimeFile.path}")
            .apply {
                println(InputStreamReader(this.errorStream).readText())
                waitFor()
            }

        Runtime.getRuntime()
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=44100 -t $sentenceGapTime ${sentenceGapTimeFile.path}")
            .apply {
                println(InputStreamReader(this.errorStream).readText())
                waitFor()
            }
        println("step: 结束生成间隔音频文件")

        println("step: 开始合并视频")

        val subList = sortedIds.subList(startSortIndex - 1, endSortIndex - 1)

        val audioFileList = File("$workingTempDir/compressed_audio").listFiles().filter {
            if (subList.isNotEmpty()) {
                subList.contains(it.name.split(".")[0])
            } else {
                true
            }
        }.sortedBy {
            it.name
        }.sortedWith { t1, t2 ->
            subList.indexOf(t1.name.split(".")[0]) - subList.indexOf(t2.name.split(".")[0])
        }

        val inputAudioList = StringBuilder()
        val inputImgList = StringBuilder()
//        inputVideoList.append("file '/Users/lingodeer-yxg/Downloads/片头.mp4'")
//        inputVideoList.append("\n")

        audioFileList.forEachIndexed { index, audioFile ->

            val audioFileName = audioFile.name.split(".")[0]
            val probeResult = ffprobe.probe(audioFile.path)
            val fFmpegFormat = probeResult.format
            val curAudioDuration = fFmpegFormat.duration
            var specialGapTimeFile: File? = null
            var specialAudioGapTime = 0.0

            val audioMistakeDuration = (curAudioDuration - curAudioDuration.toInt())
            if (audioMistakeDuration != 0.0) {

                specialAudioGapTime = 1.0 - audioMistakeDuration

                specialGapTimeFile = File("$workingTempDir/gap_time/$specialAudioGapTime.mp3")
                Runtime.getRuntime()
                    .exec("$cmdPath -y -f lavfi -i anullsrc=r=44100 -t $specialAudioGapTime ${specialGapTimeFile.path}")
                    .apply {
                        waitFor()
                    }
            }

            for (i in 0 until 2) {
                inputAudioList.append("file '${audioFile.path}'")
                inputAudioList.append("\n")
                if (specialGapTimeFile != null) {
                    inputAudioList.append("file '${specialGapTimeFile.path}'")
                    inputAudioList.append("\n")
                }
                inputAudioList.append("file '${repeatGameTimeFile.path}'")
                inputAudioList.append("\n")
            }
            inputAudioList.append("file '${audioFile.path}'")
            inputAudioList.append("\n")
            if (specialGapTimeFile != null) {
                inputAudioList.append("file '${specialGapTimeFile.path}'")
                inputAudioList.append("\n")
            }
            inputAudioList.append("file '${sentenceGapTimeFile.path}'")
            inputAudioList.append("\n")

            //002-ENJP.288.png
            val firstPicIndex = (index + 1) * 2 - 1
            val nextPicIndex = (index + 1) * 2


            val fistPicFileName = "002-ENJP.${(1000 + firstPicIndex).toString().substring(1, 4)}.png"
            val secondPicFileName = "002-ENJP.${(1000 + nextPicIndex).toString().substring(1, 4)}.png"


            val correctedAudioDuration = curAudioDuration + specialAudioGapTime

            inputImgList.append("file '$inputAudioImgDirPath/听力.png'")
            inputImgList.append("\n")
            inputImgList.append("duration ${correctedAudioDuration + repeatGapTime + 1}")
            inputImgList.append("\n")
            inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
            inputImgList.append("\n")
            inputImgList.append("duration ${correctedAudioDuration + repeatGapTime + 1}")
            inputImgList.append("\n")
            if (isEnglish) {
                inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
            } else {
                inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
            }
            inputImgList.append("\n")
            inputImgList.append("duration ${correctedAudioDuration + sentenceGapTime + 1}")
            inputImgList.append("\n")
            if (index == audioFileList.size - 1) {
                if (isEnglish) {
                    inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                } else {
                    inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                }
                inputImgList.append("\n")
            }
        }

        val inputAudioListFile = File("$workingTempDir/inputAudioList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputAudioList.toString().substring(0, inputAudioList.toString().length - 1)
            )
        }

        val outputAudioFile = File("$workingDir/output/output.mp3").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        Runtime.getRuntime().exec(
            "$cmdPath -y -f concat -safe 0 -i ${inputAudioListFile.path} -c copy ${outputAudioFile.path}"
        ).apply {
            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }
        val audioDuration = ffprobe.probe(outputAudioFile.path).format.duration

        val inputImgListFile = File("$workingTempDir/inputImgList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputImgList.toString().substring(0, inputImgList.toString().length - 1)
            )
        }

        val outputVideoFile = File("$workingDir/output/output.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

//        val cmd = "$cmdPath -y -f concat -safe 0 -i ${inputImgListFile.path} -i ${outputAudioFile.path} " +
////                "-r 1 " +
//                "-c:a aac " +
////                "-b:a 128k " +
//                "-c:v libx264 -x264-params keyint=1:scenecut=0 " +
//                "-pix_fmt yuv420p " +
//                "${outputVideoFile.path}"


        val cmd = "$cmdPath -y -f concat -safe 0 -i ${inputImgListFile.path} -i ${outputAudioFile.path} -absf aac_adtstoasc -vcodec libx264 -pix_fmt yuv420p ${outputVideoFile.path}"

        println(cmd)

        Runtime.getRuntime().exec(cmd).apply {
//            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }

//        val inputVideoListFile = File("$workingTempDir/inputVideoList.txt").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//            writeText(
//                inputVideoList.toString().substring(0, inputVideoList.toString().length - 1)
//            )
//        }

//        val outputVideoFile = File("$workingDir/output/output.mp4").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//        }
//
//        Runtime.getRuntime().exec(
//            "$cmdPath -y -f concat -safe 0 -i ${inputVideoListFile.path} -c copy ${outputVideoFile.path}"
//        ).apply {
//            println(InputStreamReader(this.errorStream).readText())
//            waitFor()
//        }

        println("step: 结束合并视频")
        println("输出文件路径：${outputVideoFile.path}")
    }

    fun processImg() {
        val font = Font("Arial", Font.BOLD, 180)
        IJ.openImage("/Users/lingodeer-yxg/Downloads/video_frame_2.png").processor.apply {
            setColor(Color.BLACK)
            setFont(font)
            drawString("I can't help falling in love with you.", 660, 1050, Color.WHITE)
            ImageIO.write(this.bufferedImage, "png", File("/Users/lingodeer-yxg/Downloads/video_frame_2_v2.png"))
        }
    }


}