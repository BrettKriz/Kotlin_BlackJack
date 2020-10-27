package com.example.kotlin_blackjack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    var ET_MinBet:EditText? = null
    var TV_CashM:TextView? = null
    var TOG_UseCash:Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Start Code Area
        ET_MinBet  = findViewById<EditText>(R.id.et_MinBet)
        TV_CashM = findViewById<TextView>(R.id.tv_Cash_Menu)
        TOG_UseCash = findViewById<Switch>(R.id.tog_UseCash)

        updateCash()
    }

    fun updateCash(){
        TV_CashM!!.setText("$ " + TheTable.SHARED.Cash)
    }

    fun clickReset(view: View){
        println("[i] Reseting money to Minbet")

        // Reset money
        TheTable.SHARED.Cash = ( TheTable.SHARED.CashScaler * TheTable.SHARED.MinBet )

        updateCash()
        updateSHARED()
    }

    fun updateSHARED(){
        println("[+] Updating Main SHARED data")

        TheTable.SHARED.UseCash = TOG_UseCash!!.isChecked()
        TheTable.SHARED.MinBet = ET_MinBet!!.text.toString().toInt() ?: 100

    }

    fun clickGotoGame(view: View){
        // Change activity
        println("[i] Entering the Table")

        updateSHARED()

        updateCash()
        startActivity(Intent(this, TheTable::class.java))
    }

}