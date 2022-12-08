@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import aws.sdk.kotlin.services.polly.PollyClient
import aws.sdk.kotlin.services.polly.model.*
import aws.smithy.kotlin.runtime.content.toByteArray
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
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess


fun main(args: Array<String>) {


    runBlocking {
        val ffmpegimgtovideoutilEN = FFmpegImgToVideoB1()
        val ffmpegimgtovideoutilJP = FFmpegImgToVideoB1()
//        for (fFmpegImgToVideoUtil3 in arrayOf(ffmpegimgtovideoutilEN, ffmpegimgtovideoutilJP)) {
//            fFmpegImgToVideoUtil3.startSortIndex = 1
//            fFmpegImgToVideoUtil3.endSortIndex = 760
//        }

        launch(Dispatchers.IO) {
            ffmpegimgtovideoutilEN.isEnglish = true
            ffmpegimgtovideoutilEN.awsOutFileDirName = "B1-Audio-EN"
            ffmpegimgtovideoutilEN.outputFileName =
                "B1-output-EN.mp4"
            ffmpegimgtovideoutilEN.main()
        }
        launch(Dispatchers.IO) {
            ffmpegimgtovideoutilJP.isEnglish = false
            ffmpegimgtovideoutilJP.awsOutFileDirName = "B1-Audio-JP"
            ffmpegimgtovideoutilJP.outputFileName =
                "B1-output-JP.mp4"
            ffmpegimgtovideoutilJP.main()
        }
    }

}

class FFmpegImgToVideoB1 {
    var bgIndex = 1
    private var isDebug = false
    var isEnglish = false
    private var workingDir = ""
    private var workingTempDir = ""

    private var repeatGapTime = 3
    private var sentenceGapTime = 4

    var awsOutFileDirName = ""
    var outputFileName = ""
    private var inputAudioDirPath = "${Utils.parentDir}/素材/B1系列/B1音频汇总"
    private var inputAudioSortExcelPath =
        "${Utils.parentDir}/素材/B1系列/B1VOCAB1969-汇总表.xlsx"

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

                }).sheet().doRead()
        } else {
            combineVideoWithSortedList(ArrayList())
        }
    }

    /**
     * 合成视频
     */
    fun combineVideoWithSortedList(sortedExcelDataList: List<SortedExcelData2>) {
        val startTime = System.currentTimeMillis()
        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("ffprobe")

        val cmdPath = "ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        val subList = sortedExcelDataList.subList(0, sortedExcelDataList.size)
        val audioFileList = subList.map { File("$workingTempDir/compressed_audio/yt-en-b1vocab-${it.ID}.mp3") }

        if (!isEnglish) {
            genLocalAudioFromAWS(subList)
        }

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
            inputVideoList.append("file '${Utils.parentDir}/素材/B1系列/片头片尾/FD片头-新英语介绍-formatted.mp4'")
        } else {
            inputVideoList.append("file '${Utils.parentDir}/素材/B1系列/片头片尾/FD片头-新日语介绍-formatted.mp4'")
        }
        inputVideoList.append("\n")

        for (audioFile in audioFileList) {
            val audioFileName = audioFile.name.split(".")[0]
            val outputVideoFile = File("$workingTempDir/output/handbrake-output-${audioFileName}.mp4")
            inputVideoList.append("file '${outputVideoFile.path}'")
            inputVideoList.append("\n")
        }

        if (isEnglish) {
            inputVideoList.append("file '${Utils.parentDir}/素材/B1系列/片头片尾/新FD片尾-英-formatted.mp4'")
        } else {
            inputVideoList.append("file '${Utils.parentDir}/素材/B1系列/片头片尾/新FD片尾-日-formatted.mp4'")
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

        outputVideoFile.delete()

        println("step: 结束合并视频")
        println("输出文件路径：${outputVideoFormattedFile.path} 耗时：${(System.currentTimeMillis() - startTime) / 1000L} s")
    }

    private fun compressAudio(cmdPath: String, subList: List<SortedExcelData2>) {
        runBlocking {
            for (listFile in File(inputAudioDirPath).listFiles().filter { file ->
                subList.find {
                    "yt-en-b1vocab-${it.ID}" == file.name.split(".")[0]
                } != null
            }) {
                launch(Dispatchers.IO) {
                    val outputFile =
                        File("$workingTempDir/compressed_audio/${(listFile.name.split(".")[0])}.mp3").apply {
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
                    val slowAudioFile =
                        File("$workingDir/aws-audio/$awsOutFileDirName/${(listFile.name.split(".")[0])}-jp.mp3")
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

    private fun outputImg(index: Int, sortedExcelData: SortedExcelData2) {
//        bgIndex = index % 7 + 1
        val bgImg = "${Utils.parentDir}/素材/B1系列/背景图/B1背景图.png"
        val listenImg = "${Utils.parentDir}/素材/B1系列/背景图/B1listen图.png"

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
                File("$workingTempDir/output_img/yt-en-b1vocab-${sortedExcelData.ID}-pic0.png").apply {
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
                File("$workingTempDir/output_img/yt-en-b1vocab-${sortedExcelData.ID}-pic1.png").apply {
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
                File("$workingTempDir/output_img/yt-en-b1vocab-${sortedExcelData.ID}-pic2.png").apply {
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

    private fun genLocalAudioFromAWS(subList: List<SortedExcelData2>) {
        for (sortedExcelDataList in Utils.averageAssignFixLength(subList, 10)) {
            runBlocking {
                for (sortedExcelData in sortedExcelDataList) {
                    val localAudioFile =
                        File("$workingDir/aws-audio/$awsOutFileDirName/" + "yt-en-b1vocab-${sortedExcelData.ID}" + "-jp.mp3")
                    println(localAudioFile.path)
                    launch {
                        getMp3AndJson(
                            sortedExcelData.TRANS_JP_ZHUYIN.ifEmpty { sortedExcelData.TRANS_JP },
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