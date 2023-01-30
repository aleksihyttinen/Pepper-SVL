package fi.tuni.pepper_svl

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var sanaView: TextView
    private lateinit var input: EditText
    private lateinit var checkAnswer: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        sanaView = findViewById(R.id.sana)
        input = findViewById(R.id.input)
        checkAnswer = findViewById(R.id.check_answer)
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        val say: Say = SayBuilder.with(qiContext)
            .withText("Täällä voit pelata kanssani sananlaskupeliä. Aloitetaanko uusi peli?")
            .build()
        say.run()
        startGame()
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
    fun checkAnswer() {
        //todo
    }

}