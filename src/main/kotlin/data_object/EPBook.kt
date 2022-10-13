package data_object


data class EPBook(
    var ebook_id: String = "",
    var ebook_name: String = "",
    var ebook_author: String = "",
    var ebook_pubdate: String = "",
    var ebook_intro: String = "",
    var icon: String = "",
    var tag: String = "",
    var difficulty: Int = 1,
    var sort_index: Int = -1,
    var ebook_name_trans: EPBookTrans = EPBookTrans(),
    var ebook_author_trans: EPBookTrans = EPBookTrans(),
    var chapters_array: List<EPBookChapter> = ArrayList(),
)
