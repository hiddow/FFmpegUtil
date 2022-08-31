@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import data_object.SortedExcelData
import ij.IJ
import ij.process.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.bramp.ffmpeg.FFprobe
import java.awt.*
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.imageio.ImageIO
import kotlin.system.exitProcess


fun main(args: Array<String>) {
//    FFmpegUtil.test()

    for (i in 2 until 7) {
        FFmpegImgToVideoUtil.bgIndex = i + 1
        val count = if (i == 6) {
            177
        } else {
            150
        }
        FFmpegImgToVideoUtil.startSortIndex = (i * 150) + 1
        FFmpegImgToVideoUtil.endSortIndex = FFmpegImgToVideoUtil.startSortIndex + count

        FFmpegImgToVideoUtil.isEnglish = true
        FFmpegImgToVideoUtil.outputFileName =
            "output(${FFmpegImgToVideoUtil.startSortIndex}-${FFmpegImgToVideoUtil.endSortIndex - 1})-EN.mp4"
        FFmpegImgToVideoUtil.main()

        FFmpegImgToVideoUtil.isEnglish = false
        FFmpegImgToVideoUtil.outputFileName =
            "output(${FFmpegImgToVideoUtil.startSortIndex}-${FFmpegImgToVideoUtil.endSortIndex - 1})-EN-JP.mp4"
        FFmpegImgToVideoUtil.main()
    }

//    FFmpegImgToVideoUtil.main()

//    val videoList = listOf(
//        "/Users/lingodeer-yxg/Desktop/FFmpegUtil/output/enpal-片头.mp4",
//        "/Users/lingodeer-yxg/Desktop/FFmpegUtil/output/output-formatted.mp4",
//        "/Users/lingodeer-yxg/Desktop/FFmpegUtil/output/enpal-片尾.mp4"
//    )
//
//    Mp4ParseUtil.mergeVideo(videoList, "/Users/lingodeer-yxg/Desktop/FFmpegUtil/output/", "output-full.mp4")
//    FFmpegUtil2.processImg()
}

object FFmpegImgToVideoUtil {
    var bgIndex = 1
    var isDebug = false
    var isEnglish = false
    var workingDir = ""
    var workingTempDir = ""

    var functionIndex = 2
    var repeatCount = 3
    var repeatGapTime = 3
    var sentenceGapTime = 4
    var startSortIndex = 1
    var endSortIndex = 151

    var outputFileName = "output(1-150).mp4"
    var inputAudioDirPath = "/Users/lingodeer-yxg/Downloads/YT-Ref-compressed"
    var inputAudioSortExcelPath = "/Users/lingodeer-yxg/Desktop/FFmpegUtil/1077-with-Japanese(排序后).xlsx"

