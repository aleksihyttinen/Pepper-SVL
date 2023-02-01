package fi.tuni.pepper_svl

import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.PhraseSet
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder

class Sananlaskut {
    fun getPhrases(): Array<String> {
        return arrayOf(
            "Parempi katsoa kuin katua",
            "Joka kuuseen kurkottaa, se katajaan kapsahtaa",
            "Ei lämmin luita riko",
            "Ei oppi ojaan kaada, eikä tieto tieltä työnnä",
            "Ei omena kauas puusta putoa",
            "Ei ole koiraa karvoihin katsominen",
            "Ei Roomaakaan päivässä rakennettu",
            "Eteenpäin, sanoi mummo lumessa",
            "Hyvin suunniteltu on puoliksi tehty",
            "Hyvä antaa vähästään, paha ei paljostaankaan",
            "Hädässä ystävä tunnetaan",
            "Jokainen on oman onnensa seppä",
            "Kaksi kärpästä yhdellä iskulla",
            "Kertaus on opintojen äiti",
            "Kuin apteekin hyllyltä",
            "Kyllä routa porsaan kotiin ajaa",
            "Lapsen suusta kuulee totuuden",
            "Loppui lyhyeen kuin kanan lento",
            "Luulo ei ole tiedon väärti",
            "Minkä nuorena oppii, sen vanhana taitaa",
            "Mitä useampi kokki, sitä huonompi soppa",
            "Niin metsä vastaa kuin sinne huudetaan",
            "Ojasta allikkoon",
            "On taottava silloin, kun rauta on kuuma",
            "Paistaa se päivä risukasaankin",
            "Ruoho on vihreämpää aidan toisella puolen",
            "Suutarin lapsilla ei ole kenkiä",
            "Tie miehen sydämeen käy vatsan kautta",
            "Uusi lumi on vanhan surma",
            "Vesi vanhin voitehista",
            "Vierivä kivi ei sammaloidu",
            "Älä laita kaikkia munia samaan koriin",
            "Alku aina hankalaa, lopussa kiitos seisoo",
            "Ei auta itku markkinoilla",
            "Ei haukku haavaa tee",
            "Hätä ei lue lakia",
            "Ilta on aamua viisaampi",
            "Joka toiselle kuoppaa kaivaa, se itse siihen lankeaa",
            "Kateus vie kalatkin vedestä",
            "Kolmas kerta toden sanoo",
            "Kyllä sokeakin kana joskus jyvän löytää",
            "Nauru pidentää ikää",
            "Oma maa mansikka, muu maa mustikka",
            "Parempi katsoa kuin katua",
            "Parempi pyy pivossa kuin kymmenen oksalla",
            "Pilkka sattuu omaan nilkkaan",
            "Puhtaus on puoli ruokaa",
            "Se koira älähtää, johon kalikka kalahtaa",
            "Se parhaiten nauraa, joka viimeksi nauraa",
            "Sopu sijaa antaa",
            "Vahinko ei tule kello kaulassa"
        )
    }


    private fun createPhraseSet(qiContext: QiContext?): PhraseSet {
        val phrases = listOf(
            "Alku", "Ei", "Eteenpäin", "Hyvin", "Hyvä", "Hädässä", "Hätä", "Ilta", "Joka", "Jokainen",
            // the rest of the phrases...
        )
        return PhraseSetBuilder.with(qiContext)
            .withTexts(*phrases.toTypedArray())
            .build()
    }

}