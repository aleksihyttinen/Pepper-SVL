package fi.tuni.pepper_svl.adapters

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.pepper_svl.R
import fi.tuni.pepper_svl.data.Sananlaskut
import fi.tuni.pepper_svl.models.ItemsViewModel

fun getUrl(name: String) : String {
    when (name) {
        "Yeti" -> return "https://frendy.fi/yeti-jattitabletti"
        "Pepper" -> return "https://www.robotie.fi/tuotteet/pepper/"
        "Evondos" -> return "https://www.evondos.fi/"
        "Ohmni" -> return "https://ohmnilabs.com/products/ohmni-telepresence-robot/"
        "Somnox" -> return "https://somnox.com/"
        "Mpower" -> return "http://mpower-bestrong.com/fi/science.html"
        "Kelosound" -> return "https://kelosound.com/fi/pikkukelo/"
        "Oculus" -> return "https://www.meta.com/fi/quest/products/quest-2/"
        "Exoskeleton" -> return "https://www.epressi.com/tiedotteet/terveys/puettava-ylaraaja-exoskeleton-auttaa-tyontekijoita-jaksamaan-pitempaan-ja-ehkaisemaan-vaivoja.html"
        "Igloo" -> return "https://www.igloovision.com/"
        "Säkkituoli" -> return "https://www.haltija.fi/tuotteet/kuntoutus-ja-terapia/aistit-ja-aktivointi/shx-aistituotteet/vibroakustinen-sakkituoli/"
        "Taikaseinä" -> return "https://www.aistikanava.fi/taikaseina/"
        "Älyseinä" -> return "https://oioi.fi/smart-space-products/"
        "Optogait" -> return "http://optogait.com/Applications"
    }
    return ""
}


class CustomAdapter(private val mList: List<ItemsViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.labra_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageButton.setImageResource(ItemsViewModel.image)
        holder.imageButton.scaleType = ImageView.ScaleType.CENTER_INSIDE
        holder.imageButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(holder.itemView.context)
            alertDialog.setTitle(ItemsViewModel.name)
            alertDialog.setMessage("Haluatko katsoa lisätietoja valmistajan sivuilta?")
            alertDialog.setPositiveButton("Kyllä") { dialog, whichButton ->
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(getUrl(ItemsViewModel.name)))
                holder.itemView.context.startActivity(i)
            }
            alertDialog.setNegativeButton("Ei") { dialog, whichButton ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
        // sets the text to the textview from our itemHolder class
        holder.textView.text = ItemsViewModel.name

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageButton: ImageButton = itemView.findViewById(R.id.image_btn)
        val textView: TextView = itemView.findViewById(R.id.name)
    }
}