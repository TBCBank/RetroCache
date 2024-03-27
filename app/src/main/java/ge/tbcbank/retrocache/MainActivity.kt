package ge.tbcbank.retrocache

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import ge.tbcbank.retrocache.di.CoreModule
import ge.tbcbank.retrocache.theme.RetroCacheTheme

class MainActivity : ComponentActivity() {

    private lateinit var api: TestApi

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = CoreModule.api

        setContent {
            val coroutineScope = rememberCoroutineScope()
            var books = viewModel.books.collectAsState(BookPageDto("1", "1", listOf()))

            RetroCacheTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(content = {
                            books.value.books.forEach {
                                item {
                                    Text(text = it.title)
                                }
                            }
                        })
                        Button(onClick = {
                            viewModel.fetchData()
                        }) {
                            Text(text = "Fetch data")
                        }


                        Button(onClick = {
                            viewModel.cacheFor10Sec()
                        }) {
                            Text(text = "Cache for 10 sec")
                        }

                        Button(onClick = {
                            viewModel.removeFromCache()
                        }) {
                            Text(text = "Remove from cache")
                        }

                        Button(onClick = {
                            viewModel.refresh()
                        }) {
                            Text(text = "Refresh")
                        }
                    }

                }
            }
        }
    }
}