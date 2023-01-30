package fi.tuni.pepper_svl

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity

class SananlaskuActivity : RobotActivity(), RobotLifecycleCallbacks {
    var sananlaskut = arrayOf("Parempi katsoa kuin katua",
        "Joka kuuseen kurkottaa, se katajaan kapsahtaa",
        "Ei lämmin luita riko")
    var oikeaVastaus = ""
    var count = 0
    var rightAnswerCount = 0
    private lateinit var sanaView: TextView
    private lateinit var input: EditText
    private lateinit var checkAnswerBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        sanaView = findViewById(R.id.sana)
        input = findViewById(R.id.input)
        input.setOnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.i("test", "toimii")
                checkAnswer(input.text.toString())
            }
            return@setOnEditorActionListener false
        }
        input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { hideKeyboard(v) }
        }
        checkAnswerBtn = findViewById(R.id.check_answer)
        checkAnswerBtn.isEnabled = true
        startGame()
        checkAnswerBtn.setOnClickListener {
            if(input.text.isNotEmpty()) {
                checkAnswer(input.text.toString())
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
    override fun onRobotFocusGained(qiContext: QiContext?) {
        val say: Say = SayBuilder.with(qiContext)
            .withText("Täällä voit pelata kanssani sananlaskupeliä. Aloitetaanko uusi peli?")
            .build()
        say.run()
        if(count <= 10) {
            startGame()
        }
        checkAnswerBtn.setOnClickListener {
            if(input.text.isNotEmpty()) {
                checkAnswer(input.text.toString())
            }
        }
    }

    override fun onRobotFocusLost() {
        TODO("Not yet implemented")
    }

    override fun onRobotFocusRefused(reason: String?) {
        TODO("Not yet implemented")
    }
    override fun onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
    fun startGame() {
        input.setText("")
        val lause = sananlaskut.random()
        val sanat = lause.split(" ").toTypedArray()
        val rng = (sanat.indices).random()
        var sana = sanat[rng]
        oikeaVastaus = sana
        sana = sana.replace("[a-zA-ZäöåÄÖÅ]".toRegex(), "_")
        sanat[rng] = sana
        runOnUiThread {
            sanaView.text = sanat.joinToString(separator = " ")
        }
    }
    private fun checkAnswer(text: String) {
        if (text.lowercase().trim() == oikeaVastaus.lowercase().trim()) {
            //val say = SayBuilder.with(qiContext)
                //.withText("Jee oikein meni!!")
                //.build()
            //say.run()
            Log.i("vastaus", "oikein")
            startGame()
            rightAnswerCount++
        } else {
            //val say = SayBuilder.with(qiContext)
                //.withText("Tämä meni väärin, oikea vastaus oli: $oikeaVastaus")
                //.build()
            //say.run()
            Log.i("vastaus", "väärin")
            startGame()
        }
        count++
    }

}