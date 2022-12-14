@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import aws.sdk.kotlin.services.polly.PollyClient
import aws.sdk.kotlin.services.polly.model.*
import aws.smithy.kotlin.runtime.content.toByteArray
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


    for (i in 0 until 1) {
//        val count = if (i == 6) {
//            177
//        } else {
//            150
//        }
        val count = 1077
        runBlocking {
            val ffmpegimgtovideoutilEN = FFmpegImgToVideoUtil3()
            val ffmpegimgtovideoutilJP = FFmpegImgToVideoUtil3()
            for (fFmpegImgToVideoUtil3 in arrayOf(ffmpegimgtovideoutilEN, ffmpegimgtovideoutilJP)) {
                fFmpegImgToVideoUtil3.bgIndex = i + 1
                fFmpegImgToVideoUtil3.startSortIndex = (i * 150) + 1
                fFmpegImgToVideoUtil3.endSortIndex = fFmpegImgToVideoUtil3.startSortIndex + count
            }

//            launch(Dispatchers.IO) {
//                ffmpegimgtovideoutilEN.isEnglish = true
//                ffmpegimgtovideoutilEN.outputFileName =
//                    "output(${ffmpegimgtovideoutilEN.startSortIndex}-${ffmpegimgtovideoutilEN.endSortIndex - 1})-EN.mp4"
//                ffmpegimgtovideoutilEN.main()
//            }
            launch(Dispatchers.IO) {
                ffmpegimgtovideoutilJP.isEnglish = false
                ffmpegimgtovideoutilJP.outputFileName =
                    "output(${ffmpegimgtovideoutilJP.startSortIndex}-${ffmpegimgtovideoutilJP.endSortIndex - 1})-EN-JP.mp4"
                ffmpegimgtovideoutilJP.main()
            }
        }
    }

}

