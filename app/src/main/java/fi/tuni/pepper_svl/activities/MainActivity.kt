package fi.tuni.pepper_svl.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import fi.tuni.pepper_svl.R

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    private var playAnimation = false
    override fun onCreate(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) {
            playAnimation = true
        }
        Log.i("test",savedInstanceState.toString())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        QiSDK.register(this, this)
        val sananlaskuButton : Button = findViewById(R.id.sananlasku_button)
        sananlaskuButton.setOnClickListener {
            val intent = Intent(this, SananlaskuActivity::class.java)
            startActivity(intent)
        }
        val labraButton : Button = findViewById(R.id.labra_button)
        labraButton.setOnClickListener {
            val intent = Intent(this, LabraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    override fun onRobotFocusGained(qiContext: QiContext?) {
        if(playAnimation) {
            val say: Say = SayBuilder.with(qiContext)
                .withText("Hei ihminen, minä olen Pepper")
                .build()
            say.async().run()
            val animation = AnimationBuilder.with(qiContext).withResources(R.raw.wave).build()
            val animate = AnimateBuilder.with(qiContext).withAnimation(animation).build()
            animate.run()
        }
    }

    override fun onRobotFocusLost() {

    }

    override fun onRobotFocusRefused(reason: String?) {

    }
}