@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.read.listener.ReadListener
import data_object.SortedExcelData
import ij.IJ
import ij.process.ImageProcessor
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.io.File
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    ImageJUtil.processImg()
}

object ImageJUtil {


    fun processImg() {
//        val sentence =
//            SortedExcelData("440", "I can't see it anywhere.", "14")
//
//        IJ.openImage("/Users/lingodeer-yxg/Downloads/enpal-bg-1.png").processor.apply {
//            setColor(Color.BLACK)
//            setFont(font)
//            setAntialiasedText(true)
//
//            drawSentence(font, sentence.SENTENCE)
//            ImageIO.write(
//                this.bufferedImage,
//                "png",
//                File("/Users/lingodeer-yxg/Downloads/output_img/${sentence.ID}.png")
//            )
//        }
//
//        return


        EasyExcel.read(
            File(FFmpegImgToVideoUtil.inputAudioSortExcelPath),
            SortedExcelData::class.java,
            object : ReadListener<SortedExcelData> {


                val sentences = ArrayList<SortedExcelData>()


                override fun invoke(data: SortedExcelData, context: AnalysisContext) {
                    sentences.add(data)
                }

                override fun doAfterAllAnalysed(context: AnalysisContext) {


                    EasyExcel.read(
                        File("/Users/lingodeer-yxg/Desktop/FFmpegUtil/301～1077-tans-jp.xlsx"),
                        SortedExcelData::class.java,
                        object : ReadListener<SortedExcelData> {
                            val translateExcelData = ArrayList<SortedExcelData>()


                            override fun invoke(data: SortedExcelData, context: AnalysisContext) {
                                translateExcelData.add(data)
                            }

                            override fun doAfterAllAnalysed(context: AnalysisContext) {
                                sentences.subList(FFmpegImgToVideoUtil.startSortIndex - 1, FFmpegImgToVideoUtil.endSortIndex)
                                    .forEachIndexed { index, sortedExcelData ->
                                        val font = Font("Helvetica", Font.PLAIN, 92)
                                        val indexFont = Font("Arial Rounded MT Bold", Font.PLAIN, 41)
                                        IJ.openImage("/Users/lingodeer-yxg/Downloads/enpal-bg-1.png").processor.apply {
                                            setColor(Color.BLACK)
                                            setFont(font)
                                            setAntialiasedText(true)
                                            drawSentence(font, sortedExcelData.SENTENCE)

                                            setColor(Color.decode("#2F6DAD"))
                                            setFont(indexFont)
                                            setAntialiasedText(true)
                                            drawIndex(indexFont, index + 1)

                                            val outputImgFile =
                                                File("${FFmpegImgToVideoUtil.workingDir}/output_img/${sortedExcelData.ID}-pic1.png").apply {
                                                    if (!parentFile.exists())
                                                        parentFile.mkdirs()
                                                }

                                            ImageIO.write(
                                                this.bufferedImage,
                                                "png",
                                                outputImgFile
                                            )
                                        }

                                        IJ.openImage("/Users/lingodeer-yxg/Downloads/enpal-bg-1.png").processor.apply {
                                            setColor(Color.BLACK)
                                            setFont(font)
                                            setAntialiasedText(true)
                                            drawSentence(font, sortedExcelData.SENTENCE)

                                            setColor(Color.decode("#2F6DAD"))
                                            setFont(indexFont)
                                            setAntialiasedText(true)
                                            drawIndex(indexFont, index + 1)

                                            translateExcelData.find {
                                                it.ID == sortedExcelData.ID
                                            }?.apply {
                                                drawTranslate(COUNT.trim())
                                            }

                                            val outputImgFile =
                                                File("${FFmpegImgToVideoUtil.workingDir}/output_img/${sortedExcelData.ID}-pic2.png").apply {
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

                            }

                        }).sheet("777 WITH Japanese").doRead()


                }

            }).sheet("sorted-list").doRead()


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