    init {
        val protocol = javaClass.getResource("")?.protocol ?: ""
        if (protocol == "file") {
            isDebug = true
        }
        println("protocol = $protocol")
        workingDir =
            if (isDebug) {
                "/Users/lingodeer-yxg/Desktop/FFmpegUtil"
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

        ffprobe.probe("/Users/lingodeer-yxg/Desktop/FFmpegUtil/temp/output/03.mp4").format.apply {
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
                    val sortedExcelDataList = ArrayList<SortedExcelData>()
                    override fun invoke(data: SortedExcelData, context: AnalysisContext) {
                        sortedExcelDataList.add(data)
                        println(data.SENTENCE)
                    }

                    override fun doAfterAllAnalysed(context: AnalysisContext) {
                        combineVideoWithSortedList(sortedExcelDataList)
                    }

                }).sheet("1077 WITH Japanese").doRead()
        } else {
            combineVideoWithSortedList(ArrayList())
        }
    }

    /**
     * 合成视频
     */
    fun combineVideoWithSortedList(sortedExcelDataList: ArrayList<SortedExcelData>) {
        var startTime = System.currentTimeMillis()
        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("$workingDir/library/ffprobe")

        val cmdPath = "$workingDir/library/ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"

        println("step: 开始压缩音频")

        File(workingTempDir).apply {
            for (listFile in listFiles()) {
                if (listFile.isDirectory) {
                    for (childListFile in listFile.listFiles()) {
                        childListFile.delete()
                    }
                } else {
                    listFile.delete()
                }
            }
        }

        runBlocking {
            for (listFile in File(inputAudioDirPath).listFiles()) {
                launch(Dispatchers.IO) {
                    val outputFile =
                        File("$workingTempDir/compressed_audio/${(listFile.name.split(".")[0].toInt())}.mp3").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }
                    // ffmpeg -i left.aac -ac 2 output.m4a
                    val cmd = "$cmdPath -y -i ${listFile.path} -ac 2 -ar 48000 ${outputFile.path}"
                    println(cmd)
                    Runtime.getRuntime().exec(cmd).apply {
                        waitFor()
                    }
                }
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
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $repeatGapTime ${repeatGameTimeFile.path}")
            .apply {
                waitFor()
            }

        Runtime.getRuntime()
            .exec("$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $sentenceGapTime ${sentenceGapTimeFile.path}")
            .apply {
                waitFor()
            }

        println("step: 结束生成间隔音频文件")

        println("step: 开始生成图片")

        val subList = sortedExcelDataList.subList(startSortIndex - 1, endSortIndex - 1)

        subList.forEachIndexed { index, sortedExcelData ->
            outputImg(index, sortedExcelData)
        }

        val audioFileList = subList
            .map {
                File("$workingTempDir/compressed_audio/${it.ID}.mp3")
            }
        println("step: 结束生成图片")

        println("step: 开始合并视频")

        val inputVideoList = StringBuilder()
//        val inputImgList = StringBuilder()
//        val inputAudioList = StringBuilder()

        for (list in Utils.averageAssignFixLength(audioFileList, 15)) {
            runBlocking(Dispatchers.IO) {
                list.forEachIndexed { index, audioFile ->
                    launch(Dispatchers.IO) {
                        if (!audioFile.exists()) {
                            println(audioFile.path)
                        }
                        val inputAudioList = StringBuilder()
                        val inputImgList = StringBuilder()

                        val audioFileName = audioFile.name.split(".")[0]
                        val probeResult = ffprobe.probe(audioFile.path)
                        val fFmpegFormat = probeResult.format
                        val curAudioDuration = fFmpegFormat.duration
                        var specialGapTimeFile: File? = null
                        var specialAudioGapTime = 0.0

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

                        val fistPicFileName = "${audioFileName}-pic1.png"
                        val secondPicFileName = "${audioFileName}-pic2.png"


                        val correctedAudioDuration = curAudioDuration + specialAudioGapTime

                        inputImgList.append("file '$workingDir/background_img/bg_listen.png'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${correctedAudioDuration + repeatGapTime}")
                        inputImgList.append("\n")
                        inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${correctedAudioDuration + repeatGapTime}")
                        inputImgList.append("\n")
                        if (isEnglish) {
                            inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        } else {
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                        }
                        inputImgList.append("\n")
                        inputImgList.append("duration ${correctedAudioDuration + sentenceGapTime + 3}")
                        inputImgList.append("\n")

                        if (isEnglish) {
                            inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        } else {
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                        }
                        inputImgList.append("\n")


                        val inputAudioListFile =
                            File("$workingTempDir/input_txt/inputAudioList-${audioFileName}.txt").apply {
                                if (!this.parentFile.exists())
                                    this.parentFile.mkdirs()
                                writeText(
                                    inputAudioList.toString().substring(0, inputAudioList.toString().length - 1)
                                )
                            }

                        val outputAudioFile = File("$workingTempDir/output/output-${audioFileName}.mp3").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }

                        var cmd =
                            "$cmdPath -y -f concat -safe 0 -i ${inputAudioListFile.path} -c copy ${outputAudioFile.path}"

                        Runtime.getRuntime().exec(
                            cmd
                        ).apply {
                            waitFor()
                        }

                        val inputImgListFile =
                            File("$workingTempDir/input_txt/inputImgList-${audioFileName}.txt").apply {
                                if (!this.parentFile.exists())
                                    this.parentFile.mkdirs()
                                writeText(
                                    inputImgList.toString().substring(0, inputImgList.toString().length - 1)
                                )
                            }

                        val outputVideoFile = File("$workingTempDir/output/output-${audioFileName}.mp4").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }

                        cmd = "$cmdPath -y -f " +
                                "concat -safe 0 " +
                                "-i ${inputImgListFile.path} " +
//                                "-i ${outputAudioFile.path} " +
                                "-r 10 " +
                                "-vcodec libx264 " +
                                "-pix_fmt yuv420p " +
                                outputVideoFile.path
                        println(cmd)
                        Runtime.getRuntime().exec(cmd).apply {
                            waitFor()
                        }

                        val keyOutputVideoFile = File("$workingDir/temp/output/key-output-${audioFileName}.mp4").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }

                        cmd =
                            "$cmdPath -y -ss 0 -t ${ffprobe.probe(outputAudioFile.path).format.duration.toInt()} -accurate_seek -i ${outputVideoFile.path} -codec copy -avoid_negative_ts 1 ${keyOutputVideoFile.path}"
                        println(cmd)
                        Runtime.getRuntime().exec(cmd).apply {
                            waitFor()
                        }

                        val outputVideoFormattedFile =
                            File("$workingTempDir/output/formatted-output-${audioFileName}.mp4").apply {
                                if (!this.parentFile.exists())
                                    this.parentFile.mkdirs()
                            }

                        cmd =
                            "$cmdPath -y -i ${keyOutputVideoFile.path} -i ${outputAudioFile.path} ${outputVideoFormattedFile.path}"
                        println(cmd)
                        Runtime.getRuntime().exec(cmd).apply {
                            waitFor()
                        }


//                        cmd =
//                            "$cmdPath -y -i ${outputVideoFile.path} ${outputVideoFormattedFile.path}"
//                        println(cmd)
//                        Runtime.getRuntime().exec(cmd).apply {
//                            waitFor()
//                        }

//                        val handBrakeOutputVideoFile =
//                            File("$workingDir/temp/output/hanbreakeoutput-${audioFileName}.mp4").apply {
//                                if (!this.parentFile.exists())
//                                    this.parentFile.mkdirs()
//                            }
//                        cmd =
//                            "$hanBrakePath -i ${outputVideoFile.path} -o ${handBrakeOutputVideoFile.path} -e x264 -q 30 -B 160"
//                        println(cmd)
//                        Runtime.getRuntime().exec(cmd).apply {
//                            waitFor()
//                        }
                    }
                }
            }
        }


