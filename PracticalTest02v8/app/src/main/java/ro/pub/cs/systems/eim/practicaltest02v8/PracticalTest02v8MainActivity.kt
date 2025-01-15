package ro.pub.cs.systems.eim.practicaltest02v8

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class PracticalTest02v8MainActivity : AppCompatActivity() {
    lateinit var priceTextView: TextView
    lateinit var currentEURButton: Button
    lateinit var currentUSDButton: Button
    private val client = OkHttpClient() // OkHttp client instance
    private val buttonClickListener = ButtonClickListener()

    // Cache to store currency rates and timestamps
    private val cache: ConcurrentHashMap<String, Pair<String, Long>> = ConcurrentHashMap()

    private inner class ButtonClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.currentEURButton -> {
                    fetchPriceWithCache("https://api.coindesk.com/v1/bpi/currentprice.json", "EUR")
                }
                R.id.currentUSDButton -> {
                    fetchPriceWithCache("https://api.coindesk.com/v1/bpi/currentprice.json", "USD")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v8_main)

        priceTextView = findViewById<TextView>(R.id.priceTextView)
        currentEURButton = findViewById<Button>(R.id.currentEURButton)
        currentUSDButton = findViewById<Button>(R.id.currentUSDButton)

        currentEURButton.setOnClickListener(buttonClickListener)
        currentUSDButton.setOnClickListener(buttonClickListener)
    }

    private fun fetchPriceWithCache(url: String, currency: String) {
        val currentTime = System.currentTimeMillis()
        val cachedData = cache[currency]

        if (cachedData != null && currentTime - cachedData.second < 60000) {
            // Use cached data if it's less than 60 seconds old
            priceTextView.text = "$currency Price (Cached): ${cachedData.first}"
        } else {
            // Fetch new data if no valid cache exists
            fetchPrice(url, currency)
        }
    }

    private fun fetchPrice(url: String, currency: String) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        priceTextView.text = "Error: ${e.localizedMessage}"
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (response.isSuccessful) {
                            val responseData = response.body?.string()
                            val json = JSONObject(responseData ?: "{}")
                            val rate = json.getJSONObject("bpi").getJSONObject(currency).getString("rate")

                            // Update cache with the new data and timestamp
                            cache[currency] = Pair(rate, System.currentTimeMillis())

                            runOnUiThread {
                                priceTextView.text = "$currency Price: $rate"
                            }
                        } else {
                            runOnUiThread {
                                priceTextView.text = "Error: ${response.message}"
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            priceTextView.text = "Error parsing response: ${e.localizedMessage}"
                        }
                    }
                }
            })
        } catch (e: Exception) {
            runOnUiThread {
                priceTextView.text = "Request Error: ${e.localizedMessage}"
            }
        }
    }
}