package fi.tuni.pepper_svl.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.Qi
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Listen
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.`object`.conversation.BodyLanguageOption
import fi.tuni.pepper_svl.R
import fi.tuni.pepper_svl.data.Sananlaskut
import kotlin.concurrent.thread


class SananlaskuActivity : RobotActivity(), RobotLifecycleCallbacks {
    private var alertDialog: AlertDialog? = null
    private var sananlaskut = Sananlaskut().getSananlaskut()
    private var rightAnswer = ""
    private var count = 1
    private var qiContext : QiContext? = null
    private var rightAnswerCount = 0
    private lateinit var wordView: TextView
    private var btnList = mutableListOf<Button>()
    private var listenFuture: Future<ListenResult>? = null
    private lateinit var menuButton: Button
    private lateinit var endGameButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        wordView = findViewById(R.id.word)
        btnList.add(findViewById(R.id.answer1))
        btnList.add(findViewById(R.id.answer2))
        btnList.add(findViewById(R.id.answer3))
        menuButton = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        endGameButton = findViewById(R.id.end_game)
        endGameButton.setOnClickListener {
            stopListen()
            endGameDialog()
        }
    }

    override fun onRobotFocusGained(_qiContext: QiContext?) {
        qiContext = _qiContext
        val animation = AnimationBuilder.with(qiContext).withResources(R.raw.show_tablet).build()
        val animate = AnimateBuilder.with(qiContext).withAnimation(animation).build()
        animate.async().run()
        val say: Say = SayBuilder.with(qiContext)
            .withText("Nyt voit pelata kanssani sananlaskupeliä. Voit sanoa minulle" +
                    "puuttuvan sanan tai painaa näytöllä näkyvistä vaihtoehdoista. Näytän sinulle 10 sananlaskua ja lopuksi kerron montako meni oikein.")
            .build()
        say.run()
        runOnUiThread{
            endGameButton.isEnabled = true
        }
        startGame()
    }

    override fun onRobotFocusLost() {
    }

    override fun onRobotFocusRefused(reason: String?) {
    }
    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    private fun startListen() {
        thread {
            val phraseSet = PhraseSetBuilder.with(qiContext)
                .withTexts(*Sananlaskut().getWords(), "uusi peli", "takaisin päävalikkoon", "lopeta peli", "lopeta kuuntelu")
                .build()
            val listen: Listen = ListenBuilder.with(qiContext)
                .withBodyLanguageOption(BodyLanguageOption.DISABLED)
                .withPhraseSet(phraseSet)
                .build()
            listenFuture = listen.async().run()
            listenFuture?.thenConsume { future ->
                if (future.isSuccess) {
                    val text = listenFuture?.get()?.heardPhrase!!.text
                    if (text.lowercase() == "takaisin päävalikkoon") {
                        runOnUiThread {
                            alertDialog!!.dismiss()
                            menuButton.performClick()
                        }
                    } else if (text.lowercase() == "lopeta kuuntelu") {
                        stopListen()
                    } else if (text.lowercase() == "uusi peli") {
                        runOnUiThread {
                            alertDialog!!.dismiss()
                        }
                        count = 1
                        rightAnswerCount = 0
                        sananlaskut = Sananlaskut().getSananlaskut()
                        startGame()
                    } else if (text.lowercase() == "lopeta peli")  {
                        endGameDialog()
                    } else {
                        checkAnswer(text)
                    }
                } else if (future.hasError()) {
                    Log.e("test", "Error", future.error)
                }
            }
        }
    }
    private fun stopListen() {
        listenFuture?.requestCancellation()
        listenFuture = null
    }

    private fun getRandomWord() :String {
        var phrases = mutableListOf(*Sananlaskut().getWords())
        phrases = phrases.filter { phrase -> phrase != rightAnswer }.toMutableList()
        return phrases.random().toString()
    }

    private fun getCurrentPhrase(): String {
        val sentence = sananlaskut.random()
        sananlaskut.remove(sentence)
        val wordArray = sentence.split(" ").toTypedArray()
        val index = wordArray.indices.random()
        var word = wordArray[index]
        rightAnswer = word.replace(",", "")
        word = word.replace("[a-zA-ZäöåÄÖÅ]".toRegex(), "_")
        wordArray[index] = word
        return wordArray.joinToString(separator = " ")
    }

    private fun startGame() {
        val buttonOrder = (0..2).shuffled() //Randomize numbers 0, 1 and 2 and add them to a list
        if(count < 11) {
            val phrase = getCurrentPhrase()
            runOnUiThread {

                wordView.text = phrase
                btnList.forEachIndexed {index, btn ->
                    if(index == buttonOrder[1]) {
                        btnList[index].text = rightAnswer //Set correct answer to the index which matches second item in buttonOrder
                    } else {
                        btnList[index].text = getRandomWord() //Set random words on others
                    }

                    btn.isEnabled = true
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    btn.setOnClickListener {
                        stopListen()
                        checkAnswer(btn.text.toString())
                    }
                }
            }
            startListen()
        } else {
            endGameDialog()
        }
    }

    private fun updateButtons() {
        runOnUiThread {
            btnList.forEach { btn ->
                btn.isEnabled = false
                if(btn.text.equals(rightAnswer)) {
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                }
            }
        }
    }

    private fun endGameDialog() {
        runOnUiThread {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Peli loppui")
            dialogBuilder.setMessage("Sait $rightAnswerCount/${count -1} oikein!\nHaluatko pelata uudelleen?")
            dialogBuilder.setCancelable(false)
            dialogBuilder.setPositiveButton("Uusi peli") { dialog, whichButton ->
                count = 1
                rightAnswerCount = 0
                sananlaskut = Sananlaskut().getSananlaskut()
                startGame()
            }
            dialogBuilder.setNegativeButton(
                "Takaisin päävalikkoon",
            ) { dialog, whichButton ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            alertDialog = dialogBuilder.create()
            alertDialog!!.show()

        }
        thread {
            val say = SayBuilder.with(qiContext)
                .withText("Peli loppui, sait $rightAnswerCount oikein.")
                .build()
            say.run()
            startListen()
        }
    }
    val goodPhrases = arrayOf("Jee oikein meni", "Hienoa tämä meni oikein", "Mahtavaa tämä oli oikein")
    val badPhrases = arrayOf("Höh tämä meni väärin", "Tämä ei mennyt oikein", "Ensi kerralla paremmin")
    @SuppressLint("ResourceAsColor")
    private fun checkAnswer(text: String) {
        updateButtons()
        if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
            thread {
                val animation = AnimationBuilder.with(qiContext).withResources(R.raw.nice_reaction).build()
                val animate = AnimateBuilder.with(qiContext).withAnimation(animation).build()
                animate.async().run()
                val say = SayBuilder.with(qiContext)
                    .withText(goodPhrases.random())
                    .build()
                say.run()
                rightAnswerCount++
                count++
                startGame()
            }
        } else {
            thread {
                val animation = AnimationBuilder.with(qiContext).withResources(R.raw.sad_reaction).build()
                val animate = AnimateBuilder.with(qiContext).withAnimation(animation).build()
                animate.async().run()
                val say = SayBuilder.with(qiContext)
                    .withText(badPhrases.random() + "Oikea vastaus oli: $rightAnswer")
                    .build()
                say.run()
                count++
                startGame()
            }
        }
    }

}