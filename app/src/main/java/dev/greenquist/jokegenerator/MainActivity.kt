package dev.greenquist.jokegenerator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface JokeApi {

    @GET("api")
    suspend fun fetchJoke(): Response<String>

}

class MainActivity : AppCompatActivity() {

    private val baseUrl = "https://geek-jokes.sameerkumar.website/"
    private var joke : String? = null
    private var job: Job? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception: ${throwable.localizedMessage}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loading_view.visibility = View.INVISIBLE



        new_joke_button.setOnClickListener {

            loading_view.visibility = View.VISIBLE
            joke_text.text = ""

            job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val response = getJokeService().fetchJoke()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        joke = response.body()

                        if (joke.isNullOrEmpty()) {
                            onError("Error: could not load joke")
                        } else {
                            joke_text.text = joke
                        }

                        loading_view.visibility = View.INVISIBLE
                    } else {
                        onError("Error: ${response.message()}")
                    }
                }
            }
        }
    }


    private fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        //loading_view.visibility = View.INVISIBLE
    }

    private fun getJokeService(): JokeApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(JokeApi::class.java)
    }
}

