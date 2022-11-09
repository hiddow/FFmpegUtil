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
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess


fun main(args: Array<String>) {
//    FFmpegUtil.test()

    for (i in 0 until 1) {
        val count = if (i == 6) {
            177
        } else {
            15
        }
        FFmpegImgToVideoUtil_2.startSortIndex = (i * 150) + 1
        FFmpegImgToVideoUtil_2.endSortIndex = FFmpegImgToVideoUtil_2.startSortIndex + count

//        FFmpegImgToVideoUtil_2.isEnglish = true
//        FFmpegImgToVideoUtil_2.outputFileName =
//            "output(${FFmpegImgToVideoUtil_2.startSortIndex}-${FFmpegImgToVideoUtil_2.endSortIndex - 1})-EN-test.mp4"
//        FFmpegImgToVideoUtil_2.main()


        FFmpegImgToVideoUtil_2.isEnglish = false
        FFmpegImgToVideoUtil_2.outputFileName =
            "output(${FFmpegImgToVideoUtil_2.startSortIndex}-${FFmpegImgToVideoUtil_2.endSortIndex - 1})-EN-JP.mp4"
        FFmpegImgToVideoUtil_2.main()
    }

//    FFmpegImgToVideoUtil.main()

//    val videoList = listOf(
//        "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/output/enpal-片头.mp4",
//        "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/output/output-formatted.mp4",
//        "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/output/enpal-片尾.mp4"
//    )
//
//    Mp4ParseUtil.mergeVideo(videoList, "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/output/", "output-full.mp4")
//    FFmpegUtil2.processImg()
}

object FFmpegImgToVideoUtil_2 {

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
    var inputAudioSortExcelPath = "/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/1077-with-Japanese(排序后).xlsx"

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

        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("$workingDir/library/ffprobe")

        val cmdPath = "$workingDir/library/ffmpeg"

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

//        val inputVideoList = StringBuilder()
        val inputImgList = StringBuilder()
        val inputAudioList = StringBuilder()

        audioFileList.forEachIndexed { index, audioFile ->
            if (!audioFile.exists()) {
                println(audioFile.path)
            }
//            val inputAudioList = StringBuilder()
//            val inputImgList = StringBuilder()

            val audioFileName = audioFile.name.split(".")[0]
            val probeResult = ffprobe.probe(audioFile.path)
            val fFmpegFormat = probeResult.format
            val curAudioDuration = fFmpegFormat.duration
            var specialGapTimeFile: File? = null
            var specialAudioGapTime = 0.0

//            val audioMistakeDuration = (curAudioDuration - curAudioDuration.toInt())
//            if (audioMistakeDuration != 0.0) {
//                specialAudioGapTime = 1.0 - audioMistakeDuration
//                specialGapTimeFile = File("$workingTempDir/gap_time/$specialAudioGapTime.mp3")
//                Runtime.getRuntime()
//                    .exec("$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $specialAudioGapTime ${specialGapTimeFile.path}")
//                    .apply {
//                        waitFor()
//                    }
//            }

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


//            val fistPicFileName = "002-ENJP.${(1000 + firstPicIndex).toString().substring(1, 4)}.png"
//            val secondPicFileName = "002-ENJP.${(1000 + nextPicIndex).toString().substring(1, 4)}.png"

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

        }

        val inputAudioListFile =
            File("$workingTempDir/input_txt/inputAudioList.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    inputAudioList.toString().substring(0, inputAudioList.toString().length - 1)
                )
            }

        val outputAudioFile = File("$workingTempDir/output/output.mp3").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        Runtime.getRuntime().exec(
            "$cmdPath -y -f concat -safe 0 -i ${inputAudioListFile.path} -c copy ${outputAudioFile.path}"
        ).apply {
            waitFor()
        }

        val inputImgListFile =
            File("$workingTempDir/input_txt/inputImgList.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    inputImgList.toString().substring(0, inputImgList.toString().length - 1)
                )
            }

        val outputVideoFile = File("$workingTempDir/output/output.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        var cmd = "$cmdPath -y -f " +
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


        val keyOutputVideoFile = File("$workingDir/temp/output/keyoutput.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        cmd =
            "$cmdPath -y -ss 0 -t ${ffprobe.probe(outputAudioFile.path).format.duration.toInt()} -accurate_seek -i ${outputVideoFile.path} -codec copy -avoid_negative_ts 1 ${keyOutputVideoFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            waitFor()
        }

        cmd = "$cmdPath -y -i ${keyOutputVideoFile.path} -i ${outputAudioFile.path} -strict experimental ${outputVideoFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            waitFor()
        }



//        val inputVideoListFile = File("$workingTempDir/inputVideoList.txt").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//            writeText(
//                inputVideoList.toString().substring(0, inputVideoList.toString().length - 1)
//            )
//        }
//
//        val outputVideoFile = File("$workingDir/output/output.mp4").apply {
//            if (!this.parentFile.exists())
//                this.parentFile.mkdirs()
//        }
//
//        var cmd = "$cmdPath -y -f concat -safe 0 -i ${inputVideoListFile.path} ${outputVideoFile.path}"
//
//        println(cmd)
//
//        Runtime.getRuntime().exec(cmd).apply {
//            waitFor()
//        }
//
        val outputVideoFormattedFile = File("$workingDir/output/${outputFileName}").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        cmd = "$cmdPath -y -i ${outputVideoFile.path} ${outputVideoFormattedFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(
            "$cmdPath -y -i ${outputVideoFile.path} ${outputVideoFormattedFile.path}"
        ).apply {
            waitFor()
        }

//        outputVideoFile.delete()
        println("step: 结束合并视频")
        println("输出文件路径：${outputVideoFormattedFile.path}")
    }

    private fun outputImg(index: Int, sortedExcelData: SortedExcelData) {
        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)

//        val bgImg = if (index < 38) {
//            "$workingDir/background_img/bg_1.png"
//        } else if (index < 76) {
//            "$workingDir/background_img/bg_2.png"
//        } else if (index < 114) {
//            "$workingDir/background_img/bg_3.png"
//        } else {
//            "$workingDir/background_img/bg_4.png"
//        }

        val bgImg = "$workingDir/background_img/bg_1.png"

        IJ.openImage(bgImg).processor.apply {
            setColor(Color.BLACK)
            setFont(font)
            setAntialiasedText(true)
            drawSentence(font, sortedExcelData.SENTENCE)

            setColor(Color.decode("#2F6DAD"))
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
            setColor(Color.BLACK)
            setFont(font)
            setAntialiasedText(true)
            drawSentence(font, sortedExcelData.SENTENCE)

            setColor(Color.decode("#2F6DAD"))
            setFont(indexFont)
            setAntialiasedText(true)
            drawIndex(indexFont, index + 1)

            drawTranslate(sortedExcelData.TRANS_JP)

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
    }


    private fun ImageProcessor.drawIndex(font: Font, index: Int) {
        val outlineRect = Rectangle(1410, 215, 86, 86)

        val contentRect = getContentRect(font, index.toString())

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height

        drawString(index.toString(), startX, startY)
    }

    private fun ImageProcessor.drawTranslate(translate: String) {
        var translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, 50)
        setColor(Color.decode("#4DA0F8"))
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

            drawString(sentence, startX, startY)
        }

    }

    fun getContentRect(font: Font, content: String): Rectangle {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(content, frc).bounds
    }
}