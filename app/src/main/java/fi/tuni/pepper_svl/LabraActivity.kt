package fi.tuni.pepper_svl

import android.os.Bundle
import android.util.Log
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labra)
        QiSDK.register(this, this)
    }

    override fun onRobotFocusGained(qiContext: QiContext) {
        val topic: Topic = TopicBuilder.with(qiContext)
            .withResource(R.raw.labra_topic)
            .build()
        val humanAwareness: HumanAwareness = qiContext.humanAwareness
        val engagedHuman: Human = humanAwareness.engagedHuman
        val followHuman = FollowHuman(qiContext, engagedHuman)
        val qiChatbot: QiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopic(topic)
            .build()
        val locale = Locale(Language.FINNISH, Region.FINLAND);
        chat = ChatBuilder.with(qiContext).withChatbot(qiChatbot).withLocale(locale).build();
        chat.addOnStartedListener { Log.i("chat", "chat started")}
        val chatFuture : Future<Void?> = chat.async().run();
        chat.addOnHeardListener {heardPhrase ->
            Log.i("chat", heardPhrase.text)
            if(heardPhrase.text == "Seuraa minua" && engagedHuman != null) {
                followHuman.start()
            }
            if(heardPhrase.text == "Lopeta seuraaminen" && engagedHuman != null) {
                followHuman.stop()
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
    }

    override fun onRobotFocusRefused(reason: String?) {
        TODO("Not yet implemented")
    }
    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }
}