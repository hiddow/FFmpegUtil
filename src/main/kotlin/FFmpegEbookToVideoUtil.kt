@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import data_object.*
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
import java.io.InputStreamReader
import javax.imageio.ImageIO
import kotlin.system.exitProcess

val epBookJsonAdapter: JsonAdapter<EPBook> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(EPBook::class.java)

val epBookWordJsonAdapter: JsonAdapter<EPBookWord> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(EPBookWord::class.java)

val epBookSentenceJsonAdapter: JsonAdapter<EPBookSentence> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(EPBookSentence::class.java)

val epBookParagraphJsonAdapter: JsonAdapter<EPParagraph> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(EPParagraph::class.java)

fun main(args: Array<String>) {
    arrayOf("005", "003", "011", "015", "001", "007", "004", "002", "009")
    val indexArray = arrayOf("001")

    indexArray.forEachIndexed { index, bookIdStr ->
        FFmpegEbookToVideoUtil().apply {
            bookId = bookIdStr
            workingTempDir = "$workingDir/temp-book/book-$bookId"
            main()
        }
    }


//    FFmpegEbookToVideoUtil().apply {
//        bookId = "005"
//        workingTempDir = "$workingDir/temp/book-$bookId"
//        main()
//    }

}

class FFmpegEbookToVideoUtil {
    var isDebug = false
    var workingDir = ""
    var workingTempDir = ""
    var bookId = ""

