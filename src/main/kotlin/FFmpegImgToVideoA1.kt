@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import data_object.SortedExcelData2
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


    runBlocking {
        val ffmpegimgtovideoutilEN = FFmpegImgToVideoA1()
        val ffmpegimgtovideoutilJP = FFmpegImgToVideoA1()
//        for (fFmpegImgToVideoUtil3 in arrayOf(ffmpegimgtovideoutilEN, ffmpegimgtovideoutilJP)) {
//            fFmpegImgToVideoUtil3.startSortIndex = 1
//            fFmpegImgToVideoUtil3.endSortIndex = 760
//        }

        launch(Dispatchers.IO) {
            ffmpegimgtovideoutilEN.isEnglish = true
            ffmpegimgtovideoutilEN.outputFileName =
                "A1-output-EN.mp4"
            ffmpegimgtovideoutilEN.main()
        }
        launch(Dispatchers.IO) {
            ffmpegimgtovideoutilJP.isEnglish = false
            ffmpegimgtovideoutilJP.outputFileName =
                "A1-output-JP.mp4"
            ffmpegimgtovideoutilJP.main()
        }
    }

}

class FFmpegImgToVideoA1 {
    var bgIndex = 1
    private var isDebug = false
    var hasCompressedAudio = false
    var isEnglish = false
    private var workingDir = ""
    private var workingTempDir = ""

    var functionIndex = 2
    var repeatCount = 3
    private var repeatGapTime = 3
    private var sentenceGapTime = 4
    var startSortIndex = 1
    var endSortIndex = 151

    var outputFileName = "output(1-150)"
    private var inputAudioDirPath = "/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/A1VOCAB音频汇总"
    private var inputAudioSortExcelPath = "/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/A1VOCAB英日句子.xlsx"

