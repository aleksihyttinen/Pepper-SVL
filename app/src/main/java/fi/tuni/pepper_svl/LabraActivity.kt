package fi.tuni.pepper_svl

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.conversation.Topic
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.builder.ChatBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.dx.followme.FollowHuman

class LabraActivity : RobotActivity(), RobotLifecycleCallbacks {
    private lateinit var chat: Chat
    private lateinit var text : TextView
    private lateinit var qiContext: QiContext
    var followHuman: FollowHuman? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labra)
        QiSDK.register(this, this)
        text = findViewById(R.id.text)
    }
    private fun startFollowing(qiContext: QiContext?) {
        val engagedHuman: Human = qiContext!!.humanAwareness.engagedHuman
        followHuman = FollowHuman(qiContext, engagedHuman)
        Log.i("seuraa", "seuraan")
        followHuman!!.start()
    }
    @SuppressLint("ResourceAsColor")
    override fun onRobotFocusGained(_qiContext: QiContext) {
        qiContext = _qiContext
        val topic: Topic = TopicBuilder.with(qiContext)
            .withResource(R.raw.labra_topic)
            .build()
        val qiChatbot: QiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(topic)
            .build()
        val locale = Locale(Language.FINNISH, Region.FINLAND);
        chat = ChatBuilder.with(qiContext).withChatbot(qiChatbot).withLocale(locale).build();
        chat.addOnStartedListener { Log.i("chat", "chat started")}
        val chatFuture : Future<Void?> = chat.async().run()
        if(qiContext.humanAwareness.engagedHuman != null ) {
            runOnUiThread {
                text.text = "Seurattavaa ihmistä ei löydy"
                text.setTextColor(R.color.red)
            }
        } else {
            runOnUiThread {
                text.text = "Valmiina seuraamaan!"
                text.setTextColor(R.color.green)
            }

        }
        qiContext.humanAwareness.addOnEngagedHumanChangedListener {
            if(qiContext.humanAwareness.engagedHuman == null) {
                runOnUiThread {
                    text.text = "Seurattavaa ihmistä ei löydy"
                    text.setTextColor(R.color.red)
                }
            } else {
                runOnUiThread {
                    text.text = "Valmiina seuraamaan!"
                    text.setTextColor(R.color.green)
                }
            }
        }
        chat.addOnHeardListener {heardPhrase ->
            Log.i("chat", heardPhrase.text)
            if(heardPhrase.text == "seuraa minua") {
                startFollowing(qiContext)
            }
            if(heardPhrase.text == "pysähdy") {
                Log.i("seuraa", "lopeta")
                followHuman!!.stop()
                followHuman = null
            }
        }
        qiChatbot.addOnEndedListener { endPhrase: String ->
            Log.i("chat", "chat ended = $endPhrase")
            chatFuture.requestCancellation()
        }
        chatFuture.thenConsume { future: Future<Void?> ->
            if (future.hasError()) {
                Log.e("Error", "Discussion finished with error.", future.error)
            }
        }
    }

    override fun onRobotFocusLost() {
        chat.removeAllOnStartedListeners()
        chat.removeAllOnHeardListeners()
        qiContext.humanAwareness.removeAllOnEngagedHumanChangedListeners()
    }

    override fun onRobotFocusRefused(reason: String?) {
        TODO("Not yet implemented")
    }
    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
}