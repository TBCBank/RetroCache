package ge.tbcbank.retrocache

import com.google.gson.annotations.SerializedName

class BookPageDto(
    @SerializedName("total")
    val total: String,
    @SerializedName("page")
    val page: String,
    @SerializedName("books")
    val books: List<BookDto>
)

class BookDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("subtitle")
    val subtitle: String,
    @SerializedName("isbn13")
    val isbn: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("image")
    val imageUrl: String,
    @SerializedName("url")
    val bookUrl: String
)