//        inputVideoList.append("file '$workingDir/output/enpal-片头.mp4'")
//        inputVideoList.append("\n")

        for (audioFile in audioFileList) {
            val audioFileName = audioFile.name.split(".")[0]
            val outputVideoFile = File("$workingDir/temp/output/formatted-output-${audioFileName}.mp4")
//            val outputAudioFile = File("$workingDir/temp/output/output-${audioFileName}.mp3")

            inputVideoList.append("file '${outputVideoFile.path}'")
            inputVideoList.append("\n")
        }

//        inputVideoList.append("file '$workingDir/output/enpal-片尾.mp4'")
//        inputVideoList.append("\n")


        val inputVideoListFile = File("$workingTempDir/inputVideoList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputVideoList.toString().substring(0, inputVideoList.toString().length - 1)
            )
        }

        val outputVideoFile = File("$workingDir/output/output.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        var cmd = "$cmdPath -y -f concat -safe 0 -i ${inputVideoListFile.path} -c copy ${outputVideoFile.path}"

        println(cmd)

        Runtime.getRuntime().exec(cmd).apply {
            println(InputStreamReader(this.inputStream).readText())
            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }

        val outputVideoHandBrakeFile = File("$workingDir/output/output-handbrake.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        cmd = "$hanBrakePath -i ${outputVideoFile.path} -o ${outputVideoHandBrakeFile.path} -e x264 -q 30 -B 160"

        Runtime.getRuntime().exec(cmd).apply {
            println(InputStreamReader(this.inputStream).readText())
            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }

        val outputVideoFormattedFile = File("$workingDir/output/${outputFileName}").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        cmd = "$cmdPath -y -i ${outputVideoHandBrakeFile.path} ${outputVideoFormattedFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(
            cmd
        ).apply {
            println(InputStreamReader(this.inputStream).readText())
            println(InputStreamReader(this.errorStream).readText())
            waitFor()
        }

//        val inputVideoFinalList = StringBuilder()
//        inputVideoFinalList.append("file '${workingDir}/output/enpal-片头.mp4'")
//        inputVideoFinalList.append("\n")
//        inputVideoFinalList.append("file '${outputVideoFormattedFile.path}'")
//        inputVideoFinalList.append("\n")
//        inputVideoFinalList.append("file '${workingDir}/output/enpal-片尾.mp4'")
//        inputVideoFinalList.append("\n")
//
//
//        val inputVideoFinalListFile = File("$workingTempDir/inputVideoFinalList.txt").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//            writeText(
//                inputVideoFinalList.toString().substring(0, inputVideoFinalList.toString().length - 1)
//            )
//        }
//
//        val outputVideoFinalFile = File("$workingDir/output/output-final.mp4").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//        }
//
//        cmd = "$cmdPath -y -f concat -safe 0 -i ${inputVideoFinalListFile.path} -c copy ${outputVideoFinalFile.path}"
//
//        println(cmd)
//
//        Runtime.getRuntime().exec(cmd).apply {
//            println(InputStreamReader(this.inputStream).readText())
//            println(InputStreamReader(this.errorStream).readText())
//            waitFor()
//        }

//        outputVideoFile.delete()
        println("step: 结束合并视频")
        println("输出文件路径：${outputVideoFormattedFile.path} 耗时：${(System.currentTimeMillis() - startTime) / 1000L} s")
    }

    private fun outputImg(index: Int, sortedExcelData: SortedExcelData) {
//        bgIndex = index % 7 + 1
        val bgImg = "$workingDir/background_img/bg_${bgIndex}.png"

        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)

        IJ.openImage(bgImg).processor.apply {
            if (bgIndex > 4) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.BLACK)
            }
            setFont(font)
            setAntialiasedText(true)

            drawSentence(font, sortedExcelData.SENTENCE)

            if (bgIndex > 4) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.decode("#2F6DAD"))
            }
            setFont(indexFont)
            setAntialiasedText(true)
            drawIndex(indexFont, index + 1)

            val outputImgFile =
                File("$workingTempDir/output_img/${sortedExcelData.ID}-pic1.png").apply {
                    if (!parentFile.exists())
                        parentFile.mkdirs()
                }

            ImageIO.write(
                this.bufferedImage,
                "png",
                outputImgFile
            )
        }

        IJ.openImage(bgImg).processor.apply {
            if (bgIndex > 4) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.BLACK)
            }
            setFont(font)
            setAntialiasedText(true)

            drawSentence(font, sortedExcelData.SENTENCE)

            if (bgIndex > 4) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.decode("#2F6DAD"))
            }
            setFont(indexFont)
            setAntialiasedText(true)
            drawIndex(indexFont, index + 1)

            drawTranslate(bgIndex, sortedExcelData.TRANS_JP)

            val outputImgFile =
                File("$workingTempDir/output_img/${sortedExcelData.ID}-pic2.png").apply {
                    if (!parentFile.exists())
                        parentFile.mkdirs()
                }

            ImageIO.write(
                this.bufferedImage,
                "png",
                outputImgFile
            )
        }

