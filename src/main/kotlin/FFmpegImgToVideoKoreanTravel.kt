@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import aws.sdk.kotlin.services.polly.PollyClient
import aws.sdk.kotlin.services.polly.model.*
import aws.smithy.kotlin.runtime.content.toByteArray
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import data_object.SortedExcelChineseTravelData2
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


    for (nameEndFix in arrayOf("7", "8")) {
        runBlocking {
            val ffmpegimgtovideoutilEN = FFmpegImgToVideoKoreanTravel()
            val ffmpegimgtovideoutilJP = FFmpegImgToVideoKoreanTravel()
            for (fFmpegImgToVideoUtil3 in arrayOf(ffmpegimgtovideoutilEN, ffmpegimgtovideoutilJP)) {
                fFmpegImgToVideoUtil3.inputAudioDirPath =
                    "/Users/yxg/Documents/from-iMac/视频课/素材/YT_KR-EN词汇/YT_KR-EN词汇-$nameEndFix-音频"
                fFmpegImgToVideoUtil3.inputAudioSortExcelPath =
                    "/Users/yxg/Documents/from-iMac/视频课/素材/YT_KR-EN词汇/YT_KR-EN词汇-$nameEndFix.xlsx"
                fFmpegImgToVideoUtil3.bgPicPath =
                    "/Users/yxg/Documents/from-iMac/视频课/素材/YT_KR-EN词汇/YT_韩语词汇背景/背景4.png"
            }

            launch(Dispatchers.IO) {
                ffmpegimgtovideoutilEN.isEnglish = true
                ffmpegimgtovideoutilEN.outputFileName =
                    "YT_KR-EN词汇-$nameEndFix"
                ffmpegimgtovideoutilEN.awsOutFileDirName =
                    "YT_KR-EN词汇"
                ffmpegimgtovideoutilEN.main()
            }
        }

    }

}

class FFmpegImgToVideoKoreanTravel {
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
    private var startGapTime = 0.5
    private var gapTime1Second = 1
    private var gapTime2Second = 2
    private var gapTime3Second = 3
    var startSortIndex = 1
    var endSortIndex = 151

    var awsOutFileDirName = ""
    var outputFileName = ""
    var bgPicPath = ""
    var inputAudioDirPath = ""
    var inputAudioSortExcelPath = ""

    private val repeatGameTimeFile get() = File("$workingTempDir/gap_time/$repeatGapTime.mp3")
    private val sentenceGapTimeFile get() = File("$workingTempDir/gap_time/$sentenceGapTime.mp3")
    private val startGapTimeFile get() = File("$workingTempDir/gap_time/$startGapTime.mp3")
    private val gapTime1SecondFile get() = File("$workingTempDir/gap_time/$gapTime1Second.mp3")
    private val gapTime2SecondFile get() = File("$workingTempDir/gap_time/$gapTime2Second.mp3")
    private val gapTime3SecondFile get() = File("$workingTempDir/gap_time/$gapTime3Second.mp3")

