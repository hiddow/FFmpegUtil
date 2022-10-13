package data_object

data class EPBookPhrase(
    var phrase_id: String = "",
    var phrase_list: String = "",
    var phrase_txt: String = "",
    var phrase_txt_lemma: String = "",
    var pos: String = "",
    var trans: EPBookTrans = EPBookTrans(),
)
