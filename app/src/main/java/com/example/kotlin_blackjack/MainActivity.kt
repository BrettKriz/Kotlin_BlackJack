package com.example.kotlin_blackjack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    var ET_MinBet:EditText? = null

    var TV_CashM: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Start Code Area
        ET_MinBet  = findViewById<EditText>(R.id.et_MinBet)
        TV_CashM = findViewById<TextView>(R.id.tv_Cash_Menu)

        updateCash()
    }

    fun updateCash(){
        TV_CashM!!.setText("$" + TheTable::Cash)
    }

    fun clickReset(){
        println("[i] Reseting money to Minbet")
        TheTable::resetCash.call()
        updateCash()
    }

    fun clickGotoGame(){
        // Change activity
        println("[i] Entering the Table")
        updateCash()
        startActivity(Intent(this, TheTable::class.java))
    }

}