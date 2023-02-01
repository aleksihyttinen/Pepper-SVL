package fi.tuni.pepper_svl

import fi.tuni.pepper_svl.Sananlaskut
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.thread
import kotlin.concurrent.timerTask


class SananlaskuActivity : RobotActivity(), RobotLifecycleCallbacks {
    private lateinit var phraseSet: PhraseSet
    private var sananlaskut = Sananlaskut().getPhrases()
    var rightAnswer = ""
    var count = 1
    var rightAnswerCount = 0
    private lateinit var sanaView: TextView
    private var btnList = mutableListOf<Button>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        sanaView = findViewById(R.id.sana)
        btnList.add(findViewById(R.id.answer1))
        btnList.add(findViewById(R.id.answer2))
        btnList.add(findViewById(R.id.answer3))
        startGame()
    }

    private fun startListen(qiContext: QiContext?) {
        phraseSet = PhraseSetBuilder.with(qiContext)
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
        btnList.forEach {btn -> btn.setOnClickListener {
            checkAnswer(btn.text.toString(), qiContext)
        }}
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
    private fun getRandomWord() :String {
        var phrases = mutableListOf<String>("Alku","Ei","Eteenpäin","Hyvin","Hyvä","Hädässä","Hätä","Ilta","Joka","Jokainen","Kaksi","Kateus","Kertaus","Kolmas","Kuin","Kyllä","Lapsen","Loppui","Luulo","Minkä","Mitä","Nauru","Niin","Ojasta","Oma","On","Paistaa","Parempi","Pilkka","Puhtaus","Ruoho","Se","Sopu","Suutarin","Tie","Uusi","Vahinko","Vesi","Vierivä","Älä","Roomaakaan","aamua","aidan","aina","ajaa","allikkoon","antaa","antaa","apteekin","auta","ei","eikä","haavaa","hankalaa","haukku","huonompi","huudetaan","hyllyltä","ikää","iskulla","itku","itse","johon","joka","joskus","jyvän","kaada","kaikkia","kaivaa","kalahtaa","kalatkin","kalikka","kana","kanan","kapsahtaa","karvoihin","katajaan","katsoa","katsominen","katua","kauas","kaulassa","kautta","kello","kenkiä","kerta","kiitos","kivi","koira","koiraa","kokki","koriin","kotiin","kuin","kun","kuoppaa","kurkottaa","kuulee","kuuma","kuuseen","kymmenen","kärpästä","käy","laita","lakia","lankeaa","lapsilla","lento","lopussa","lue","luita","lumessa","lumi","lyhyeen","lämmin","löytää","maa","mansikka","markkinoilla","metsä","miehen","mummo","munia","mustikka","muu","nauraa","nauraa","nilkkaan","nuorena","ojaan","oksalla","ole","omaan","oman","omena","on","onnensa","opintojen","oppi","oppii","paha","paljostaankaan","parhaiten","pidentää","pivossa","porsaan","puolen","puoli","puoliksi","putoa","puusta","pyy","päivä","päivässä","rakennettu","rauta","riko","risukasaankin","routa","ruokaa","samaan","sammaloidu","sanoi","sanoo","sattuu","se","seisoo","sen","seppä","siihen","sijaa","silloin","sinne","sitä","sokeakin","soppa","surma","suunniteltu","suusta","sydämeen","taitaa","taottava","tee","tehty","tiedon","tieltä","tieto","toden","toisella","toiselle","totuuden","tule","tunnetaan","työnnä","useampi","vanhan","vanhana","vanhin","vastaa","vatsan","vedestä","vie","vihreämpää","viimeksi","viisaampi","voitehista","vähästään","väärti","yhdellä","ystävä","äiti","älähtää")
        phrases = phrases.filter { phrase -> !phrase.equals(rightAnswer) }.toMutableList()
        return phrases.random().toString()
    }
    private fun startGame() {
        Log.i("test", "$count")
        val order = (0..2).shuffled()
        if(count <= 10) {
            val lause = sananlaskut.random()
            val sanat = lause.split(" ").toTypedArray()
            val index = sanat.indices.random()
            var sana = sanat[index]
            rightAnswer = sana
            sana = sana.replace("[a-zA-ZäöåÄÖÅ]".toRegex(), "_")
            sanat[index] = sana
            runOnUiThread {
                sanaView.text = sanat.joinToString(separator = " ")

                order.forEach{ index ->
                    if(index == order[1]) {
                        Log.i("test", rightAnswer)
                        btnList[index].text = rightAnswer
                    } else {
                        btnList[index].text = getRandomWord()
                    }
                }
                btnList.forEach {btn -> btn.setOnClickListener {
                    checkAnswerTest(btn.text.toString())
                }
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                }
            }
        } else {
            endGame()
        }
    }
    fun checkAnswerTest(text: String) {
        @SuppressLint("ResourceAsColor")
            if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
                runOnUiThread {
                    btnList.forEach { btn ->
                        Log.i("test", btn.text.toString())
                        Log.i("test", btn.text.equals(rightAnswer).toString())
                        if(btn.text.equals(rightAnswer)) {
                            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                        } else {
                            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                        }
                    }
                }
                thread {
                    Log.i("test", "oikein")
                    rightAnswerCount++
                    count++

                    Timer().schedule(timerTask {
                        startGame()
                    }, 2000)
                }
            } else {
                thread {
                    Log.i("test", "väärin")
                    count++
                    Timer().schedule(timerTask {
                        startGame()
                    }, 2000)
                }
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
    @SuppressLint("ResourceAsColor")
    private fun checkAnswer(text: String, qiContext: QiContext?) {
        if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
            runOnUiThread {
                btnList.forEach { btn ->
                    if(btn.text.equals(rightAnswer)) {
                        btn.setBackgroundColor(R.color.green)
                    } else {
                        btn.setBackgroundColor(R.color.red)
                    }
                }
            }
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