    val ffprobe get() = FFprobe("$workingDir/library/ffprobe")
    val cmdPath get() = "$workingDir/library/ffmpeg"
    val hanBrakePath get() = "$workingDir/library/HandBrakeCLI"

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
        if (workingDir.contains(" ")) {
            println("workingDir不能包含空格")
            exitProcess(0)
        }
    }

    fun main() {
        val startTime = System.currentTimeMillis()
        println("workingTempDir = $workingTempDir")
//        Utils.emptyFileDir(workingTempDir)

        val inputVideoList = StringBuilder()
        val videoFileList = ArrayList<File>()

        val file =
            File("/Users/lingodeer-yxg/Downloads/aws-doc-sdk-examples-main/kotlin/services/polly/output/books/json_corrected/$bookId.json")
        if (file.exists()) {
            val readText =
                file.readText()
            epBookJsonAdapter.fromJson(readText)?.let { epBook ->
                outter@ for (epBookChapter in epBook.chapters_array) {

//                    if (epBookChapter.chapter_id != "001") {
//                        continue@outter
//                    }

                    val chapterInfo = if (epBookChapter.part_name.isNotEmpty()) {
                        epBookChapter.part_name + "\n" + epBookChapter.chapter_name
                    } else {
                        epBookChapter.chapter_name
                    }

                    val chapterOutputVideoFile =
                        generateCoverVideoFile(
                            cmdPath,
                            ffprobe,
                            hanBrakePath,
                            chapterInfo,
                            epBook.ebook_id + epBookChapter.chapter_id + "000", 3
                        )
                    videoFileList.add(chapterOutputVideoFile)
                    val chapterVideoFileList = ArrayList<File>()
                    for (epParagraphs in Utils.averageAssignFixLength(epBookChapter.paragraphs_array, 5)) {
                        runBlocking {
                            inner@ for (epParagraph in epParagraphs) {

//                                if (epParagraph.paragraph_id != "007") {
//                                    continue@inner
//                                }

                                launch(Dispatchers.IO) {

                                    val curParagraphId =
                                        "${epBook.ebook_id}${epBookChapter.chapter_id}${epParagraph.paragraph_id}"

                                    var handBrakeOutputVideoFile =
                                        File("$workingTempDir/output/handbrake-output-${curParagraphId}.mp4").apply {
                                            if (!this.parentFile.exists())
                                                this.parentFile.mkdirs()
                                        }

                                    if (!handBrakeOutputVideoFile.exists()) {
                                        handBrakeOutputVideoFile = generateParagraphVideoFile(
                                            epParagraph,
                                            epBookChapter,
                                            epBook,
                                            cmdPath,
                                            ffprobe,
                                            hanBrakePath
                                        )
                                    }
                                    chapterVideoFileList.add(handBrakeOutputVideoFile)
                                }
                            }
                        }
                    }

                    videoFileList.addAll(
                        chapterVideoFileList.sortedBy {
                            it.name.replace(".mp4", "").split("-")[2].substring(0, 3).toInt()
                        }.sortedBy {
                            it.name.replace(".mp4", "").split("-")[2].substring(3, 6).toInt()
                        }.sortedBy {
                            it.name.replace(".mp4", "").split("-")[2].substring(6, 9).toInt()
                        }
                    )
                }

                val coverOutputVideoFile =
                    generateCoverVideoFile(cmdPath, ffprobe, hanBrakePath, epBook.ebook_name, epBook.ebook_id, 2)
                videoFileList.add(0, coverOutputVideoFile)


                val outputVideoFormattedFile = outputFile(inputVideoList, videoFileList, "en")
                println("输出文件路径：${outputVideoFormattedFile.path} 耗时：${(System.currentTimeMillis() - startTime) / 1000L} s")

                val outputVideoFormattedLocateFile = outputFile(inputVideoList, videoFileList, "jp")
                println("输出文件路径：${outputVideoFormattedLocateFile.path} 耗时：${(System.currentTimeMillis() - startTime) / 1000L} s")

                println("step: 结束合并视频")
            }
        }
    }

    private fun outputFile(
        inputVideoList: StringBuilder,
        videoFileList: ArrayList<File>,
        locateCode: String
    ): File {
        val outputFileName = "book-$bookId-$locateCode.mp4"

        inputVideoList.append("file '/Users/lingodeer-yxg/Desktop/视频课/素材/ebook/片头片尾/enpal-片头-new.mp4'")
        inputVideoList.append("\n")

        for (audioFile in videoFileList) {
            inputVideoList.append("file '${audioFile.path}'")
            inputVideoList.append("\n")
        }

        inputVideoList.append("file '/Users/lingodeer-yxg/Desktop/视频课/素材/ebook/片头片尾/enpal-片尾-new-${locateCode}.mp4'")
        inputVideoList.append("\n")

        val inputVideoListFile = File("$workingTempDir/inputVideoList-$locateCode.txt").apply {
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

        val outputVideoFixVolumeFile = File("$workingDir/output/fix-volume-speed-${outputFileName}").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        ShellUtils.run(
            "$cmdPath -y -i ${outputVideoFormattedFile.path} -vcodec copy -af volume=3 ${outputVideoFixVolumeFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                    println(line)
                }

                override fun onError(line: String?) {
                    println(line)
                }
            })
        return outputVideoFormattedFile
    }

    private fun generateParagraphVideoFile(
        epParagraph: EPParagraph,
        epBookChapter: EPBookChapter,
        epBook: EPBook,
        cmdPath: String,
        ffprobe: FFprobe,
        hanBrakePath: String
    ): File {

        val font = Font("Helvetica", Font.PLAIN, 85)
        val indexFont = Font("Helvetica", Font.PLAIN, 48)

        val textList = ArrayList<String>()

        var oneImgText: StringBuilder
        for (epBookSentence in epParagraph.sentences_array) {
            oneImgText = StringBuilder()
            val split = epBookSentence.sent_txt.split(",")

            split.forEachIndexed { index, sentencePart ->
                println(sentencePart)
                var textToDraw = oneImgText.toString() + sentencePart
                var lines = getRealDrawSentence(font, textToDraw, 100).split("\n").size

                if (lines <= 3) {
                    oneImgText.append(sentencePart)
                    val isLastPart = (index == split.size - 1)

                    if (isLastPart) {
                        textList.add(oneImgText.toString().trim())
                    } else {
                        if (index != split.size - 1) {
                            oneImgText.append(",")
                        }
                    }
                } else {
                    if (oneImgText.isNotEmpty()) {
                        textList.add(oneImgText.toString().trim())
                    }
                    oneImgText = StringBuilder()

                    textToDraw = oneImgText.toString() + sentencePart
                    lines = getRealDrawSentence(font, textToDraw, 100).split("\n").size

                    if (lines > 3) {
                        val splitWords = sentencePart.split(" ")
                        splitWords.forEachIndexed { wordIndex, wordPart ->
                            textToDraw = oneImgText.toString() + wordPart
                            lines = getRealDrawSentence(font, textToDraw, 100).split("\n").size

                            val isLastPart = (index == split.size - 1) && (wordIndex == splitWords.size - 1)

                            var forceAdd = false
                            if (isLastPart && getRealDrawSentence(font, sentencePart, 100).split("\n").size == 1) {
                                forceAdd = true
                            }

                            if (lines <= 3 || forceAdd) {
                                oneImgText.append(wordPart)
                                if (isLastPart) {
                                    textList.add(oneImgText.toString().trim())
                                } else {
                                    if (wordIndex < splitWords.size) {
                                        oneImgText.append(" ")
                                    }
                                }
                            } else {
                                if (oneImgText.isNotEmpty()) {
                                    textList.add(oneImgText.toString().trim())
                                }
                                oneImgText = StringBuilder()
                                oneImgText.append(wordPart)

                                if (isLastPart) {
                                    textList.add(oneImgText.toString().trim())
                                } else {
                                    if (wordIndex < splitWords.size) {
                                        oneImgText.append(" ")
                                    }
                                }
                            }
                        }
                    } else {
                        oneImgText.append(sentencePart)
                        val isLastPart = (index == split.size - 1)
                        if (isLastPart) {
                            textList.add(oneImgText.toString().trim())
                        } else {
                            if (index != split.size - 1) {
                                oneImgText.append(",")
                            }
                        }
                    }
                }
            }
        }

        val outImgFileList = ArrayList<File>()

        textList.forEachIndexed { imgIndex, textToDraw ->
            ///Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/background_img/ebook/YT-story模板/005.png
            val bgImg = "$workingDir/background_img/ebook/YT-story模板/$bookId.png"

            IJ.openImage(bgImg).processor.apply {
                setColor(Color.BLACK)
                setFont(font)
                setAntialiasedText(true)

                drawSentence(font, textToDraw, 100)

                setColor(Color.WHITE)
                setFont(indexFont)
                setAntialiasedText(true)
                drawIndex(indexFont, epBookChapter.chapter_id)

                val outputImgFile =
                    File("$workingTempDir/output_img/${epBook.ebook_id}${epBookChapter.chapter_id}${epParagraph.paragraph_id}-pic${imgIndex + 1}.png").apply {
                        if (!parentFile.exists())
                            parentFile.mkdirs()
                    }

                outImgFileList.add(outputImgFile)
                ImageIO.write(
                    this.bufferedImage,
                    "png",
                    outputImgFile
                )
            }
        }

        val startTimeList = ArrayList<String>()

        val allWordArray = ArrayList<EPBookWord>()

        for (epBookSentence in epParagraph.sentences_array) {
            allWordArray.addAll(epBookSentence.words_array)
        }

        var startIndex = 0
        var lastWord = -1
        for (s in textList) {
            println(s)
            allWordArray.forEachIndexed { index, epBookWord ->
                if (index > lastWord) {
                    val wordStartIndex = s.indexOf(epBookWord.word, startIndex)
                    if (wordStartIndex != -1) {
                        if (startIndex == 0) {
                            println(epBookWord.word)
                            println(epBookWord.audio_time_1)
                            startTimeList.add(epBookWord.audio_time_1)
                        }

                        val wordEndIndex = wordStartIndex + epBookWord.word.length
                        startIndex = wordEndIndex
                        lastWord = index
                    }
                }
            }

            startIndex = 0
        }

        val curParagraphId = "${epBook.ebook_id}${epBookChapter.chapter_id}${epParagraph.paragraph_id}"

        var inputAudiPath =
            "/Users/lingodeer-yxg/Downloads/aws-doc-sdk-examples-main/kotlin/services/polly/output/mp3/books/$bookId-f/${curParagraphId}-f.mp3"

        println("step: 开始压缩音频")
        compressAudio(cmdPath, listOf(File(inputAudiPath)))
        println("step: 结束压缩音频")
        inputAudiPath = "$workingTempDir/compressed_audio/${curParagraphId}-f.mp3"

        val audioDuration = ffprobe.probe(inputAudiPath).format.duration
        println("audioDuration: $audioDuration")

        val inputImgList = StringBuilder()
        outImgFileList.forEachIndexed { index, file ->
            var duration = ""
            val nextIndex = index + 1
            if (nextIndex < startTimeList.size) {
                duration = if (index == 0) {
                    startTimeList[nextIndex]
                } else {
                    (startTimeList[nextIndex].toLong() - startTimeList[index].toLong()).toString()
                }
            }
            if (duration == "") {
                duration = if (startTimeList.isNotEmpty()) {
                    if (index == 0) {
                        (audioDuration * 1000F).toString()
                    } else {
                        (audioDuration * 1000F - (startTimeList.last().toDouble())).toString()
                    }
                } else {
                    (audioDuration * 1000F).toString()
                }
            }
            inputImgList.append("file '${file.path}'")
            inputImgList.append("\n")
            inputImgList.append("duration ${duration.toDouble() / 1000}")
            inputImgList.append("\n")
            if (index == outImgFileList.size - 1) {
                inputImgList.append("file '${file.path}'")
                inputImgList.append("\n")
                inputImgList.append("duration ${duration.toDouble() / 1000}")
                inputImgList.append("\n")
            }
        }

        val inputImgListFile =
            File("$workingTempDir/input_txt/inputImgList-${curParagraphId}.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    inputImgList.toString().substring(0, inputImgList.toString().length - 1)
                )
            }


        val outputVideoFile = File("$workingTempDir/output/output-${curParagraphId}.mp4").apply {
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
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val keyOutputVideoFile =
            File("$workingTempDir/output/key-output-${curParagraphId}.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        cmd =
            "$cmdPath -y -ss 0 -t ${ffprobe.probe(inputAudiPath).format.duration.toInt()} -accurate_seek -i ${outputVideoFile.path} -codec copy -avoid_negative_ts 1 ${keyOutputVideoFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val outputVideoFormattedFile =
            File("$workingTempDir/output/formatted-output-${curParagraphId}.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        cmd =
            "$cmdPath -y -i ${keyOutputVideoFile.path} -i $inputAudiPath ${outputVideoFormattedFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val outputVideoSpeedFile =
            File("$workingTempDir/output/formatted-output-change-speed-${curParagraphId}.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        ShellUtils.run(
            "$cmdPath -y -i ${outputVideoFormattedFile.path} -filter_complex [0:v]setpts=1.282051282051282*PTS[v];[0:a]atempo=0.78[a] -map [v] -map [a] ${outputVideoSpeedFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }
            })

        val handBrakeOutputVideoFile =
            File("$workingTempDir/output/handbrake-output-${curParagraphId}.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        ShellUtils.run(
            "$hanBrakePath -i ${outputVideoSpeedFile.path} -o ${handBrakeOutputVideoFile.path} -e x264 -q 10 -B 160",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                    println(line)
                }

                override fun onError(line: String?) {
                    println(line)
                }
            }
        )
        return handBrakeOutputVideoFile
    }

    private fun getRealDrawSentence(font: Font, textToDraw: String, bottomMargin: Int): String {
        val contentRect = getContentRect(font, textToDraw)
        val outlineRect = Rectangle(0 + 60, 175 + 30, 1920 - 60 - 60, 750 - 30 - bottomMargin)

        if (contentRect.width > (outlineRect.width)) {
            val firstLineSentence = StringBuilder()
            var curLineSentence = StringBuilder()
            textToDraw.split(" ").forEachIndexed { index, word ->
                firstLineSentence.append(word)
                firstLineSentence.append(" ")
                curLineSentence.append(word)
                curLineSentence.append(" ")

                val nextIndex = index + 1
                if (nextIndex < textToDraw.split(" ").size) {
                    val nextWord = textToDraw.split(" ")[nextIndex]
                    val nextRect = getContentRect(font, "$curLineSentence$nextWord ")
                    val curRect = getContentRect(font, curLineSentence.toString())
                    if (curRect.width <= outlineRect.width && nextRect.width > outlineRect.width) {
                        curLineSentence = StringBuilder()
                        firstLineSentence.append("\n")
                    }
                }
            }
            return firstLineSentence.toString().substring(0, firstLineSentence.length - 1)
        } else {
            return textToDraw
        }
    }

    private fun ImageProcessor.drawSentence(font: Font, textToDraw: String, bottomMargin: Int) {
        val outlineRect = Rectangle(0 + 60, 175 + 30, 1920 - 60 - 60, 750 - 30 - bottomMargin)
        val realDrawSentence = getRealDrawSentence(font, textToDraw, bottomMargin)

        var sentenceWidth = 0
        var sentenceHeight = 0

        val split = realDrawSentence.split("\n")
        for (s in split) {
            getContentRect(font, s).apply {
                sentenceHeight += this.height
                sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
            }
        }

        val startX = outlineRect.x + outlineRect.width / 2 - (sentenceWidth / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + (sentenceHeight / split.size)

        var preLineHeight = 0
        for (s in split) {
            getContentRect(font, s).apply {
                val curLineStarY = startY + preLineHeight
                drawString(s, startX, curLineStarY)
                preLineHeight += this.height
                preLineHeight += 15
            }
        }

//        val contentRect = getContentRect(font, textToDraw)
//        val outlineRect = Rectangle(0 + 60, 175 + 30, 1920 - 60 - 60, 750 - 30 - bottomMargin)
//
//        if (contentRect.width > (outlineRect.width)) {
//            val firstLineSentence = StringBuilder()
//            var curLineSentence = StringBuilder()
//            textToDraw.split(" ").forEachIndexed { index, word ->
//                firstLineSentence.append(word)
//                firstLineSentence.append(" ")
//                curLineSentence.append(word)
//                curLineSentence.append(" ")
//
//                val nextIndex = index + 1
//                if (nextIndex < textToDraw.split(" ").size) {
//                    val nextWord = textToDraw.split(" ")[nextIndex]
//                    val nextRect = getContentRect(font, "$curLineSentence$nextWord ")
//                    val curRect = getContentRect(font, curLineSentence.toString())
//                    if (curRect.width <= outlineRect.width && nextRect.width > outlineRect.width) {
//                        curLineSentence = StringBuilder()
//                        firstLineSentence.append("\n")
//                    }
//                }
//            }
//
//            val realWriteSentence =
//                firstLineSentence.toString().substring(0, firstLineSentence.length - 1)
//
//            var sentenceWidth = 0
//            var sentenceHeight = 0
//
//            val split = realWriteSentence.split("\n")
//            for (s in split) {
//                getContentRect(font, s).apply {
//                    sentenceHeight += this.height
//                    sentenceWidth = kotlin.math.max(sentenceWidth, this.width)
//                }
//            }
//
//            val startX = outlineRect.x + outlineRect.width / 2 - (sentenceWidth / 2)
//            val startY = outlineRect.y + outlineRect.height / 2 - (sentenceHeight / 2) + (sentenceHeight / split.size)
//
//            var preLineHeight = 0
//            for (s in split) {
//                getContentRect(font, s).apply {
//                    val curLineStarY = startY + preLineHeight
//                    drawString(s, startX, curLineStarY)
//                    preLineHeight += this.height
//                    preLineHeight += 15
//                }
//            }
//
//
//        } else {
//            val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
//            val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height
//            drawString(textToDraw, startX, startY)
//        }
    }

    private fun generateCoverVideoFile(
        cmdPath: String,
        ffprobe: FFprobe,
        hanBrakePath: String,
        bookName: String,
        coverFileName: String,
        coverDuration: Int
    ): File {
        ///Users/lingodeer-yxg/Desktop/视频课/FFmpegUtil/background_img/ebook/YT-story标题页模板/005.png
        val bgImg = "$workingDir/background_img/ebook/YT-story标题页模板/$bookId.png"
        val font = Font("Playfair Display", Font.PLAIN, 105)

        val outputImgFile =
            File("$workingTempDir/output_img/${coverFileName}.png").apply {
                if (!parentFile.exists())
                    parentFile.mkdirs()
            }

        IJ.openImage(bgImg).processor.apply {
            setColor(Color.BLACK)
            setFont(font)
            setAntialiasedText(true)

            drawSentence(font, bookName, 30)

            ImageIO.write(
                this.bufferedImage,
                "png",
                outputImgFile
            )
        }

        val bookCoverImgList = StringBuilder()
        bookCoverImgList.append("file '${outputImgFile.path}'")
        bookCoverImgList.append("\n")
        bookCoverImgList.append("duration $coverDuration")
        bookCoverImgList.append("\n")
        bookCoverImgList.append("file '${outputImgFile.path}'")
        bookCoverImgList.append("\n")
        bookCoverImgList.append("duration $coverDuration")
        bookCoverImgList.append("\n")

        val bookCoverImgListFile =
            File("$workingTempDir/input_txt/inputImgList-$coverFileName.txt").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
                writeText(
                    bookCoverImgList.toString().substring(0, bookCoverImgList.toString().length - 1)
                )
            }


        val bookCoverVideoFile = File("$workingTempDir/output/output-$coverFileName.mp4").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        var cmd = "$cmdPath -y -f " +
                "concat -safe 0 " +
                "-i ${bookCoverImgListFile.path} " +
                //                                "-i ${outputAudioFile.path} " +
                "-r 10 " +
                "-vcodec libx264 " +
                "-pix_fmt yuv420p " +
                bookCoverVideoFile.path
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val bookCoverKeyOutputVideoFile =
            File("$workingTempDir/output/key-output-$coverFileName.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        val bookCoverAudioFile = File("$workingTempDir/gap_time/$coverDuration.mp3").apply {
            if (!this.parentFile.exists())
                this.parentFile.mkdirs()
        }

        ShellUtils.run(
            "$cmdPath -y -f lavfi -i anullsrc=r=48000 -t $coverDuration ${bookCoverAudioFile.path}",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                }

                override fun onError(line: String?) {
                }

            }
        )

        cmd =
            "$cmdPath -y -ss 0 -t ${ffprobe.probe(bookCoverAudioFile.path).format.duration.toInt()} -accurate_seek -i ${bookCoverVideoFile.path} -codec copy -avoid_negative_ts 1 ${bookCoverKeyOutputVideoFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val bookCoverOutputVideoFormattedFile =
            File("$workingTempDir/output/formatted-output-$coverFileName.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        cmd =
            "$cmdPath -y -i ${bookCoverKeyOutputVideoFile.path} -i ${bookCoverAudioFile.path} ${bookCoverOutputVideoFormattedFile.path}"
        println(cmd)
        Runtime.getRuntime().exec(cmd).apply {
            for (readLine in InputStreamReader(this.inputStream).readLines()) {
                println(readLine)
            }
            waitFor()
        }

        val handBrakeOutputVideoFile =
            File("$workingTempDir/output/handbrake-output-$coverFileName.mp4").apply {
                if (!this.parentFile.exists())
                    this.parentFile.mkdirs()
            }

        ShellUtils.run(
            "$hanBrakePath -i ${bookCoverOutputVideoFormattedFile.path} -o ${handBrakeOutputVideoFile.path} -e x264 -q 10 -B 160",
            object : ShellUtils.OnCommandExecOutputListener {
                override fun onSuccess(line: String?) {
                    println(line)
                }

                override fun onError(line: String?) {
                    println(line)
                }
            }
        )
        return handBrakeOutputVideoFile
    }

    private fun compressAudio(cmdPath: String, inputFiles: List<File>) {
        runBlocking {
            for (listFile in inputFiles) {
                launch(Dispatchers.IO) {
                    val outputFile =
                        File("$workingTempDir/compressed_audio/${(listFile.name.split(".")[0])}.mp3").apply {
                            println(this.path)
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

    private fun getContentRect(font: Font, content: String): Rectangle {
        val frc = FontRenderContext(AffineTransform(), true, true)
        return font.getStringBounds(content, frc).bounds
    }

    private fun ImageProcessor.drawIndex(font: Font, index: String) {
        val outlineRect = Rectangle(1540, 112, 270, 70)

        val contentRect = getContentRect(font, index)

        val startX = outlineRect.x + outlineRect.width / 2 - (contentRect.width / 2)
        val startY = outlineRect.y + outlineRect.height / 2 - (contentRect.height / 2) + contentRect.height

        drawString(index, startX, startY)
    }
}