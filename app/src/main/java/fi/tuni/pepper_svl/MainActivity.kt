package fi.tuni.pepper_svl

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.aldebaran.qi.sdk.design.activity.RobotActivity

class MainActivity : RobotActivity(), RobotLifecycleCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
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
        val say: Say = SayBuilder.with(qiContext)
            .withText("Hei ihminen!")
            .build()
        say.run()
    }

    override fun onRobotFocusLost() {

    }

    override fun onRobotFocusRefused(reason: String?) {

    }
}