//        addWaterMarkToImage(
//            bgImg,
//            "$workingTempDir/output_img/${sortedExcelData.ID}-pic1.png",
//            index,
//            false,
//            sortedExcelData
//        )
//
//        addWaterMarkToImage(
//            bgImg,
//            "$workingTempDir/output_img/${sortedExcelData.ID}-pic2.png",
//            index,
//            true,
//            sortedExcelData
//        )
    }


    private fun ImageProcessor.drawIndex(font: Font, index: Int) {
        val outlineRect = Rectangle(1410, 215, 86, 86)

        val contentRect = getContentRect(font, index.toString())

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height

        drawString(index.toString(), startX, startY)
    }

    private fun ImageProcessor.drawTranslate(bgIndex: Int, translate: String) {
        var translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, 50)
        if (bgIndex > 4) {
            setColor(Color.decode("#FCF071"))
        } else {
            setColor(Color.decode("#4DA0F8"))
        }

        setFont(translateFont)
        setAntialiasedText(true)


        val outlineRect = Rectangle(254 + 30, 200 + 30, 1280 - 60, 670 - 60)

        var contentRect = getContentRect(translateFont, translate)

        while (contentRect.width > outlineRect.width) {
            translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, translateFont.size - 1)
            setFont(translateFont)
            setAntialiasedText(true)

            contentRect = getContentRect(translateFont, translate)
        }

        val startX = outlineRect.x + outlineRect.width / 2 - contentRect.width / 2
        val startY = outlineRect.y + outlineRect.height - 56 - (contentRect.height / 2) + contentRect.height

        drawString(translate, startX, startY)
    }

    private fun ImageProcessor.drawSentence(font: Font, sentence: String) {
        val contentRect = getContentRect(font, sentence)
        println("contentRect.width:${contentRect.width}")
        println("contentRect.height:${contentRect.height}")

        println("img.height:${width}")
        println("img.height:${height}")

        val outlineRect = Rectangle(254 + 30, 200 + 30, 1280 - 60, 670 - 60)

        if (contentRect.width > (outlineRect.width)) {
            val firstLineSentence = StringBuilder()
            var curLineSentence = StringBuilder()
            sentence.split(" ").forEachIndexed { index, word ->
                firstLineSentence.append(word)
                firstLineSentence.append(" ")
                curLineSentence.append(word)
                curLineSentence.append(" ")

                val nextIndex = index + 1
                if (nextIndex < sentence.split(" ").size) {
                    val nextWord = sentence.split(" ")[nextIndex]
                    val nextRect = getContentRect(font, "$curLineSentence$nextWord ")
                    val curRect = getContentRect(font, curLineSentence.toString())
                    if (curRect.width <= outlineRect.width && nextRect.width > outlineRect.width) {
                        curLineSentence = StringBuilder()
                        firstLineSentence.append("\n")
                    }
                }
            }

            val realWriteSentence = firstLineSentence.toString().substring(0, firstLineSentence.length - 1)

            var sentenceWidth = 0
            var sentenceHeight = 0
            for (s in realWriteSentence.split("\n")) {
                getContentRect(font, s).apply {
                    sentenceHeight += this.height
                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
                }
            }

            val startX = outlineRect.x + outlineRect.width / 2 - (sentenceWidth / 2)
            val startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + 80

            drawString(realWriteSentence, startX, startY)

        } else {
            val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
            val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + 80

            println("sentence:$sentence")
            println("startX:$startX")
            println("startY:$startY")
            drawString(sentence, startX, startY)
        }

    }

    fun getContentRect(font: Font, content: String): Rectangle {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(content, frc).bounds
    }

    private fun addWaterMarkToImage(
        inputImagePath: String,
        outputPath: String,
        index: Int,
        isDrawTranslate: Boolean,
        sortedExcelData: SortedExcelData
    ) {
        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)
        val translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, 50)

        val file = File(inputImagePath)
        //源图片
        val image: Image = ImageIO.read(file)
        val bi = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val alpha = 1f
        //得到画笔对象
        val g2 = bi.createGraphics()

        //高清代码,不加水印会模糊
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        //设置文字水印透明度
        g2.composite = ac
        g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null)
        //设置字体

        val sentence = sortedExcelData.SENTENCE + sortedExcelData.SENTENCE
        g2.font = font
        var frc = g2.fontRenderContext
        var tl = TextLayout(sentence, font, frc)
        var sha: Shape
        drawSentence(tl, font, sentence).apply {
            println(this.first)
            tl = TextLayout(this.first, font, frc)
            sha = second
        }

        //设置好水印位置x,y

        if (bgIndex > 4) {
            //描边色
            g2.color = Color.black
            g2.draw(sha)

            //字体色
            g2.color = Color.white
            g2.fill(sha)
        } else {
            //描边色
            g2.color = Color.black
            g2.draw(sha)

            //字体色
            g2.color = Color.black
            g2.fill(sha)
        }


        g2.font = font
        frc = g2.fontRenderContext
        tl = TextLayout((index + 1).toString(), indexFont, frc)
        sha = drawIndex(tl, indexFont, index + 1)

        if (bgIndex > 4) {
            //描边色
            g2.color = Color.white
            g2.draw(sha)

            //字体色
            g2.color = Color.white
            g2.fill(sha)
        } else {
            //描边色
            g2.color = Color.decode("#2F6DAD")
            g2.draw(sha)

            //字体色
            g2.color = Color.decode("#2F6DAD")
            g2.fill(sha)
        }

        if (isDrawTranslate) {
            val transText = sortedExcelData.TRANS_JP
            g2.font = translateFont
            frc = g2.fontRenderContext
            tl = TextLayout(
                transText,
                translateFont,
                frc
            )
            drawTranslate(
                tl,
                translateFont,
                transText
            ).apply {
                g2.font = this.first
                frc = g2.fontRenderContext
                tl = TextLayout(transText, this.first, frc)
                sha = drawTranslate(tl, this.first, transText).second
            }

            if (bgIndex > 4) {
                //描边色
                g2.color = Color.decode("#FCF071")
                g2.draw(sha)

                //字体色
                g2.color = Color.decode("#FCF071")
                g2.fill(sha)
            } else {
                //描边色
                g2.color = Color.decode("#4DA0F8")
                g2.draw(sha)

                //字体色
                g2.color = Color.decode("#4DA0F8")
                g2.fill(sha)
            }
        }

        ImageIO.write(bi, "PNG", FileOutputStream(outputPath))
    }

    private fun drawIndex(textLayout: TextLayout, font: Font, index: Int): Shape {
        val outlineRect = Rectangle(1410, 215, 86, 86)

        val contentRect = getContentRect(font, index.toString())

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + 38

        return textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble()))
    }


    private fun drawTranslate(
        textLayout: TextLayout,
        translateFont: Font,
        translate: String
    ): Pair<Font, Shape> {
        var font = translateFont
        val outlineRect = Rectangle(254 + 30, 200 + 30, 1280 - 60, 670 - 60)

        var contentRect = getContentRect(font, translate)

        while (contentRect.width > outlineRect.width) {
            font = Font("Hiragino Maru Gothic Pro", Font.PLAIN, font.size - 1)
            contentRect = getContentRect(font, translate)
        }

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height - (contentRect.height / 2)

        return Pair(
            font,
            textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble()))
        )
    }

    private fun drawSentence(textLayout: TextLayout, font: Font, sentence: String): Pair<String, Shape> {
        val contentRect = getContentRect(font, sentence)
        val outlineRect = Rectangle(254 + 30, 200 + 30, 1280 - 60, 670 - 60)

        if (contentRect.width > (outlineRect.width)) {
            val firstLineSentence = StringBuilder()
            var curLineSentence = StringBuilder()
            sentence.split(" ").forEachIndexed { index, word ->
                firstLineSentence.append(word)
                firstLineSentence.append(" ")
                curLineSentence.append(word)
                curLineSentence.append(" ")

                val nextIndex = index + 1
                if (nextIndex < sentence.split(" ").size) {
                    val nextWord = sentence.split(" ")[nextIndex]
                    val nextRect = getContentRect(font, "$curLineSentence$nextWord ")
                    val curRect = getContentRect(font, curLineSentence.toString())
                    if (curRect.width <= outlineRect.width && nextRect.width > outlineRect.width) {
                        curLineSentence = StringBuilder()
                        firstLineSentence.append("\n")
                    }
                }
            }

            val realWriteSentence = firstLineSentence.toString().substring(0, firstLineSentence.length - 1)

            var sentenceWidth = 0
            var sentenceHeight = 0
            for (s in realWriteSentence.split("\n")) {
                getContentRect(font, s).apply {
                    sentenceHeight += this.height
                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
                }
            }

            val startX = outlineRect.x + outlineRect.width / 2 - (sentenceWidth / 2)
            val startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + 55

            return Pair(realWriteSentence, textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble())))
        } else {
            val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
            val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + 55

            println("ff sentence:$sentence")
            println("startX:$startX")
            println("startY:$startY")
            return Pair(sentence, textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble())))
        }

    }
}