    init {
        val protocol = javaClass.getResource("")?.protocol ?: ""
        if (protocol == "file") {
            isDebug = true
        }
        println("protocol = $protocol")
        workingDir =
            if (isDebug) {
                "/Users/yxg/Documents/from-iMac/视频课/FFmpegUtil"
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
//        outputImg(1, SortedExcelChineseTravelData2("1", "이러쿵저러쿵하다", "ileokungjeoleokunghada", "to go to work and come back from work"))
    }

    /**
     * 合成音频
     */
    private fun combineMedia() {
        if (inputAudioSortExcelPath.isNotEmpty()) {
            EasyExcel.read(
                File(inputAudioSortExcelPath),
                SortedExcelChineseTravelData2::class.java,
                object : ReadListener<SortedExcelChineseTravelData2> {
                    val sortedExcelDataList = ArrayList<SortedExcelChineseTravelData2>()
                    override fun invoke(data: SortedExcelChineseTravelData2, context: AnalysisContext) {
                        if (data.SENTENCE.isNotEmpty()) {
                            sortedExcelDataList.add(data)
                        }
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
    fun combineVideoWithSortedList(sortedExcelDataList: List<SortedExcelChineseTravelData2>) {
        val startTime = System.currentTimeMillis()
        val inputAudioImgDirPath = "$workingTempDir/output_img"

        val ffprobe = FFprobe("ffprobe")

        val cmdPath = "ffmpeg"
        val hanBrakePath = "$workingDir/library/HandBrakeCLI"
        val subList = sortedExcelDataList.subList(0, sortedExcelDataList.size)

        genLocalAudioFromAWS(subList)

        Utils.emptyFileDir(workingTempDir)

        println("step: 开始压缩音频")
        compressAudio(cmdPath, subList)
        println("step: 结束压缩音频")

        val audioFileList = subList.map { File("$workingTempDir/compressed_audio/kr-gamevocab-w-${it.ID}.mp3") }.filter {
            it.exists()
        }

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
                outter@ fileList.forEachIndexed { index, audioFile ->

                    launch(Dispatchers.IO) {
                        if (!audioFile.exists()) {
                            println("file not exist: ${audioFile.path}")
                            return@launch
                        }

                        val inputAudioList = StringBuilder()
                        val inputImgList = StringBuilder()

                        val audioFileName = audioFile.name.split(".")[0]
                        val probeResult = ffprobe.probe(audioFile.path)
                        val fFmpegFormat = probeResult.format
                        val curAudioDuration = fFmpegFormat.duration

                        val localAudioFile = File(audioFile.path.replace(".mp3", "-en.mp3"))
                        val localProbeResult = ffprobe.probe(localAudioFile.path)
                        val localFFmpegFormat = localProbeResult.format
                        val localAudioDuration = localFFmpegFormat.duration

                        if (audioFileList.indexOf(audioFile) == 0) {
                            inputAudioList.append("file '${startGapTimeFile.path}'")
                            inputAudioList.append("\n")
                        }

                        inputAudioList.append("file '${localAudioFile.path}'")
                        inputAudioList.append("\n")
                        inputAudioList.append("file '${gapTime1SecondFile.path}'")
                        inputAudioList.append("\n")

                        inputAudioList.append("file '${audioFile.path}'")
                        inputAudioList.append("\n")
                        inputAudioList.append("file '${gapTime2SecondFile.path}'")
                        inputAudioList.append("\n")

                        inputAudioList.append("file '${audioFile.path}'")
                        inputAudioList.append("\n")
                        inputAudioList.append("file '${gapTime2SecondFile.path}'")
                        inputAudioList.append("\n")

                        inputAudioList.append("file '${audioFile.path}'")
                        inputAudioList.append("\n")
                        inputAudioList.append("file '${gapTime2SecondFile.path}'")
                        inputAudioList.append("\n")

                        val listenPicFileName = "${audioFileName}-pic2.png"
                        val fistPicFileName = "${audioFileName}-pic2.png"
                        val secondPicFileName = "${audioFileName}-pic2.png"

                        inputImgList.append("file '$inputAudioImgDirPath/${listenPicFileName}'")
                        inputImgList.append("\n")
                        if (audioFileList.indexOf(audioFile) == 0) {
                            inputImgList.append("duration ${localAudioDuration + gapTime1Second + startGapTime}")
                        } else {
                            inputImgList.append("duration ${localAudioDuration + gapTime1Second}")
                        }
                        inputImgList.append("\n")
                        inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + gapTime2Second}")
                        inputImgList.append("\n")
                        inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + gapTime2Second}")
                        inputImgList.append("\n")
                        if (isEnglish) {
                            inputImgList.append("file '$inputAudioImgDirPath/${fistPicFileName}'")
                        } else {
                            inputImgList.append("file '$inputAudioImgDirPath/${secondPicFileName}'")
                        }
                        inputImgList.append("\n")
                        inputImgList.append("duration ${curAudioDuration + gapTime2Second + 3}")
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

        inputVideoList.append("file '/Users/yxg/Documents/from-iMac/视频课/素材/LD-TravelPhrase/JP/CS-Youtube-头尾视频/Travel-JP-片头-formatted.mp4'")
        inputVideoList.append("\n")

        for (audioFile in audioFileList) {
            val audioFileName = audioFile.name.split(".")[0]
            val outputVideoFile = File("$workingTempDir/output/handbrake-output-${audioFileName}.mp4")
            inputVideoList.append("file '${outputVideoFile.path}'")
            inputVideoList.append("\n")
        }

        inputVideoList.append("file '/Users/yxg/Documents/from-iMac/视频课/素材/LD-TravelPhrase/JP/CS-Youtube-头尾视频/Travel-JP-片尾-formatted.mp4'")
        inputVideoList.append("\n")

        val inputVideoListFile = File("$workingTempDir/inputVideoList.txt").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
            writeText(
                inputVideoList.toString().substring(0, inputVideoList.toString().length - 1)
            )
        }

        val outputVideoFile = File("$workingDir/output/output-$outputFileName.mp4").apply {
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

        val outputVideoFormattedFile = File("$workingDir/output/${outputFileName}.mp4").apply {
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

    private fun compressAudio(cmdPath: String, subList: List<SortedExcelChineseTravelData2>) {
        runBlocking {
            for (sortedExcelData in subList) {
                val listFile = File("$inputAudioDirPath/kr-gamevocab-w-${sortedExcelData.ID}.mp3")
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
                        File("$workingDir/aws-audio/$awsOutFileDirName/${(listFile.name.split(".")[0])}-en.mp3")
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
                        "$cmdPath -y -i ${tempOutputFile.path} -vcodec copy -af volume=6 ${outputFile.path}",
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

        for (file in arrayOf(repeatGameTimeFile, sentenceGapTimeFile, startGapTimeFile, gapTime1SecondFile, gapTime2SecondFile, gapTime3SecondFile)) {
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

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $startGapTime ${startGapTimeFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $gapTime1Second ${gapTime1SecondFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $gapTime2Second ${gapTime2SecondFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $gapTime3Second ${gapTime3SecondFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            })
    }

    private fun isDarkBg() = arrayOf(5, 7).contains(bgIndex)

    private fun outputImg(index: Int, sortedExcelData: SortedExcelChineseTravelData2) {
//        bgIndex = index % 7 + 1
        val indexFont = Font("Apple SD Gothic Neo", Font.PLAIN, 50)

//        IJ.openImage(listenImg).processor.apply {
//            if (isDarkBg()) {
//                setColor(Color.WHITE)
//            } else {
//                setColor(Color.decode("#2F6DAD"))
//            }
//            setFont(indexFont)
//            setAntialiasedText(true)
//            drawIndex(indexFont, index + 1)
//
//            val outputImgFile =
//                File("$workingTempDir/output_img/${sortedExcelData.ID}-pic0.png").apply {
//                    if (!parentFile.exists())
//                        parentFile.mkdirs()
//                }
//
//            ImageIO.write(
//                this.bufferedImage,
//                "png",
//                outputImgFile
//            )
//        }
//
//        IJ.openImage(bgImg).processor.apply {
//            if (isDarkBg()) {
//                setColor(Color.WHITE)
//            } else {
//                setColor(Color.BLACK)
//            }
//            setFont(font)
//            setAntialiasedText(true)
//
//            drawSentence(font, sortedExcelData.SENTENCE)
//
//            if (isDarkBg()) {
//                setColor(Color.WHITE)
//            } else {
//                setColor(Color.decode("#2F6DAD"))
//            }
//            setFont(indexFont)
//            setAntialiasedText(true)
//            drawIndex(indexFont, index + 1)
//
//            val outputImgFile =
//                File("$workingTempDir/output_img/${sortedExcelData.ID}-pic1.png").apply {
//                    if (!parentFile.exists())
//                        parentFile.mkdirs()
//                }
//
//            ImageIO.write(
//                this.bufferedImage,
//                "png",
//                outputImgFile
//            )
//        }

        IJ.openImage(bgPicPath).processor.apply {
//            if (isDarkBg()) {
//                setColor(Color.WHITE)
//            } else {
//                setColor(Color.BLACK)
//            }


            drawSentence(sortedExcelData)

//            if (isDarkBg()) {
//                setColor(Color.WHITE)
//            } else {
//                setColor(Color.decode("#2F6DAD"))
//            }
            setColor(Color.decode("#03001B"))
            setFont(indexFont)
            setAntialiasedText(true)
            drawIndex(indexFont, index + 1)

            drawTranslate(sortedExcelData.TRANS_EN)

            val outputImgFile =
                File("$workingTempDir/output_img/kr-gamevocab-w-${sortedExcelData.ID}-pic2.png").apply {
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
        val outlineRect = Rectangle(35, 35, 100, 100)

        val contentRect = getContentRect(font, index.toString())

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height

        drawString(index.toString(), startX, startY)
    }

    private fun ImageProcessor.drawTranslate(translate: String) {
        var translateFont = Font("Apple SD Gothic Neo", Font.PLAIN, 170)
        setColor(Color.decode("#0082B5"))

        setFont(translateFont)
        setAntialiasedText(true)

        val outlineRect = Rectangle(72 + 30, 135 + 30, 1775 - 60, 810 - 60)

        var contentRect = getContentRect(translateFont, translate)

        while (contentRect.width > outlineRect.width) {
            translateFont = Font("Apple SD Gothic Neo", Font.PLAIN, translateFont.size - 1)
            setFont(translateFont)
            setAntialiasedText(true)
            contentRect = getContentRect(translateFont, translate)
        }

        val punchOffsetX = if (Utils.isPunch(translate.last().toString())) {
            getContentRect(translateFont, translate.last().toString()).width
        } else {
            0
        }

        var startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width - punchOffsetX) / 2
        val startY = outlineRect.y + outlineRect.height - 70


        if (font.size < 50) {
            //换行
            translateFont = Font("Apple SD Gothic Neo", Font.PLAIN, 50)
            setFont(translateFont)

            val firstLineSize = translate.split(" ").size / 2


            val sentenceWithLines = StringBuilder()

            translate.split(" ").forEachIndexed { index, s ->
                if (index <= firstLineSize) {
                    sentenceWithLines.append(s)
                    if (index == firstLineSize) {
                        sentenceWithLines.append("\n")
                    } else {
                        sentenceWithLines.append(" ")
                    }
                } else {
                    sentenceWithLines.append(s)
                    if (index != translate.split(" ").size) {
                        sentenceWithLines.append(" ")
                    }
                }
            }

            val realWriteSentence = sentenceWithLines.toString()

            val split = realWriteSentence.split("\n")

            while (getMaxWidth(font, split) > outlineRect.width) {
                translateFont = Font("Apple SD Gothic Neo", Font.PLAIN, font.size - 1)
                setFont(translateFont)
                setAntialiasedText(true)
            }

            var sentenceWidth = 0
            var sentenceHeight = 0

            for (s in split) {
                getContentRect(translateFont, s.replace("/", "")).apply {
                    sentenceHeight += this.height
                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
                }
            }


            var preLineHeight = 0
            for (s in split) {
                getContentRect(translateFont, s).apply {
                    startX = outlineRect.x + outlineRect.width / 2 - (this.width / 2)
                    val curLineStarY = startY + preLineHeight
                    drawString(s, startX, curLineStarY)
                    preLineHeight += this.height
                }
            }

        } else {
            drawString(translate, startX, startY)
        }

    }

    private fun ImageProcessor.drawSentence(sortedExelData: SortedExcelChineseTravelData2) {
        var font = Font("Apple SD Gothic Neo", Font.PLAIN, 250)
        setColor(Color.decode("#03001B"))
        setFont(font)
        setAntialiasedText(true)

        var pinyinFont = Font("PingFangSC-Light", Font.PLAIN, 120)

        val stringBuilder = StringBuilder()
        for (s in sortedExelData.SENTENCE.split("/")) {
            stringBuilder.append(s)
        }

        val luomaStringBuilder = StringBuilder()
        val pinyinList = sortedExelData.PINYIN.split("/")
        pinyinList.forEachIndexed { index, s ->
            val nextIndex = index + 1
            luomaStringBuilder.append(s.replace("_", ""))

            if (nextIndex < pinyinList.size) {
                if (!Utils.isPunch(pinyinList[nextIndex])) {
                    luomaStringBuilder.append(" ")
                }
            } else {
                luomaStringBuilder.append(" ")
            }

        }

        val sentence = stringBuilder.toString()

        val luoma = luomaStringBuilder.deleteCharAt(luomaStringBuilder.length - 1).toString()

        var contentRect = getContentRect(font, sentence)
        val punchOffsetX = if (Utils.isPunch(sentence.last().toString())) {
            getContentRect(font, sentence.last().toString()).width
        } else {
            0
        }
        val outlineRect = Rectangle(72 + 30, 135 + 30, 1775 - 60, 810 - 60)


        while (contentRect.width > outlineRect.width) {
            font = Font("Apple SD Gothic Neo", Font.PLAIN, font.size - 1)
            setFont(font)
            setAntialiasedText(true)
            contentRect = getContentRect(font, sentence)
        }

        var contentHeight = contentRect.height

        println("font.size = ${font.size}")
        var startX = outlineRect.x + outlineRect.width / 2 - ((contentRect.width - punchOffsetX) / 2)
        var startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height
        if (font.size < 60) {
            //换行
            font = Font("Apple SD Gothic Neo", Font.PLAIN, 60)
            setFont(font)

            val firstLineSize = sortedExelData.SENTENCE.split("/").size / 2


            val sentenceWithLines = StringBuilder()

            sortedExelData.SENTENCE.split("/").forEachIndexed { index, s ->
                if (index <= firstLineSize) {
                    sentenceWithLines.append(s)
                    if (index == firstLineSize) {
                        sentenceWithLines.append("\n")
                    }
                } else {
                    sentenceWithLines.append(s)
                }
            }

            val realWriteSentence = sentenceWithLines.toString()

            val split = realWriteSentence.split("\n")

            while (getMaxWidth(font, split) > outlineRect.width) {
                font = Font("Apple SD Gothic Neo", Font.PLAIN, font.size - 1)
                setFont(font)
                setAntialiasedText(true)
            }

            var sentenceWidth = 0
            var sentenceHeight = 0

            for (s in split) {
                getContentRect(font, s.replace("/", "")).apply {
                    sentenceHeight += this.height
                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
                }
            }

            startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + (sentenceHeight / split.size)

            var preLineHeight = 0
            contentHeight = 0
            for (s in split) {
                getContentRect(font, s).apply {
                    startX = outlineRect.x + outlineRect.width / 2 - (this.width / 2)
                    val curLineStarY = startY + preLineHeight
                    drawString(s, startX, curLineStarY)
                    preLineHeight += this.height
                    contentHeight = this.height
                }
            }


        } else {
            drawString(sentence, startX, startY)
        }

        setFont(pinyinFont)
        setAntialiasedText(true)

        var luomaContentRect = getContentRect(pinyinFont, luoma)


        while (luomaContentRect.width > outlineRect.width) {
            pinyinFont = Font("PingFangSC-Light", Font.PLAIN, pinyinFont.size - 1)
            setFont(pinyinFont)
            setAntialiasedText(true)
            luomaContentRect = getContentRect(pinyinFont, luoma)
        }

        val luomaPunchOffsetX = if (Utils.isPunch(luoma.last().toString())) {
            getContentRect(pinyinFont, luoma.last().toString()).width
        } else {
            0
        }

        var luomaStartX = outlineRect.x + outlineRect.width / 2 - ((luomaContentRect.width - luomaPunchOffsetX) / 2)
        var luomaStartY = startY - contentHeight

        if (pinyinFont.size < 60) {
            //换行
            pinyinFont = Font("PingFangSC-Light", Font.PLAIN, 60)
            setFont(pinyinFont)

            val firstLineSize = sortedExelData.PINYIN.split("/").size / 2


            val sentenceWithLines = StringBuilder()

            sortedExelData.PINYIN.split("/").forEachIndexed { index, s ->
                val nextIndex = index + 1
                if (index <= firstLineSize) {
                    sentenceWithLines.append(s.replace("_", ""))
                    if (index == firstLineSize) {
                        sentenceWithLines.append("\n")
                    } else {
                        if (nextIndex < pinyinList.size) {
                            if (!Utils.isPunch(pinyinList[nextIndex])) {
                                sentenceWithLines.append(" ")
                            }
                        } else {
                            sentenceWithLines.append(" ")
                        }
                    }
                } else {
                    sentenceWithLines.append(s.replace("_", ""))
                    if (index != sortedExelData.PINYIN.split("/").size) {
                        if (nextIndex < pinyinList.size) {
                            if (!Utils.isPunch(pinyinList[nextIndex])) {
                                sentenceWithLines.append(" ")
                            }
                        } else {
                            sentenceWithLines.append(" ")
                        }
                    }
                }
            }

            val realWriteSentence = sentenceWithLines.toString()

            val split = realWriteSentence.split("\n")

            while (getMaxWidth(pinyinFont, split) > outlineRect.width) {
                pinyinFont = Font("PingFangSC-Light", Font.PLAIN, pinyinFont.size - 1)
                setFont(pinyinFont)
                setAntialiasedText(true)
            }

            var sentenceHeight = 0

            for (s in split) {
                getContentRect(pinyinFont, s.replace("/", "")).apply {
                    sentenceHeight += this.height
                }
            }

            luomaStartY = startY - sentenceHeight - 30

            var preLineHeight = 0
            for (s in split) {
                getContentRect(pinyinFont, s).apply {
                    luomaStartX = outlineRect.x + outlineRect.width / 2 - (this.width / 2)
                    val curLineStarY = luomaStartY + preLineHeight
                    drawString(s, luomaStartX, curLineStarY)
                    preLineHeight += this.height
                }
            }


        } else {
            drawString(luoma, luomaStartX, luomaStartY)
        }

//        if (contentRect.width > (outlineRect.width)) {
//            val firstLineSentence = StringBuilder()
//            val pinyinLineSentence = StringBuilder()
//            var curLineSentence = StringBuilder()
//
//            sortedExelData.SENTENCE.split("/").forEachIndexed { index, c ->
//                val word = c
//                firstLineSentence.append(word)
//                firstLineSentence.append("/")
//                curLineSentence.append(word)
//                pinyinLineSentence.append(pinyinList[index].replace("_", ""))
//                pinyinLineSentence.append("/")
//
//                val nextIndex = index + 1
//                if (nextIndex < sentence.length) {
//                    val nextWord = sentence[nextIndex]
//                    val nextRect = getContentRect(font, "$curLineSentence$nextWord")
//                    val curRect = getContentRect(font, curLineSentence.toString())
//                    if (curRect.width <= outlineRect.width && nextRect.width > outlineRect.width) {
//                        curLineSentence = StringBuilder()
//                        firstLineSentence.append("\n")
//                        pinyinLineSentence.append("\n")
//                    }
//                }
//            }
//
//            val realWriteSentence = firstLineSentence.toString().substring(0, firstLineSentence.length - 1)
//
//            var sentenceWidth = 0
//            var sentenceHeight = 0
//
//            val split = realWriteSentence.split("\n")
//            val pinyinSplit = pinyinLineSentence.split("\n")
//            for (s in split) {
//                getContentRect(font, s.replace("/", "")).apply {
//                    sentenceHeight += this.height
//                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
//                }
//            }
//
//            val startX = outlineRect.x + outlineRect.width / 2 - (sentenceWidth / 2)
//            val startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + (sentenceHeight / split.size)
//
//            pinyinLineSentence.split("\n")
//
//            var preLineHeight = 0
//
//            split.forEachIndexed { index, s ->
//                getContentRect(font, s.replace("/", "")).apply {
//                    val curLineStarY = startY + preLineHeight
//
//                    var drawStartX = startX
//                    s.split("/").forEachIndexed { charIndex, c ->
//                        val pinyin = pinyinSplit[index].trim().split("/")[charIndex]
//
//                        val wordContentRect = getContentRect(font, c)
//                        val pinyinContentRect = getContentRect(pinyinFont, pinyin)
//
//                        setColor(Color.BLACK)
//                        setFont(font)
//                        setAntialiasedText(true)
//                        drawString(c, drawStartX, curLineStarY)
//
//                        val pinyinStartX = drawStartX + (wordContentRect.width - pinyinContentRect.width) / 2
//                        val pinyinStartY = curLineStarY - wordContentRect.height / 2 - pinyinContentRect.height
//
//                        if (!Utils.isPunch(c)) {
//                            setColor(Color.BLACK)
//                            setFont(pinyinFont)
//                            setAntialiasedText(true)
//                            drawString(pinyin, pinyinStartX, pinyinStartY)
//                        }
//                        drawStartX += wordContentRect.width
//                    }
//
//                    preLineHeight += this.height
//                    preLineHeight += 15
//                }
//            }
//
//        } else {
//            val startX = outlineRect.x + outlineRect.width / 2 - ((contentRect.width - punchOffsetX) / 2)
//            val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + 80
//
//            var drawStartX = startX
//
//            sortedExelData.SENTENCE.split("/").forEachIndexed { index, c ->
//
//                val pinyin = pinyinList[index].replace("_", "")
//
//                val wordContentRect = getContentRect(font, c)
//                val pinyinContentRect = getContentRect(pinyinFont, pinyin)
//
//                setColor(Color.BLACK)
//                setFont(font)
//                setAntialiasedText(true)
//                drawString(c, drawStartX, startY)
//
//                val pinyinStartX = drawStartX + (wordContentRect.width - pinyinContentRect.width) / 2
//                val pinyinStartY = startY - wordContentRect.height / 2 - pinyinContentRect.height
//
//                if (!Utils.isPunch(c)) {
//                    setColor(Color.BLACK)
//                    setFont(pinyinFont)
//                    setAntialiasedText(true)
//                    drawString(pinyin, pinyinStartX, pinyinStartY)
//                }
//
//                drawStartX += wordContentRect.width
//            }
//        }

    }

    private fun getMaxWidth(font: Font, split: List<String>): Int {
        var maxWidth = 0
        for (s in split) {
            getContentRect(font, s.replace("/", "")).apply {
                if (this.width > maxWidth) {
                    maxWidth = this.width
                }
            }
        }
        return maxWidth
    }

    private fun getContentRect(font: Font, content: String): Rectangle {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(content, frc).bounds
    }

    private fun genLocalAudioFromAWS(subList: List<SortedExcelChineseTravelData2>) {
        for (sortedExcelDataList in Utils.averageAssignFixLength(subList, 10)) {
            runBlocking {
                for (sortedExcelData in sortedExcelDataList) {
                    val localAudioFile =
                        File("$workingDir/aws-audio/$awsOutFileDirName/" + "kr-gamevocab-w-${sortedExcelData.ID}" + "-en.mp3")
                    println(localAudioFile.path)
                    launch {
                        getMp3AndJson(
                            sortedExcelData.TRANS_EN,
                            VoiceId.Matthew,
                            Engine.Neural,
                            LanguageCode.EnUs,
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