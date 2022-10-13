package data_object

data class EPBookSentence(
    var sentence_id: String = "",

    var sent_txt: String = "",

    var sent_regex: String = "",

    var trans: EPBookTrans = EPBookTrans(),

    var audio_time_1: String = "",

    var audio_time_2: String = "",

    var start_index: String = "",

    var end_index: String = "",

    var confirm: Boolean = false,

    var words_array: List<EPBookWord> = ArrayList(),

    var phrases_array: List<EPBookPhrase> = ArrayList(),
)