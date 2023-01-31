package fi.tuni.pepper_svl

import android.app.Activity
import android.app.AlertDialog
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
import com.aldebaran.qi.sdk.`object`.conversation.Listen
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.`object`.conversation.PhraseSet
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlin.concurrent.thread


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
    private lateinit var sanaView: TextView
    private lateinit var input: EditText
    private lateinit var checkAnswerBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        sanaView = findViewById(R.id.sana)
        input = findViewById(R.id.input)
        input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { hideKeyboard(v) }
        }
        checkAnswerBtn = findViewById(R.id.check_answer)
        checkAnswerBtn.isEnabled = true
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager? = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun startListen(qiContext: QiContext?) {
        val phraseSet: PhraseSet = PhraseSetBuilder.with(qiContext)
            .withTexts(    "Alku","Ei","Eteenpäin","Hyvin","Hyvä","Hädässä","Hätä","Ilta","Joka","Jokainen","Kaksi","Kateus","Kertaus","Kolmas","Kuin","Kyllä","Lapsen","Loppui","Luulo","Minkä","Mitä","Nauru","Niin","Ojasta","Oma","On","Paistaa","Parempi","Pilkka","Puhtaus","Ruoho","Se","Sopu","Suutarin","Tie","Uusi","Vahinko","Vesi","Vierivä","Älä","Roomaakaan","aamua","aidan","aina","ajaa","allikkoon","antaa","antaa","apteekin","auta","ei","eikä","haavaa","hankalaa","haukku","huonompi","huudetaan","hyllyltä","ikää","iskulla","itku","itse","johon","joka","joskus","jyvän","kaada","kaikkia","kaivaa","kalahtaa","kalatkin","kalikka","kana","kanan","kapsahtaa","karvoihin","katajaan","katsoa","katsominen","katua","kauas","kaulassa","kautta","kello","kenkiä","kerta","kiitos","kivi","koira","koiraa","kokki","koriin","kotiin","kuin","kun","kuoppaa","kurkottaa","kuulee","kuuma","kuuseen","kymmenen","kärpästä","käy","laita","lakia","lankeaa","lapsilla","lento","lopussa","lue","luita","lumessa","lumi","lyhyeen","lämmin","löytää","maa","mansikka","markkinoilla","metsä","miehen","mummo","munia","mustikka","muu","nauraa","nauraa","nilkkaan","nuorena","ojaan","oksalla","ole","omaan","oman","omena","on","onnensa","opintojen","oppi","oppii","paha","paljostaankaan","parhaiten","pidentää","pivossa","porsaan","puolen","puoli","puoliksi","putoa","puusta","pyy","päivä","päivässä","rakennettu","rauta","riko","risukasaankin","routa","ruokaa","samaan","sammaloidu","sanoi","sanoo","sattuu","se","seisoo","sen","seppä","siihen","sijaa","silloin","sinne","sitä","sokeakin","soppa","surma","suunniteltu","suusta","sydämeen","taitaa","taottava","tee","tehty","tiedon","tieltä","tieto","toden","toisella","toiselle","totuuden","tule","tunnetaan","työnnä","useampi","vanhan","vanhana","vanhin","vastaa","vatsan","vedestä","vie","vihreämpää","viimeksi","viisaampi","voitehista","vähästään","väärti","yhdellä","ystävä","äiti","älähtää"
        )
            .build()

        val listen: Listen = ListenBuilder.with(qiContext)
            .withPhraseSet(phraseSet)
            .build()

        val listenResult : ListenResult = listen.run()
        checkAnswer(listenResult.heardPhrase.text, qiContext)

    }
    override fun onRobotFocusGained(qiContext: QiContext?) {
        val say: Say = SayBuilder.with(qiContext)
            .withText("Täällä voit pelata kanssani sananlaskupeliä. Aloitetaanko uusi peli?")
            .build()
        say.run()
        startGame()
        checkAnswerBtn.setOnClickListener {
            if(input.text.isNotEmpty()) {
                checkAnswer(input.text.toString(), qiContext)
            }
        }
        input.setOnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Log.i("test", "toimii")
                checkAnswer(input.text.toString(), qiContext)
            }
            return@setOnEditorActionListener false
        }
        startListen(qiContext)
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

    private fun startGame() {
        Log.i("test", "$count")
        if(count < 10) {
            runOnUiThread {input.setText("")}
            val lause = sananlaskut.random()
            val sanat = lause.split(" ").toTypedArray()
            val index = sanat.indices.random()
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
        runOnUiThread{
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

        alertDialog.show()}
    }
    private fun checkAnswer(text: String, qiContext: QiContext?) {
        if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
            thread {
                val say = SayBuilder.with(qiContext)
                    .withText("Jee oikein meni!!")
                    .build()
                say.run()
                startListen(qiContext)
                Log.i("test", "oikein")
                rightAnswerCount++
                count++
                startGame()
            }
        } else {
            thread {
                val say = SayBuilder.with(qiContext)
                    .withText("Tämä meni väärin, oikea vastaus oli: $rightAnswer")
                    .build()
                say.run()
                startListen(qiContext)
                Log.i("test", "väärin")
                count++
                startGame()
            }
        }
    }

}