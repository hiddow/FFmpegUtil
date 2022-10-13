package data_object

data class EPParagraph(
    var paragraph_id: String = "",
    var paragraph_txt: String = "",
    var sentences_array: List<EPBookSentence> = ArrayList(),
)
