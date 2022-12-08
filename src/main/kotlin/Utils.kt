import java.io.File

object Utils {

    const val parentDir = "/Users/lingodeer-yxg/Desktop/视频课"

    fun <T> averageAssignFixLength(source: List<T>?, splitItemNum: Int): List<List<T>> {
        val result = ArrayList<List<T>>()

        if (source != null && source.run { isNotEmpty() } && splitItemNum > 0) {
            if (source.size <= splitItemNum) {
                // 源List元素数量小于等于目标分组数量
                result.add(source)
            } else {
                // 计算拆分后list数量
                val splitNum =
                    if (source.size % splitItemNum == 0) source.size / splitItemNum else source.size / splitItemNum + 1

                var value: List<T>? = null
                for (i in 0 until splitNum) {
                    value = if (i < splitNum - 1) {
                        source.subList(i * splitItemNum, (i + 1) * splitItemNum)
                    } else {
                        // 最后一组
                        source.subList(i * splitItemNum, source.size)
                    }

                    result.add(value)
                }
            }
        }

        return result
    }

    fun emptyFileDir(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) {
            return false
        } else {
            if (file.isFile) {
                return false
            }
            if (file.isDirectory) {
                val childFile = file.listFiles()
                if (childFile == null || childFile.isEmpty()) {
                    return false
                }
                for (f in childFile) {
                    if (f.isFile) {
                        f.delete()
                    } else if (f.isDirectory) {
                        emptyFileDir(f.absolutePath)
                    }
                }
                return true
            } else {
                return false
            }
        }
    }

    fun isPunch(str: String): Boolean {
        return "[\\p{P}+~$`^=|<>～｀＄＾＋＝｜＜＞￥×]".toRegex().matches(str) || arrayOf("...").contains(str)
    }

}