class FFmpegImgToVideoUtil3 {
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
    private var inputAudioDirPath = "${Utils.parentDir}/??????/FD-1077/YT-Ref-compressed"
    private var inputAudioSortExcelPath = "${Utils.parentDir}/??????/FD-1077/1077-with-Japanese(?????????).xlsx"

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
                "${Utils.parentDir}/FFmpegUtil"
            } else {
                File(
                    FFmpegUtil::class.java.protectionDomain.codeSource.location
                        .toURI()
                ).parent
            }
        println("workingDir = $workingDir")
        if (workingDir.contains(" ")) {
            println("workingDir??????????????????")
            exitProcess(0)
        }
    }

    fun main() {
        workingTempDir = "$workingDir/temp/$outputFileName"
        println("workingTempDir = $workingTempDir")

//        val scanner = Scanner(System.`in`)
//        println("1. ?????????????????????????????????????????????")
//        println("2. ?????????????????????????????????????????????")
//        println("????????????????????????????????????")
//
//        functionIndex = scanner.nextLine().trim().toInt()
//
//        println("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????/Users/lingodeer-yxg/Downloads/YT-input-audio???:")
//        inputAudioDirPath = scanner.nextLine().trim()
//        println("inputAudioDirPath = ${inputAudioDirPath}")
//
//        if (functionIndex == 2) {
//            println("\n??????????????????????????????????????????????????????????????????????????????????????????????????????????????????/Users/lingodeer-yxg/Downloads/YT-input-img???:")
//            inputAudioImgDirPath = scanner.nextLine().trim()
//            println("inputAudioDirPath = ${inputAudioImgDirPath}")
//        }
//
//        println("\n?????????????????????????????????Excel????????????????????????????????????sorted-list????????????????????????????????????????????????????????????/Users/lingodeer-yxg/Downloads/Youtube??????301-1077.xlsx??????????????????????????????????????????????????????ID?????????????????????:")
//        inputAudioSortExcelPath = scanner.nextLine().trim()
//        if (inputAudioSortExcelPath.isEmpty()) {
//            println("?????????????????????????????????????????????ID??????????????????")
//        } else {
//            println("inputAudioSortExcelPath = $inputAudioSortExcelPath")
//        }
//
//        println("\n???????????????????????????????????????")
//        repeatCount = scanner.nextLine().toInt()
//        println("repeatCount = $repeatCount")
//
//        println("\n?????????????????????????????????????????????")
//        repeatGapTime = scanner.nextLine().toInt()
//        println("repeatGapTime = ${repeatGapTime}s")
//
//        println("\n?????????????????????????????????????????????")
//        sentenceGapTime = scanner.nextLine().toInt()
//        println("sentenceGapTime = ${sentenceGapTime}s")
        combineMedia()
    }

    /**
     * ????????????
     */
    private fun combineMedia() {
        if (inputAudioSortExcelPath.isNotEmpty()) {
            EasyExcel.read(
                File(inputAudioSortExcelPath),
                SortedExcelData::class.java,
                object : ReadListener<SortedExcelData> {
                    val sortedExcelDataList = ArrayList<SortedExcelData>()
                    override fun invoke(data: SortedExcelData, context: AnalysisContext) {
                        sortedExcelDataList.add(data)
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
     * ????????????
     */
    fun combineVideoWithSortedList(sortedExcelDataList: ArrayList<SortedExcelData>) {
        val startTime = System.currentTimeMillis()
        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("ffprobe")

        val cmdPath = "ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        val subList = sortedExcelDataList.subList(startSortIndex - 1, endSortIndex - 1)
        val audioFileList = subList.map { File("$workingTempDir/compressed_audio/${it.ID}.mp3") }

        if (!isEnglish) {
            genLocalAudioFromAWS(subList)
        }

        Utils.emptyFileDir(workingTempDir)

        println("step: ??????????????????")
        compressAudio(cmdPath, subList)
        println("step: ??????????????????")

        println("step: ??????????????????????????????")
        generateGapTime(cmdPath)
        println("step: ??????????????????????????????")

        println("step: ??????????????????")
        subList.forEachIndexed { index, sortedExcelData ->
            outputImg(index, sortedExcelData)
        }
        println("step: ??????????????????")

        println("step: ??????????????????")
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

                        if (isEnglish) {
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


                            val fistPicFileName = "${audioFileName}-pic1.png"
                            val secondPicFileName = "${audioFileName}-pic2.png"

                            inputImgList.append("file '$workingDir/background_img/bg_listen.png'")
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

                        } else {
                            val localAudioFile = File(audioFile.path.replace(".mp3", "-jp.mp3"))
                            val localProbeResult = ffprobe.probe(localAudioFile.path)
                            val localFFmpegFormat = localProbeResult.format
                            val localAudioDuration = localFFmpegFormat.duration


                            inputAudioList.append("file '${audioFile.path}'")
                            inputAudioList.append("\n")
                            inputAudioList.append("file '${repeatGameTimeFile.path}'")
                            inputAudioList.append("\n")

                            inputAudioList.append("file '${localAudioFile.path}'")
                            inputAudioList.append("\n")
                            inputAudioList.append("file '${repeatGameTimeFile.path}'")
                            inputAudioList.append("\n")

                            inputAudioList.append("file '${audioFile.path}'")
                            inputAudioList.append("\n")
                            inputAudioList.append("file '${sentenceGapTimeFile.path}'")
                            inputAudioList.append("\n")

                            val secondPicFileName = "${audioFileName}-pic2.png"
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                            inputImgList.append("\n")
                            inputImgList.append("duration ${curAudioDuration + repeatGapTime + localAudioDuration + repeatGapTime + curAudioDuration + sentenceGapTime + 3}")
                            inputImgList.append("\n")
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                            inputImgList.append("\n")
                        }


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
            inputVideoList.append("file '${Utils.parentDir}/??????/FD-1077/????????????/1077??????-???-formatted.mp4'")
        } else {
            inputVideoList.append("file '${Utils.parentDir}/??????/FD-1077/????????????/FD??????-???????????????-1-formatted.mp4'")
        }
        inputVideoList.append("\n")

        for (audioFile in audioFileList) {
            val audioFileName = audioFile.name.split(".")[0]
            val outputVideoFile = File("$workingTempDir/output/handbrake-output-${audioFileName}.mp4")
            inputVideoList.append("file '${outputVideoFile.path}'")
            inputVideoList.append("\n")
        }
        if (isEnglish) {
            inputVideoList.append("file '${Utils.parentDir}/??????/FD-1077/????????????/???FD??????-???.mp4'")
        } else {
            inputVideoList.append("file '${Utils.parentDir}/??????/FD-1077/????????????/???FD??????-???-1-formatted.mp4'")
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

        println("step: ??????????????????")
        println("?????????????????????${outputVideoFormattedFile.path} ?????????${(System.currentTimeMillis() - startTime) / 1000L} s")
    }

    private fun genLocalAudioFromAWS(subList: MutableList<SortedExcelData>) {
        for (sortedExcelDataList in Utils.averageAssignFixLength(subList, 10)) {
            runBlocking {
                for (sortedExcelData in sortedExcelDataList) {
                    val localAudioFile =
                        File("${Utils.parentDir}/FFmpegUtil/aws-audio/output(1-1077)-EN-JP.mp4/" + sortedExcelData.ID + "-jp.mp3")
                    launch {
                        getMp3AndJson(
                            sortedExcelData.TRANS_JP,
                            VoiceId.Takumi,
                            Engine.Neural,
                            LanguageCode.JaJp,
                            localAudioFile
                        )
                    }
                }
            }
        }

    }

    private fun compressAudio(cmdPath: String, subList: MutableList<SortedExcelData>) {
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

                launch(Dispatchers.IO) {
                    val slowAudioFile = File("${Utils.parentDir}/FFmpegUtil/aws-audio/output(1-1077)-EN-JP.mp4/${(listFile.name.split(".")[0].toInt())}-jp.mp3")
                    val tempOutputFile =
                        File("$workingTempDir/compressed_audio/${(slowAudioFile.name.split(".")[0])}-temp.mp3").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }

                    ShellUtils.run(
                        "$cmdPath -y -i ${slowAudioFile.path} -ac 2 -ar 48000 ${tempOutputFile.path}",
                        object : ShellUtils.OnCommandExecOutputListener {
                            override fun onSuccess(line: String?) {
                            }

                            override fun onError(line: String?) {
                            }
                        })

                    val outputFile =
                        File("$workingTempDir/compressed_audio/${(slowAudioFile.name.split(".")[0])}.mp3").apply {
                            if (!this.parentFile.exists())
                                this.parentFile.mkdirs()
                        }

                    ShellUtils.run(
                        "$cmdPath -y -i ${tempOutputFile.path} -vcodec copy -af volume=2 ${outputFile.path}",
                        object : ShellUtils.OnCommandExecOutputListener {
                            override fun onSuccess(line: String?) {
                                tempOutputFile.delete()
                                println(line)
                            }

                            override fun onError(line: String?) {
                                println(line)
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

    private fun outputImg(index: Int, sortedExcelData: SortedExcelData) {
//        bgIndex = index % 7 + 1
        val bgImg = "$workingDir/background_img/bg_${bgIndex}.png"

        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)

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
        val outlineRect = Rectangle(1410, 215, 86, 86)

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
//        println("contentRect.width:${contentRect.width}")
//        println("contentRect.height:${contentRect.height}")
//
//        println("img.height:${width}")
//        println("img.height:${height}")

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
        sortedExcelData: SortedExcelData
    ) {
        val font = Font("Helvetica", Font.PLAIN, 92)
        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)
        val translateFont = Font("Hiragino Maru Gothic Pro", Font.PLAIN, 50)

        val file = File(inputImagePath)
        //?????????
        val image: Image = ImageIO.read(file)
        val bi = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
        val alpha = 1f
        //??????????????????
        val g2 = bi.createGraphics()

        //????????????,?????????????????????
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        val ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        //???????????????????????????
        g2.composite = ac
        g2.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null)
        //????????????

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

        //?????????????????????x,y

        if (isDarkBg()) {
            //?????????
            g2.color = Color.black
            g2.draw(sha)

            //?????????
            g2.color = Color.white
            g2.fill(sha)
        } else {
            //?????????
            g2.color = Color.black
            g2.draw(sha)

            //?????????
            g2.color = Color.black
            g2.fill(sha)
        }


        g2.font = font
        frc = g2.fontRenderContext
        tl = TextLayout((index + 1).toString(), indexFont, frc)
        sha = drawIndex(tl, indexFont, index + 1)

        if (isDarkBg()) {
            //?????????
            g2.color = Color.white
            g2.draw(sha)

            //?????????
            g2.color = Color.white
            g2.fill(sha)
        } else {
            //?????????
            g2.color = Color.decode("#2F6DAD")
            g2.draw(sha)

            //?????????
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
                //?????????
                g2.color = Color.decode("#FCF071")
                g2.draw(sha)

                //?????????
                g2.color = Color.decode("#FCF071")
                g2.fill(sha)
            } else {
                //?????????
                g2.color = Color.decode("#4DA0F8")
                g2.draw(sha)

                //?????????
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
        val cmdPath = "ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        var cmd = ""
        loop@ for (listFile in File("$workingDir/output/1-1077??????-???????????????").listFiles()) {
            if (!listFile.name.endsWith(".mp4")) {
                continue@loop
            }
            val outPutFile = File("$workingDir/output/1-1077??????-fixed/${listFile.name}").apply {
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
            inputVideoFinalList.append("file '${workingDir}/output/enpal-??????.mp4'")
            inputVideoFinalList.append("\n")
            inputVideoFinalList.append("file '${outPutFile.path}'")
            inputVideoFinalList.append("\n")
            inputVideoFinalList.append("file '${workingDir}/output/enpal-??????.mp4'")
            inputVideoFinalList.append("\n")


            val inputVideoFinalListFile = File("$workingTempDir/inputVideoFinalList.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    inputVideoFinalList.toString().substring(0, inputVideoFinalList.toString().length - 1)
                )
            }

            val outputVideoWithStartEndFile =
                File("$workingDir/output/1-1077??????-with-start-end/${listFile.name}").apply {
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

        Runtime.getRuntime().exec("${Utils.parentDir}/FFmpegUtil/final.sh").apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }
    }


    private suspend fun getMp3AndJson(
        fullArticle: String,
        voiceId: VoiceId,
        engine: Engine,
        languageCode: LanguageCode,
        file: File,
    ) {
        PollyClient { region = "us-west-2" }.use { polly ->
            if (!file.exists()) {
                polly.synthesizeSpeech(SynthesizeSpeechRequest {
                    this.text = fullArticle
                    this.voiceId = voiceId
                    this.engine = engine
                    this.languageCode = languageCode
                    this.outputFormat = OutputFormat.Mp3
                }) { resp ->
                    val audioData = resp.audioStream?.toByteArray()
                    file.apply {

                        if (!this.parentFile.exists())
                            this.parentFile.mkdirs();
                        if (!this.exists())
                            this.createNewFile();

                        writeBytes(audioData!!)
                    }
                    println("OutPut MP3 Success")
                }
            } else {
                println(file.path)
            }

        }
    }
}