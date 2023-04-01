package fi.tuni.pepper_svl.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.`object`.actuation.*
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.softbankrobotics.dx.followme.FollowHuman
import fi.tuni.pepper_svl.R
import fi.tuni.pepper_svl.adapters.CustomAdapter
import fi.tuni.pepper_svl.models.ItemsViewModel
import java.util.*


class LabraActivity : RobotActivity(), RobotLifecycleCallbacks {
    private  var chat: Chat? = null
    private var chatFuture: Future<Void>? = null
    private lateinit var text : TextView
    private var qiContext: QiContext? = null
    private var humanAwareness: HumanAwareness? = null
    private lateinit var menuButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_labra)
        QiSDK.register(this, this)
        text = findViewById(R.id.text)
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = GridLayoutManager(this, 4)
        val deviceNames = arrayOf("Yeti", "Igloo",  "Evondos", "Somnox", "Ohmni", "Mpower", "Kelosound",
            "Oculus", "Exoskeleton", "Taikaseinä", "Säkkituoli", "Älyseinä", "Optogait", "Pepper")
        val data = ArrayList<ItemsViewModel>()
        for (device in deviceNames) {
            data.add(ItemsViewModel(this.resources.getIdentifier(device.lowercase().replace("ä", "a"), "drawable", this.packageName), device))
        }
        val adapter = CustomAdapter(data)
        recyclerview.adapter = adapter
        menuButton = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onRobotFocusGained(_qiContext: QiContext) {
        qiContext = _qiContext
        humanAwareness = qiContext?.humanAwareness
        updateUi(humanAwareness?.engagedHuman != null)
        humanAwareness!!.addOnEngagedHumanChangedListener {
            updateUi(humanAwareness?.engagedHuman != null)
        }
        val say: Say = SayBuilder.with(qiContext)
            .withText("Nyt voit pyytää minua kertomaan labrasta löytyvistä laitteista..." +
                    "Sano esimerkiksi kerro itsestäsi")
            .build()
        say.run()
        startChat()
    }
    override fun onRobotFocusLost() {
        chat?.removeAllOnStartedListeners()
        chat?.removeAllOnHeardListeners()
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
        if (qiContext?.power?.chargingFlap?.state?.open == true) {
            cancelChat()
            val say: Say = SayBuilder.with(qiContext)
                .withText("Latausporttini on auki, en voi liikkua ennen kuin suljet sen.")
                .build()
            say.run()
            startChat()
        } else {
            val engagedHuman: Human? = humanAwareness?.engagedHuman
            val approachHuman = ApproachHumanBuilder.with(qiContext)
                .withHuman(engagedHuman)
                .build()
            approachHuman.async().run()
        }
    }
    private fun updateUi(humanFound: Boolean) {
        runOnUiThread {
            if (humanFound) {
                text.text = "Valmiina liikkumaan!"
                text.setTextColor(ContextCompat.getColor(this, R.color.green))
            } else {
                text.text = "En näe ketään"
                text.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }
    }
    private fun cancelChat() {
        chatFuture?.requestCancellation()
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
        chat?.addOnStartedListener { Log.i("chat", "chat started")}
        chat?.listeningBodyLanguage = BodyLanguageOption.DISABLED
        chatFuture = chat?.async()?.run()
        chat?.addOnHeardListener { heardPhrase ->
            if(heardPhrase.text.contains("tule tänne", ignoreCase = true)) {
                if(humanAwareness?.engagedHuman == null) {
                    cancelChat()
                    val say: Say = SayBuilder.with(qiContext)
                        .withText("En näe ketään jonka luokse liikkua")
                        .build()
                    say.run()
                    startChat()
                } else {
                    startFollowing()
                }
            }
            if(heardPhrase.text.lowercase() == "takaisin päävalikkoon") {
                runOnUiThread {menuButton.performClick()}
            }
        }
        qiChatbot.addOnEndedListener { endPhrase: String ->
            Log.i("chat", "chat ended = $endPhrase")
            chatFuture?.requestCancellation()
        }
        chatFuture?.thenConsume { future: Future<Void?> ->
            if (future.hasError()) {
                Log.e("Error", "Discussion finished with error.", future.error)
            }
        }
    }
}