    private val repeatGameTimeFile get() = File("$workingTempDir/gap_time/$repeatGapTime.mp3")
    private val sentenceGapTimeFile get() = File("$workingTempDir/gap_time/$sentenceGapTime.mp3")

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
        println("workingDir = $workingDir")
        if (workingDir.contains(" ")) {
            println("workingDir不能包含空格")
            exitProcess(0)
        }
    }

    fun main() {
        workingTempDir = "$workingDir/temp/$outputFileName"
        println("workingTempDir = $workingTempDir")

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
    private fun combineMedia() {
        if (inputAudioSortExcelPath.isNotEmpty()) {
            EasyExcel.read(
                File(inputAudioSortExcelPath),
                SortedExcelData2::class.java,
                object : ReadListener<SortedExcelData2> {
                    val sortedExcelDataList = ArrayList<SortedExcelData2>()
                    override fun invoke(data: SortedExcelData2, context: AnalysisContext) {
                        sortedExcelDataList.add(data)
                    }

                    override fun doAfterAllAnalysed(context: AnalysisContext) {
                        combineVideoWithSortedList(sortedExcelDataList)
                    }

                }).sheet("Sheet1").doRead()
        } else {
            combineVideoWithSortedList(ArrayList())
        }
    }

    /**
     * 合成视频
     */
    fun combineVideoWithSortedList(sortedExcelDataList: ArrayList<SortedExcelData2>) {
        val startTime = System.currentTimeMillis()
        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("$workingDir/library/ffprobe")

        val cmdPath = "$workingDir/library/ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        val subList = sortedExcelDataList.subList(0, sortedExcelDataList.size)
        val audioFileList = subList.map { File("$workingTempDir/compressed_audio/${it.ID}.mp3") }

        Utils.emptyFileDir(workingTempDir)

        println("step: 开始压缩音频")
        compressAudio(cmdPath, subList)
        println("step: 结束压缩音频")

        println("step: 开始生成间隔音频文件")
        generateGapTime(cmdPath)
        println("step: 结束生成间隔音频文件")

        println("step: 开始生成图片")
        subList.forEachIndexed { index, sortedExcelData ->
            outputImg(index, sortedExcelData)
        }
        println("step: 结束生成图片")

        println("step: 开始合并视频")
        val inputVideoList = StringBuilder()

        for (fileList in Utils.averageAssignFixLength(audioFileList, 5)) {
            runBlocking {
                fileList.forEachIndexed { index, audioFile ->

                    launch(Dispatchers.IO) {
                        if (!audioFile.exists()) {
                            println("file not exist: ${audioFile.path}")
                        }
                        val inputAudioList = StringBuilder()
                        val inputImgList = StringBuilder()

                        val audioFileName = audioFile.name.split(".")[0]
                        val probeResult = ffprobe.probe(audioFile.path)
                        val fFmpegFormat = probeResult.format
                        val curAudioDuration = fFmpegFormat.duration

                        for (i in 0 until 2) {
                            inputAudioList.append("file '${audioFile.path}'")
                            inputAudioList.append("\n")
                            inputAudioList.append("file '${repeatGameTimeFile.path}'")
                            inputAudioList.append("\n")
                        }
                        inputAudioList.append("file '${audioFile.path}'")
                        inputAudioList.append("\n")
                        inputAudioList.append("file '${sentenceGapTimeFile.path}'")
                        inputAudioList.append("\n")

                        val listenPicFileName = "${audioFileName}-pic0.png"
                        val fistPicFileName = "${audioFileName}-pic1.png"
                        val secondPicFileName = "${audioFileName}-pic2.png"

                        inputImgList.append("file '$inputAudioImgDirPath/${listenPicFileName}'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + repeatGapTime}")
                        inputImgList.append("\n")
                        inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + repeatGapTime}")
                        inputImgList.append("\n")
                        if (isEnglish) {
                            inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        } else {
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                        }
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + sentenceGapTime + 3}")
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

                        ShellUtils.run(
                            "$cmdPath -y -f concat -safe 0 -i ${inputAudioListFile.path} -c copy ${outputAudioFile.path}",
                            object : ShellUtils.OnCommandExecOutputListener {
                                override fun onSuccess(line: String?) {
                                    println(line)
                                }

                                override fun onError(line: String?) {
                                    println(line)
                                }

                            }
                        )

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

                        ShellUtils.run(
                            "$cmdPath -y -f concat -safe 0 -i ${inputImgListFile.path} -r 10 -vcodec libx264 -pix_fmt yuv420p -preset ultrafast ${outputVideoFile.path}",
                            object : ShellUtils.OnCommandExecOutputListener {
                                override fun onSuccess(line: String?) {
                                    println(line)
                                }

                                override fun onError(line: String?) {
                                    println(line)
                                }
                            })

                        val outputAudioFileDuration = ffprobe.probe(outputAudioFile.path).format.duration
                        val keyOutputVideoFile = File("$workingTempDir/output/key-output-${audioFileName}.mp4").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }
                        ShellUtils.run(
                            "$cmdPath -y -ss 0 -t $outputAudioFileDuration -accurate_seek -i ${outputVideoFile.path} -codec copy -avoid_negative_ts 1 ${keyOutputVideoFile.path}",
                            object : ShellUtils.OnCommandExecOutputListener {
                                override fun onSuccess(line: String?) {
                                    println(line)
                                }

                                override fun onError(line: String?) {
                                    println(line)
                                }

                            })

                        val outputVideoFormattedFile =
                            File("$workingTempDir/output/formatted-output-${audioFileName}.mp4").apply {
                                if (!this.parentFile.exists())
                                    this.parentFile.mkdirs()
                            }

                        ShellUtils.run(
                            "$cmdPath -y -i ${keyOutputVideoFile.path} -i ${outputAudioFile.path} ${outputVideoFormattedFile.path}",
                            object : ShellUtils.OnCommandExecOutputListener {
                                override fun onSuccess(line: String?) {
                                    println(line)
                                }

                                override fun onError(line: String?) {
                                    println(line)
                                }
                            }
                        )

                        val handBrakeOutputVideoFile =
                            File("$workingTempDir/output/handbrake-output-${audioFileName}.mp4").apply {
                                if (!this.parentFile.exists())
                                    this.parentFile.mkdirs()
                            }

                        ShellUtils.run(
                            "$hanBrakePath -i ${outputVideoFormattedFile.path} -o ${handBrakeOutputVideoFile.path} -e x264 -q 10 -B 160",
                            object : ShellUtils.OnCommandExecOutputListener {
                                override fun onSuccess(line: String?) {
                                    println(line)
                                }

                                override fun onError(line: String?) {
                                    println(line)
                                }
                            }
                        )

//            val mtsFile =
//                File("$workingTempDir/output/handbrake-output-${audioFileName}.ts").apply {
//                    if (!this.parentFile.exists())
//                        this.parentFile.mkdirs()
//                }
//
//            ShellUtils.run(
//                "$cmdPath -y -i ${handBrakeOutputVideoFile.path} -vcodec copy -acodec copy ${mtsFile.path}",
//                object : ShellUtils.OnCommandExecOutputListener {
//                    override fun onSuccess(line: String?) {
//                        println(line)
//                    }
//
//                    override fun onError(line: String?) {
//                        println(line)
//                    }
//                }
//            )
                    }
                }
            }
        }

        if (isEnglish) {
            inputVideoList.append("file /Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/新FD片头-英.mp4'")
        } else {
            inputVideoList.append("file /Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/新FD片头-日.mp4'")
        }
        inputVideoList.append("\n")

        for (audioFile in audioFileList) {
            val audioFileName = audioFile.name.split(".")[0]
            val outputVideoFile = File("$workingTempDir/output/handbrake-output-${audioFileName}.mp4")
            inputVideoList.append("file '${outputVideoFile.path}'")
            inputVideoList.append("\n")
        }

        if (isEnglish) {
            inputVideoList.append("file '/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/新FD片尾-英.mp4'")
        } else {
            inputVideoList.append("file '/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/新FD片尾-日.mp4'")
        }
        inputVideoList.append("\n")

        val inputVideoListFile = File("$workingTempDir/inputVideoList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputVideoList.toString().substring(0, inputVideoList.toString().length - 1)
            )
        }

        val outputVideoFile = File("$workingDir/output/output-$outputFileName").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        ShellUtils.run(
            "$cmdPath -y -f concat -safe 0 -i ${inputVideoListFile.path} -c copy ${outputVideoFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                    println(line)
                }

                override fun onError(line: String?) {
                    println(line)
                }
            })

        val outputVideoFormattedFile = File("$workingDir/output/${outputFileName}").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        ShellUtils.run(
            "$cmdPath -y -i ${outputVideoFile.path} -preset ultrafast ${outputVideoFormattedFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                    println(line)
                }

                override fun onError(line: String?) {
                    println(line)
                }

            })

        println("step: 结束合并视频")
        println("输出文件路径：${outputVideoFormattedFile.path} 耗时：${(System.currentTimeMillis() - startTime) / 1000L} s")
    }

    private fun compressAudio(cmdPath: String, subList: MutableList<SortedExcelData2>) {
        runBlocking {
            for (listFile in File(inputAudioDirPath).listFiles().filter { file ->
                subList.find {
                    it.ID == file.name.split(".")[0].toInt().toString()
                } != null
            }) {
                launch(Dispatchers.IO) {
                    val outputFile =
                        File("$workingTempDir/compressed_audio/${(listFile.name.split(".")[0].toInt())}.mp3").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }
                    ShellUtils.run(
                        "$cmdPath -y -i ${listFile.path} -ac 2 -ar 48000 ${outputFile.path}",
                        object : ShellUtils.OnCommandExecOutputListener {
                            override fun onSuccess(line: String?) {
                            }

                            override fun onError(line: String?) {
                            }
                        })
                }
            }
        }
    }

    private fun generateGapTime(cmdPath: String) {

        for (file in arrayOf(repeatGameTimeFile, sentenceGapTimeFile)) {
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
        }

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $repeatGapTime ${repeatGameTimeFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $sentenceGapTime ${sentenceGapTimeFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })
    }

    private fun isDarkBg() = arrayOf(5, 7).contains(bgIndex)

    private fun outputImg(index: Int, sortedExcelData: SortedExcelData2) {
//        bgIndex = index % 7 + 1
        val bgImg = "/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/A1背景图.png"
        val listenImg = "/Users/lingodeer-yxg/Downloads/A1系列视频素材(1)/新listen图.png"

        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)

        IJ.openImage(listenImg).processor.apply {
            if (isDarkBg()) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.decode("#2F6DAD"))
            }
            setFont(indexFont)
            setAntialiasedText(true)
            drawIndex(indexFont, index + 1)

            val outputImgFile =
                File("$workingTempDir/output_img/${sortedExcelData.ID}-pic0.png").apply {
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
            if (isDarkBg()) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.BLACK)
            }
            setFont(font)
            setAntialiasedText(true)

            drawSentence(font, sortedExcelData.SENTENCE)

            if (isDarkBg()) {
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
            if (isDarkBg()) {
                setColor(Color.WHITE)
            } else {
                setColor(Color.BLACK)
            }
            setFont(font)
            setAntialiasedText(true)

            drawSentence(font, sortedExcelData.SENTENCE)

            if (isDarkBg()) {
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
        val outlineRect = Rectangle(1605, 215, 86, 86)

        val contentRect = getContentRect(font, index.toString())

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height

        drawString(index.toString(), startX, startY)
    }

    private fun ImageProcessor.drawTranslate(bgIndex: Int, translate: String) {
        var translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, 50)
        if (isDarkBg()) {
            setColor(Color.decode("#FCF071"))
        } else {
            setColor(Color.decode("#4DA0F8"))
        }

        setFont(translateFont)
        setAntialiasedText(true)


        val outlineRect = Rectangle(185 + 30, 165 + 30, 1555 - 60, 715 - 60)

        var contentRect = getContentRect(translateFont, translate)

        while (contentRect.width > outlineRect.width) {
            translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, translateFont.size - 1)
            setFont(translateFont)
            setAntialiasedText(true)

            contentRect = getContentRect(translateFont, translate)
        }

        val startX = outlineRect.x + outlineRect.width / 2 - contentRect.width / 2
        val startY = outlineRect.y + outlineRect.height - 100 - (contentRect.height / 2) + contentRect.height

        drawString(translate, startX, startY)
    }

    private fun ImageProcessor.drawSentence(font: Font, sentence: String) {
        val contentRect = getContentRect(font, sentence)
//        println("contentRect.width:${contentRect.width}")
//        println("contentRect.height:${contentRect.height}")
//
//        println("img.height:${width}")
//        println("img.height:${height}")

        val outlineRect = Rectangle(185 + 30, 165 + 30, 1555 - 60, 715 - 60)

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

//            println("sentence:$sentence")
//            println("startX:$startX")
//            println("startY:$startY")
            drawString(sentence, startX, startY)
        }

    }

    private fun getContentRect(font: Font, content: String): Rectangle {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(content, frc).bounds
    }

    private fun addWaterMarkToImage(
        inputImagePath: String,
        outputPath: String,
        index: Int,
        isDrawTranslate: Boolean,
        sortedExcelData: SortedExcelData2
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

        if (isDarkBg()) {
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

        if (isDarkBg()) {
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

            if (isDarkBg()) {
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

            return Pair(
                realWriteSentence,
                textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble()))
            )
        } else {
            val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
            val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + 55

            println("ff sentence:$sentence")
            println("startX:$startX")
            println("startY:$startY")
            return Pair(
                sentence,
                textLayout.getOutline(AffineTransform.getTranslateInstance(startX.toDouble(), startY.toDouble()))
            )
        }

    }

    fun writeStartAndEnd() {
        val cmdPath = "$workingDir/library/ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        var cmd = ""
        loop@ for (listFile in File("$workingDir/output/1-1077视频-已固定帧率").listFiles()) {
            if (!listFile.name.endsWith(".mp4")) {
                continue@loop
            }
            val outPutFile = File("$workingDir/output/1-1077视频-fixed/${listFile.name}").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }
            cmd = "$hanBrakePath -i ${listFile.path} -o ${outPutFile.path} -e x264 -q 30 -B 160"
            Runtime.getRuntime().exec(cmd).apply {
                for (readLine in InputStreamReader(this.inputStream).readLines()) {
                    println(readLine)
                }
                waitFor()
            }

            val inputVideoFinalList = StringBuilder()
            inputVideoFinalList.append("file '${workingDir}/output/enpal-片头.mp4'")
            inputVideoFinalList.append("\n")
            inputVideoFinalList.append("file '${outPutFile.path}'")
            inputVideoFinalList.append("\n")
            inputVideoFinalList.append("file '${workingDir}/output/enpal-片尾.mp4'")
            inputVideoFinalList.append("\n")


            val inputVideoFinalListFile = File("$workingTempDir/inputVideoFinalList.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    inputVideoFinalList.toString().substring(0, inputVideoFinalList.toString().length - 1)
                )
            }

            val outputVideoWithStartEndFile =
                File("$workingDir/output/1-1077视频-with-start-end/${listFile.name}").apply {
                    if (!this.parentFile.exists())
                        this.parentFile.mkdirs()
                }

            cmd =
                "$cmdPath -y -f concat -safe 0 -i ${inputVideoFinalListFile.path} -c copy ${outputVideoWithStartEndFile.path}"

            println(cmd)

            Runtime.getRuntime().exec(cmd).apply {
                for (readLine in InputStreamReader(this.inputStream).readLines()) {
                    println(readLine)
                }
                waitFor()
            }
        }

        Runtime.getRuntime().exec("/Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/final.sh").apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }
    }
}