package fi.tuni.pepper_svl

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
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import kotlin.concurrent.thread


class SananlaskuActivity : RobotActivity(), RobotLifecycleCallbacks {
    private var sananlaskut = Sananlaskut().getSananlaskut()
    private var rightAnswer = ""
    private var count = 0
    private var rightAnswerCount = 0
    private lateinit var wordView: TextView
    private var btnList = mutableListOf<Button>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sananlasku)
        QiSDK.register(this, this)
        wordView = findViewById(R.id.word)
        btnList.add(findViewById(R.id.answer1))
        btnList.add(findViewById(R.id.answer2))
        btnList.add(findViewById(R.id.answer3))
        startGame()
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        val say: Say = SayBuilder.with(qiContext)
            .withText("Nyt voit pelata kanssani sananlaskupeliä. Voit sanoa minulle" +
                    "puuttuvan sanan tai painaa näytöllä näkyvistä vaihtoehdoista.")
            .build()
        say.run()
        startListen(qiContext)
        startGame()
        btnList.forEach {btn -> btn.setOnClickListener {
            checkAnswer(btn.text.toString(), qiContext)
        }}
    }

    override fun onRobotFocusLost() {
    }

    override fun onRobotFocusRefused(reason: String?) {
    }
    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    private fun startListen(qiContext: QiContext?) {
        val phraseSet = PhraseSetBuilder.with(qiContext)
            .withTexts(*Sananlaskut().getWords())
            .build()
        val listen: Listen = ListenBuilder.with(qiContext)
            .withPhraseSet(phraseSet)
            .build()
        val listenResult : ListenResult = listen.run()
        checkAnswer(listenResult.heardPhrase.text, qiContext)
    }

    private fun getRandomWord() :String {
        var phrases = mutableListOf(*Sananlaskut().getWords())
        phrases = phrases.filter { phrase -> phrase != rightAnswer }.toMutableList()
        return phrases.random().toString()
    }

    private fun getCurrentPhrase(): String {
        val sentence = sananlaskut.random()
        sananlaskut.remove(sentence)
        Log.i("test", sananlaskut.indices.toString())
        val wordArray = sentence.split(" ").toTypedArray()
        val index = wordArray.indices.random()
        var word = wordArray[index]
        rightAnswer = word.replace(",", "")
        word = word.replace("[a-zA-ZäöåÄÖÅ]".toRegex(), "_")
        wordArray[index] = word
        return wordArray.joinToString(separator = " ")
    }

    private fun startGame() {
        val buttonOrder = (0..2).shuffled()
        if(count < 10) {
            val phrase = getCurrentPhrase()
            runOnUiThread {
                wordView.text = phrase
                btnList.forEachIndexed {index, btn ->
                    if(index == buttonOrder[1]) {
                        btnList[index].text = rightAnswer
                    } else {
                        btnList[index].text = getRandomWord()
                    }
                    btn.isEnabled = true
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                    /*Only for testing on an android device
                    btn.setOnClickListener {
                        checkAnswerTest(btn.text.toString())
                    }*/
                }
            }
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

    /* Only for testing on an android device when Pepper is unavailable
    fun checkAnswerTest(text: String) {
        @SuppressLint("ResourceAsColor")
            if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
                thread {
                    Log.i("test", "oikein")
                    updateButtons()
                    rightAnswerCount++
                    count++
                    Timer().schedule(timerTask {
                        startGame()
                    }, 2000)
                }
            } else {
                thread {
                    Log.i("test", "väärin")
                    updateButtons()
                    count++
                    Timer().schedule(timerTask {
                        startGame()
                    }, 2000)
                }
            }
    }*/

    private fun endGameDialog() {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Peli loppui")
            alertDialog.setMessage("Sait $rightAnswerCount/10 oikein!\nHaluatko pelata uudelleen?")
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("Uusi peli") { dialog, whichButton ->
                count = 1
                rightAnswerCount = 0
                sananlaskut = Sananlaskut().getSananlaskut()
                startGame()
            }
            alertDialog.setNegativeButton("Palaa valikkoon",
            ) { dialog, whichButton ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            alertDialog.show()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun checkAnswer(text: String, qiContext: QiContext?) {
        if (text.lowercase().trim() == rightAnswer.lowercase().trim()) {
            thread {
                updateButtons()
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
                updateButtons()
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