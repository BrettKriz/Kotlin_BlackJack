package com.example.kotlin_blackjack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class TheTable : AppCompatActivity() {
    var isInitd = false // Make it easy to tell if we've started yet

    // Define finals
    val GAME_SCORE_CEIL = 10 // Dont let value be too much

    //var Cash = 400 // The table uses money the most, therefore base it in here
    //val CashScaler = 4
    var BetDoubled = false

    // Track static elements
    var B_Hit: Button? = null
    var B_Double: Button? = null
    var B_Stay: Button? = null
    var B_GotoMain: Button? = null

    var TV_Main: TextView? = null
    var TV_CPlayer: TextView? = null
    var TV_CDealer: TextView? = null
    var TV_Cash: TextView? = null
    var TV_CashM: TextView? = null
    var TV_Name: TextView? = null

    var ET_CurBet: EditText? = null
    //var ET_MinBet: EditText? = null
    //var ET_Name: EditText? = null

    //var TOG_UseCash: Switch? = null

    // Create resource tables
    val AllSuits = listOf("Diamonds", "Clubs", "Hearts", "Spades")
    val AllSuitsC = listOf('♦', '♣', '♥', '♠')
    val AllFaces = listOf("Ace", "Jack", "Queen", "King", "Joker")
    val AllColors = listOf("Red" , "Black")

    // Define a custom type for cards
    class Card(val index:Int, val value:Int, val Suit:Int, var Holder:Boolean?) // Null = deck, true = player, false = dealer

    var Deck: Array<Card?> = arrayOfNulls<Card>(52)
    var Hand_Player: Array<Card?> = arrayOfNulls<Card>(13)
    var Hand_Dealer: Array<Card?> =  arrayOfNulls<Card>(13)

    // Add a companion object to store shared vars
    companion object SHARED {
        // Forget reflection
        // COMPANIONS ARE GODLIKE!

        // vars
        var Cash = 400
        var MinBet = 100
        var UseCash = true
        var PName = "Player"
        val CashScaler = 4

        // funcs
    }


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_the_table)
        // Start Code Area
        if (isInitd == false) {
            println("Initializing the Table")
            isInitd = true

            createDeck()
            initTable()
            // Start the game with reset
            resetGame()
        }
    }

    fun createDeck(){
        // Create all 13 values of cards
        // and create the suits as well
        println("[i] Creating the Deck!")

        for (x in 0 until 13){
            // Make all 4

            val i1 = 0 + x
            val i2 = 1 + x
            val i3 = 2 + x
            val i4 = 3 + x
            println("[i] Creating cards @ index $i1 to $i4")

            Deck[i1] = Card(i1, x+1, 0, null)
            Deck[i2] = Card(i2, x+1, 1, null)
            Deck[i3] = Card(i3, x+1, 2, null)
            Deck[i4] = Card(i4, x+1, 3, null)
        }
    }

    fun initTable(){
        // Reset everything
        println("[i] Building element vars")

        B_Hit = findViewById<Button>(R.id.b_GHit)
        B_Double = findViewById<Button>(R.id.b_GDouble)
        B_Stay = findViewById<Button>(R.id.b_GStay)
        B_GotoMain = findViewById<Button>(R.id.b_gotoGame)

        TV_Main = findViewById<TextView>(R.id.tv_MainText)
        TV_CPlayer = findViewById<TextView>(R.id.tv_PlayerCards)
        TV_CDealer = findViewById<TextView>(R.id.tv_DealerCards)
        TV_Cash = findViewById<TextView>(R.id.tv_Cash_Table)
        TV_CashM = findViewById<TextView>(R.id.tv_Cash_Menu)
        TV_Name = findViewById<TextView>(R.id.tv_Name)

        ET_CurBet  = findViewById<EditText>(R.id.et_CurBet)
        //ET_MinBet  = findViewById<EditText>(R.id.et_MinBet)
        //ET_Name = findViewById<EditText>(R.id.et_PlayerName)

        //TOG_UseCash = findViewById<Switch>(R.id.tog_UseCash)

        // Correct logic
        resetCash()
    }

    fun setCash(amt: Int):Int{
        // Money needs to be exchanged
        // The game is over
        println("[i] Setting cash to $amt")

        val isWin = amt > 0

        if (useCash()) {
            val minb = getMinBet()
            if (amt < minb && isWin) {
                println("[!] User attempted to undervalue bets")
                Cash += minb
            }else{
                Cash += amt
            }

            TV_Cash!!.setText("$ " + amt)
            //TV_CashM!!.setText(amt)
        }

        endState(isWin, amt) // Round over

        return amt
    }

    fun endState(isWin:Boolean, amt:Int){
        // A hang state which unlocks the central text
        println("Hiding buttons, unhiding text with onClick")

        TV_Main!!.visibility = View.VISIBLE
        var mstr = "Tie Game"

        if (amt > 0) {
            if (isWin) {
                mstr = "You've won $ " + amt
            } else {
                var sadd = ""
                if (isBust(true)){
                    sadd = "Bust!"
                }
                mstr = sadd + "You've lost $ " + amt
            }
        }

        // Let them know the game will end
        if (isTooBroke()){
            mstr = "You're broke!"
        }

        TV_Main!!.setText(mstr)

        B_Double!!.visibility = View.INVISIBLE
        B_Hit!!.visibility = View.INVISIBLE
        B_Stay!!.visibility = View.INVISIBLE
    }

    fun checkGameStatus(DMove: Boolean){
        // Someone has won, who is it?
        println("[i] Evaluating game")

        var pt: Int = getPlayerTotal()
        var dt: Int = getDealerTotal()

        var pr = pt <= 21
        var dr = dt <= 21

        if (DMove && dr && dt < 16){
            // Draw a card if dealer is below 16
            // and give it to the dealer
            drawCardFromDeck(false)

            dt = getDealerTotal()
            dr = dt <= 21
        }

        // Show the dealers full hand
        TV_CDealer!!.setText(getDealerAll())

        if ((!pr && !dr) || (pr && dr && pt == dt)) {
            // Tie
            game_Tie()
            return
        }else if (!pr){ // Check Busts, then amounts
            // Bust Player
            // Lose
            game_Lose()

        }else if (!dr){
            // Bust Dealer
            // Win
            game_Win()

        }else if (dr && dt > pt){
            // Player Loss
            game_Lose()
        }else if (pr && pt > dt){
            // PLayer wins
            game_Win()
        }

    }

    fun drawCardFromDeck(side: Boolean):Card{
        // Take a number from the deck at random
        // and give it a side of the table

        var go = true
        var temp: Card

        // Get a random card thats not in a hand
        do {
            temp = Deck.random()!!
        } while (temp.Holder != null)

        // Mark as held
        temp.Holder = side

        // Add the card to the Hand
        if (side) {
            Hand_Player.plus(temp)
            // Update card string
            TV_CPlayer!!.setText(cardToStr(temp))
        } else {
            Hand_Dealer.plus(temp)
        }

        return temp
    }

    fun resetGame(){
        // Rest sate to a start

        val name_s = SHARED.PName
        TV_Name!!.setText( name_s )

        BetDoubled = false

        Hand_Dealer = arrayOfNulls<Card>(13)
        Hand_Player = arrayOfNulls<Card>(13)

        for (x in 0 until Deck.size){
            // Clean deck
            Deck[x]!!.Holder = null
        }

        // Dealers Hand
        var damt = 0
        val peak: Card = drawCardFromDeck(false)
        TV_CDealer!!.setText(cardToStr(peak))
        drawCardFromDeck(false)
        damt = getDealerTotal()

        // Players Hand
        var pamt = 0
        drawCardFromDeck(true)
        drawCardFromDeck(true)
        pamt = getPlayerTotal()

        if (damt >= 21 || pamt >= 21){
            checkGameStatus(damt < 16)
            return
        }

        // Now wait for player to play
        B_Double!!.visibility = View.VISIBLE
        B_Hit!!.visibility = View.VISIBLE
        B_Stay!!.visibility = View.VISIBLE
        TV_Main!!.visibility = View.INVISIBLE
    }

    fun game_Win(){
        // Add money and change status
        println("[i] Win game")

        val amt = setCash(getTotalBet() * 2)
    }

    fun game_Lose(){
        // Subtract money and change status
        println("[i] Lose game")

        val amt = setCash(-1 * getTotalBet())
    }

    fun game_Tie(){
        println("[i] Tie game")
        endState(false, 0)
    }

    fun getPlayerTotal():Int{
        // Sum the cards

        var total = 0
        var str:String = "None"

        for (x in 0 until Hand_Player.size){
            val cur: Card = Hand_Player[x]!!

            if (str.length < 1){
                str = cardToStr(cur)
            }else{
                str = str + " , " + cardToStr(cur)
            }

            total = total + Math.max( cur.value, GAME_SCORE_CEIL )
        }

        TV_CPlayer!!.setText(str)

        return total
    }

    fun resetCash(){
        // Reset cash

        //val t:String = ET_MinBet!!.text.toString()
        val t:Int = 100
        var amt = 100 * CashScaler

        if (t != null){
            amt = t * CashScaler
        }else{
            amt = 100
        }

        Cash = amt
    }

    fun getTotalBet():Int{
        // Calculate the bet in full

        var b = ET_CurBet.toString()
        var m = SHARED.MinBet
        var bi:Int = 100
        var s:Int = 1

        if (BetDoubled){
            s = 2
        }
        if (b.length > 0){
            bi = b.toInt()
        }
        if (m != null) {
            bi = Math.max(bi, m)
        } else {
            bi = 100
            println("[!] Shared bet min came back null! 0,0")
        }

        val ans:Int = (s * bi)
        return ans
    }

    fun getDealerTotal():Int{
        // Calculate dealers cards
        var total = 0

        for (x in 0 until Hand_Dealer.size){
            val cur: Card = Hand_Dealer[x]!!
            total = total + Math.max( cur.value, GAME_SCORE_CEIL )
        }

        return total
    }

    fun getDealerAll():String{
        // Get the string for ALL of the dealers cards
        var str:String = "None"

        for (x in 0 until Hand_Dealer.size){
            val cur: Card = Hand_Dealer[x]!!

            if (str.length < 1) {
                str = cardToStr(cur)
            } else {
                str = str + " , " + cardToStr(cur)
            }
        }

        return str
    }

    fun getMinBet():Int{
        // Grab the current value
        // for Min Bet

        var arg = MinBet
        var ans: Int = 0

        ans = arg // Removed alot of test code here

        return ans
    }

    fun isBust(isP:Boolean):Boolean{
        // Over 21?
        var amt: Int

        if (isP) {
            // Is the player bust?
            amt = getPlayerTotal()
        } else {
            // Is the dealer bust?
            amt = getDealerTotal()
        }

        return amt > 21
    }

    fun isTooBroke():Boolean{
        // Is player too broke?
        val b1:Boolean = SHARED.UseCash
        return b1 && Cash < getMinBet()
    }

    fun doTooBroke(){
        println("[i] Too broke, exiting..")
        doGotoMain()
    }

    fun useCash():Boolean{
        // Return the value of the slider
        return UseCash
    }

    fun getSuitName(s: Int):String{
        // Might not use, too long
        // see: Diamond Queen
        return AllSuits[s]
    }

    fun getSuitChar(s: Int):Char{
        return AllSuitsC[s]
    }

    fun getCardName(v: Int):String{
        // Value, not index
        // Returns the name of the card
        var ans = v.toString()
        val arg = v // Casting relic

        if (arg == 1 ) {
            // Search for alt
            ans = AllFaces[0]
        }else if (arg > 10) {
            ans = AllFaces[arg - 10]
        }

        return ans
    }

    fun getCardColor(s: Int):String{
        // Get colors of card
        // @@Todo Colored cards
        val arg = s % 2
        return AllColors[arg]
    }

    fun cardToStr(t:Card):String{
        // Build a sudo card
        var ans:String = "["

        ans = ans + getSuitChar(t.Suit) + getCardName(t.value) + "]"

        return ans
    }

    fun clickMain(view: View){
        // Reset for another round
        // Or boot player
        resetGame()

        if (isTooBroke()) {
            doTooBroke()
        }
    }

    fun clickHit(view: View){
        // Add a card to clients hand
        doHit(true)

    }
    fun clickDouble(view: View){
        // Double the bet
        doDouble()

    }
    fun clickStay(view: View){
        // Stay and end dealing
        doStay(true)

    }
    fun clickGotoMain(view: View){
        // Exit to the main menu
        doGotoMain()
    }

    fun doHit(side: Boolean){
        // Add a card to the hand
        drawCardFromDeck(side)
        if (isBust(true)) {
            checkGameStatus(false)
        }
    }

    fun doStay(side:Boolean){
        // Trigger the ending

        checkGameStatus(isBust(false))
    }

    fun doDouble(){
        // Dealer cant bet
        BetDoubled = true
        B_Double!!.visibility = View.INVISIBLE

    }

    fun doGotoMain(){
        // Change activity
        TV_CashM!!.setText("$ " + Cash)
        startActivity(Intent(this, MainActivity::class.java))
    }
}