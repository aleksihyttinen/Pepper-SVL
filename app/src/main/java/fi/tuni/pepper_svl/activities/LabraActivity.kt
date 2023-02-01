package fi.tuni.pepper_svl.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import fi.tuni.pepper_svl.adapters.CustomAdapter
import fi.tuni.pepper_svl.models.ItemsViewModel
import fi.tuni.pepper_svl.R

class LabraActivity : RobotActivity(), RobotLifecycleCallbacks {
    private lateinit var chat: Chat
    private lateinit var text : TextView
    private var qiContext: QiContext? = null
    private var followHuman: FollowHuman? = null
    private var humanAwareness: HumanAwareness? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labra)
        QiSDK.register(this, this)
        text = findViewById(R.id.text)
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = GridLayoutManager(this, 6)
        val deviceNames = arrayOf("Yeti", "Igloo",  "evondos", "somnox", "ohmni", "mpower", "kelosound",
            "oculus", "exoskeleton", "taikaseinä", "säkkituoli")
        val data = ArrayList<ItemsViewModel>()
        for (device in deviceNames) {
            data.add(ItemsViewModel(R.drawable.test, device))
        }
        val adapter = CustomAdapter(data)
        recyclerview.adapter = adapter
    }

    @SuppressLint("ResourceAsColor")
    override fun onRobotFocusGained(_qiContext: QiContext) {
        qiContext = _qiContext
        humanAwareness = qiContext?.humanAwareness
        startChat()
        updateUi(humanAwareness?.engagedHuman != null)
        humanAwareness!!.addOnEngagedHumanChangedListener {
            updateUi(humanAwareness?.engagedHuman != null)
            stopFollowing()
        }

    }

    override fun onRobotFocusLost() {
        chat.removeAllOnStartedListeners()
        chat.removeAllOnHeardListeners()
        humanAwareness?.removeAllOnEngagedHumanChangedListeners()
        qiContext = null
    }

    override fun onRobotFocusRefused(reason: String?) {
    }
    override fun onDestroy() {
        super.onDestroy()
        QiSDK.unregister(this, this)
    }
    private fun startFollowing() {
        val engagedHuman: Human? = humanAwareness?.engagedHuman
        followHuman = engagedHuman?.let { FollowHuman(qiContext!!, it) }
        followHuman?.start()
    }
    private fun stopFollowing() {
        followHuman?.stop()
        followHuman = null
    }
    private fun updateUi(humanFound: Boolean) {
        runOnUiThread {
            if (humanFound) {
                text.text = "Valmiina seuraamaan!"
                text.setTextColor(ContextCompat.getColor(this, R.color.green))
            } else {
                text.text = "Seurattavaa ihmistä ei löydy"
                text.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }
    private fun startChat() {
        val topic: Topic = TopicBuilder.with(qiContext)
            .withResource(R.raw.labra_topic)
            .build()
        val qiChatbot: QiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(topic)
            .build()
        val locale = Locale(Language.FINNISH, Region.FINLAND)
        chat = ChatBuilder.with(qiContext).withChatbot(qiChatbot).withLocale(locale).build()
        chat.addOnStartedListener { Log.i("chat", "chat started")}
        val chatFuture = chat.async().run()
        chat.addOnHeardListener { heardPhrase ->
            if(heardPhrase.text == "seuraa minua") {
                startFollowing()
            }
            if(heardPhrase.text == "pysähdy") {
                stopFollowing()
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
}