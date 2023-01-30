package fi.tuni.pepper_svl

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
    private var sananlaskut = arrayOf(
        "Parempi katsoa kuin katua",
        "Joka kuuseen kurkottaa, se katajaan kapsahtaa",
        "Ei lämmin luita riko",
        "Ei oppi ojaan kaada, eikä tieto tieltä työnnä",
        "Ei omena kauas puusta putoa",
        "Ei ole koiraa karvoihin katsominen",
        "Ei Roomaakaan päivässä rakennettu",
        "Eteenpäin, sanoi mummo lumessa",
        "Hyvin suunniteltu on puoliksi tehty",
        "Hyvä antaa vähästään, paha ei paljostaankaan",
        "Hädässä ystävä tunnetaan",
        "Jokainen on oman onnensa seppä",
        "Kaksi kärpästä yhdellä iskulla",
        "Kertaus on opintojen äiti",
        "Kuin apteekin hyllyltä",
        "Kyllä routa porsaan kotiin ajaa",
        "Lapsen suusta kuulee totuuden",
        "Loppui lyhyeen kuin kanan lento",
        "Luulo ei ole tiedon väärti",
        "Minkä nuorena oppii, sen vanhana taitaa",
        "Mitä useampi kokki, sitä huonompi soppa",
        "Niin metsä vastaa kuin sinne huudetaan",
        "Ojasta allikkoon",
        "On taottava silloin, kun rauta on kuuma",
        "Paistaa se päivä risukasaankin",
        "Ruoho on vihreämpää aidan toisella puolen",
        "Suutarin lapsilla ei ole kenkiä",
        "Tie miehen sydämeen käy vatsan kautta",
        "Uusi lumi on vanhan surma",
        "Vesi vanhin voitehista",
        "Vierivä kivi ei sammaloidu",
        "Älä laita kaikkia munia samaan koriin",
        "Alku aina hankalaa, lopussa kiitos seisoo",
        "Ei auta itku markkinoilla",
        "Ei haukku haavaa tee",
        "Hätä ei lue lakia",
        "Ilta on aamua viisaampi",
        "Joka toiselle kuoppaa kaivaa, se itse siihen lankeaa",
        "Kateus vie kalatkin vedestä",
        "Kolmas kerta toden sanoo",
        "Kyllä sokeakin kana joskus jyvän löytää",
        "Nauru pidentää ikää",
        "Oma maa mansikka, muu maa mustikka",
        "Parempi katsoa kuin katua",
        "Parempi pyy pivossa kuin kymmenen oksalla",
        "Pilkka sattuu omaan nilkkaan",
        "Puhtaus on puoli ruokaa",
        "Se koira älähtää, johon kalikka kalahtaa",
        "Se parhaiten nauraa, joka viimeksi nauraa",
        "Sopu sijaa antaa",
        "Vahinko ei tule kello kaulassa"
        )
    var rightAnswer = ""
    var count = 1
    var rightAnswerCount = 0
    var usedPhrases = mutableListOf<Int>()
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
        startGame()
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
    fun rng() : Int {
        var number = sananlaskut.indices.random()
        while (usedPhrases.contains(number)) {
            number = sananlaskut.indices.random()
        }
        usedPhrases.add(number)
        return number
    }
    private fun startGame() {
        Log.i("test", "$count")
        if(count < 10) {
            input.setText("")
            val lause = sananlaskut.random()
            val sanat = lause.split(" ").toTypedArray()
            var index = rng()
            var sana = sanat[index]
            rightAnswer = sana
            sana = sana.replace("[a-zA-ZäöåÄÖÅ]".toRegex(), "_")
            sanat[index] = sana
            runOnUiThread {
                sanaView.text = sanat.joinToString(separator = " ")
            }
        } else {
            endGame()
        }
    }
    private fun endGame() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Peli loppui")
        alertDialog.setMessage("Sait $rightAnswerCount/10 oikein!\nHaluatko pelata uudelleen?")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton("Uusi peli") { dialog, whichButton ->
            count = 1
            rightAnswerCount = 0
            startGame()
        }

        alertDialog.setNegativeButton("Palaa valikkoon",
        ) { dialog, whichButton ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }
    private fun checkAnswer(text: String) {
        if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
            //val say = SayBuilder.with(qiContext)
                //.withText("Jee oikein meni!!")
                //.build()
            //say.run()
            Log.i("test", "oikein")
            rightAnswerCount++
            count++
            startGame()
        } else {
            //val say = SayBuilder.with(qiContext)
                //.withText("Tämä meni väärin, oikea vastaus oli: $oikeaVastaus")
                //.build()
            //say.run()
            Log.i("test", "väärin")
            count++
            startGame()
        }
    }

}