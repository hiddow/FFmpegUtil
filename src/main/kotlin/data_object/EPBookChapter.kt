package data_object

data class EPBookChapter(
    var chapter_id: String = "",
    var part_name: String = "",
    var chapter_name: String = "",
    var paragraphs_array: List<EPParagraph> = ArrayList()
)
