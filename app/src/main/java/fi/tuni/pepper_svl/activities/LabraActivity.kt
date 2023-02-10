package fi.tuni.pepper_svl.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
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
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.`object`.actuation.*
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.conversation.Topic
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.util.FutureUtils
import com.softbankrobotics.dx.followme.FollowHuman
import fi.tuni.pepper_svl.R
import fi.tuni.pepper_svl.adapters.CustomAdapter
import fi.tuni.pepper_svl.data.RobotHelper
import fi.tuni.pepper_svl.data.Sananlaskut
import fi.tuni.pepper_svl.data.SaveFileHelper
import fi.tuni.pepper_svl.data.Vector2theta
import fi.tuni.pepper_svl.models.ItemsViewModel
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class LabraActivity : RobotActivity(), RobotLifecycleCallbacks {
    private lateinit var chat: Chat
    private lateinit var text : TextView
    private var qiContext: QiContext? = null
    private var followHuman: FollowHuman? = null
    private var humanAwareness: HumanAwareness? = null
    private var localizingAndMapping: Future<Void>? = null
    private var localizeAndMap: LocalizeAndMap? = null
    private var localize: Localize? = null
    private var explorationMap: ExplorationMap? = null
    private val filesDirectoryPath = "/sdcard/Maps"
    private val locationsFileName = "points.json"
    var savedLocations: TreeMap<String, AttachedFrame> = TreeMap()
    var robotHelper: RobotHelper? = null
    var saveFileHelper: SaveFileHelper? = null
    private val loadLocationSuccess: AtomicBoolean = AtomicBoolean(false)
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
    }

    @SuppressLint("ResourceAsColor")
    override fun onRobotFocusGained(_qiContext: QiContext) {
        qiContext = _qiContext
        humanAwareness = qiContext?.humanAwareness
        updateUi(humanAwareness?.engagedHuman != null)
        humanAwareness!!.addOnEngagedHumanChangedListener {
            updateUi(humanAwareness?.engagedHuman != null)
            stopFollowing()
        }
        startChat()
        loadLocations()

    }
    private fun loadLocations(): Future<Boolean>? {
        Log.i("test", "load started")
        return FutureUtils.futureOf<Boolean> { f: Future<Void?>? ->
            // Read file into a temporary hashmap.
            val file = File(filesDirectoryPath, locationsFileName)
            if (file.exists()) {
                val vectors: Map<String, Vector2theta> =
                    saveFileHelper!!.getLocationsFromFile(filesDirectoryPath, locationsFileName)

                // Clear current savedLocations.
                savedLocations = TreeMap()
                val mapFrame: Frame = robotHelper!!.mapFrame

                // Build frames from the vectors.
                for ((key, value) in vectors) {
                    // Create a transform from the vector2theta.
                    val t: Transform = value.createTransform()
                    Log.d("test", "loadLocations: $key")

                    // Create an AttachedFrame representing the current robot frame relatively to the MapFrame.
                    val attachedFrame = mapFrame.async().makeAttachedFrame(t).value

                    // Store the FreeFrame.
                    savedLocations[key] = attachedFrame
                    loadLocationSuccess.set(true)
                }
                Log.d("test", "loadLocations: Done")
                if (loadLocationSuccess.get()) return@futureOf Future.of<Boolean>(
                    true
                ) else throw Exception("Empty file")
            } else {
                throw Exception("No file")
            }
        }
    }
    fun goToLocation(location: String, orientationPolicy: OrientationPolicy) {
        Log.i("test", location)
        Log.i("test", robotHelper.toString())
        Log.i("test", savedLocations.toString())
        robotHelper!!.goToHelper.checkAndCancelCurrentGoto().thenConsume { aVoid ->
            robotHelper!!.holdAbilities(true)
            if (location.lowercase() == "mapframe") {
                robotHelper!!.goToHelper.goToMapFrame(false, false, orientationPolicy)
            } else if (location.lowercase() == "ChargingStation") {
                robotHelper!!.goToHelper.goToChargingStation(
                    savedLocations[location],
                    false,
                    false,
                    orientationPolicy
                )
            } else {
                // Get the FreeFrame from the saved locations.
                robotHelper!!.goToHelper.goTo(
                    savedLocations[location],
                    false,
                    false,
                    orientationPolicy
                )
            }
        }
    }

    override fun onRobotFocusLost() {
        chat.removeAllOnStartedListeners()
        chat.removeAllOnHeardListeners()
        humanAwareness?.removeAllOnEngagedHumanChangedListeners()
        localizeAndMap?.removeAllOnStatusChangedListeners()
        localize?.removeAllOnStatusChangedListeners()
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
        /*followHuman = engagedHuman?.let { FollowHuman(qiContext!!, it) }
        followHuman?.start()*/
        // Build the action.
        val approachHuman = ApproachHumanBuilder.with(qiContext)
            .withHuman(engagedHuman)
            .build()

// Run the action asynchronously.
        approachHuman.async().run()
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
                if(humanAwareness?.engagedHuman == null) {
                    val say: Say = SayBuilder.with(qiContext)
                        .withText("En näe ketään jota voisin seurata")
                        .build()
                    say.run()
                } else {
                    startFollowing()
                }
            }
            if(heardPhrase.text == "pysähdy") {
                stopFollowing()
            }
            if(heardPhrase.text == "sohvat") {
                goToLocation("sohvat", OrientationPolicy.ALIGN_X)
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
    fun itemDialog(item: String) {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(item)
            alertDialog.show()
        }
    }
}