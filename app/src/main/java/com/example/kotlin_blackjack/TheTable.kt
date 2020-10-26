package com.example.kotlin_blackjack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView

class TheTable : AppCompatActivity() {
    var isInitd = false
    val GAME_SCORE_CEIL = 10 // Dont let value be too much

    public var Cash = 0 // The table uses money the most, therefore base it in here
    val CashScaler = 4
    var isBetDoubled = false

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
    var ET_MinBet: EditText? = null
    var ET_Name: EditText? = null

    var TOG_UseCash: Switch? = null

    val AllSuits = listOf("Diamonds", "Clubs", "Hearts", "Spades")
    val AllSuitsC = listOf('♦', '♣', '♥', '♠')
    val AllFaces = listOf("Ace", "Jack", "Queen", "King", "Joker")
    val AllColors = listOf("Red" , "Black")

    class Card(val index:Int, val value:Int, val Suit:Int, var Holder:Boolean?) // Null = deck, true = player, false = dealer

    var Deck: Array<Card?> = arrayOfNulls<Card>(52)

    var Hand_Player: Array<Card?> = arrayOfNulls<Card>(52)
    var Hand_Dealer: Array<Card?> =  arrayOfNulls<Card>(52)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_the_table)
        // Start Code Area
        if (isInitd == false) {
            println("Initializing the Table")
            isInitd = true

            createDeck()
            initTable()
            resetGame()
        }
    }

    fun createDeck(){
        for (x in 0 until 13){
            // Make 4 at a time
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
    fun getSuitName(s: Int):String{ // Might not use, too long
        return AllSuits[s]
    }
    fun getSuitChar(s: Int):Char{
        return AllSuitsC[s]
    }
    fun getCardName(v: Int):String{ // Value, not index
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
        val arg = s % 2
        return AllColors[arg]
    }
    // val button = findViewById<Button>(R.id.Button)
    //fun sendMessage(view: View) {
    fun initTable(){
        // Reset everything
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
        ET_MinBet  = findViewById<EditText>(R.id.et_MinBet)
        ET_Name = findViewById<EditText>(R.id.et_PlayerName)

        TOG_UseCash = findViewById<Switch>(R.id.tog_UseCash)

        // Correct logic
        val cash_base = ET_MinBet!!.text.toString().toInt() ?: 100
        setCash(CashScaler * cash_base)


    }

    fun useCash():Boolean{
        return TOG_UseCash!!.isSelected
    }

    fun setCash(amt: Int):Int {
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

            TV_Cash!!.setText("$" + amt)
            //TV_CashM!!.setText(amt)

        }

        endState(isWin, amt)

        return amt
    }

    fun endState(isWin:Boolean, amt:Int){
        TV_Main!!.visibility = View.VISIBLE
        var mstr = "Tie Game"

        if (amt > 0) {
            if (isWin) {
                mstr = "You've won $" + amt
            } else {
                mstr = "You've lost $" + amt
            }
        }
        TV_Main!!.setText(mstr)

        B_Double!!.visibility = View.INVISIBLE
        B_Hit!!.visibility = View.INVISIBLE
        B_Stay!!.visibility = View.INVISIBLE
    }

    fun checkGameStatus(DMove: Boolean){
        // Make sure nobodys won
        println("Evaluate game")
        var pt: Int = getPlayerTotal()
        var dt: Int = getDealerTotal()

        var pr = pt <= 21
        var dr = dt <= 21

        if (DMove && dr && dt < 16){
            // Draw a card if dealer is below 16
            drawCardFromDeck(false)

            dt = getDealerTotal()
            dr = dt <= 21
        }

        // Show dealers hand
        TV_CDealer!!.setText(getDealerAll())

        if ((!pr && !dr) || (pr && dr && pt == dt)) {
            // Tie
            Game_Tie()
            return
        }

        // Busts
        if (!pr){
            // Bust Player
            // Lose
            Game_Loose()

        }else if (!dr){
            // Bust Dealer
            // Win
            Game_Win()

        }else if (dr && dt > pt){
            // Player Loss
            Game_Loose()
        }else if (pr && pt > dt){
            // PLayer wins
            Game_Win()
        }

    }

    fun clickMain(view: View){
        // Reset for another round
        // Or boot player
        resetGame()

        if (isTooBroke()) {
            println("Too poor, exiting")
            doGotoMain()
        }
    }

    fun getPlayerTotal(): Int{
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
        val t:String = ET_MinBet!!.text.toString()
        var amt = 100 * CashScaler

        if (t.length > 0){
            amt = t.toInt() * CashScaler
        }

        Cash = amt
    }

    fun getTotalBet(): Int {
        var b = ET_CurBet.toString()
        var m = ET_MinBet.toString()
        var bi:Int = 100
        var s:Int = 1


        if (isBetDoubled){
            s = 2
        }
        if (b.length > 0){
            bi = b.toInt()
        }
        if (m.length > 0){
            bi = Math.max(bi, m.toInt())
        }

        val ans:Int = (s * bi)
        return ans
    }

    fun getDealerTotal(): Int{
        var total = 0
        for (x in 0 until Hand_Dealer.size){
            val cur: Card = Hand_Dealer[x]!!
            total = total + Math.max( cur.value, GAME_SCORE_CEIL )
        }
        return total
    }

    fun getDealerAll():  String{
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



    fun Game_Win(){
        val amt = setCash(getTotalBet() * 2)
    }
    fun Game_Loose(){
        val amt = setCash(-1 * getTotalBet())

        if (isTooBroke()) {
            // Eject them from the game
            doTooBroke()
            return
        }
        // Keep going


    }
    fun Game_Tie(){
        println("Tie game")
        endState(false, 0)
    }
    fun resetGame(){
        TV_Name!!.setText( ET_Name!!.text )

        isBetDoubled = false

        Hand_Dealer = arrayOfNulls<Card>(52)
        Hand_Player = arrayOfNulls<Card>(52)

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

    fun getMinBet():Int{
        var arg = ET_MinBet!!.text.toString()
        var ans: Int = 0
        if (arg.length < 1) {
            ans = 100
            ET_MinBet!!.setText("100")
        }else{
            ans = arg.toInt() ?: 100
        }

        return ans
    }

    fun cardToStr(t:Card):String{
        var ans:String = ""

        ans = ans + getSuitChar(t.Suit) + getCardName(t.value)

        return ans
    }

    fun isTooBroke():Boolean{
        return TOG_UseCash!!.isChecked && Cash < getMinBet()
    }

    fun doTooBroke(){
        println("Too broke exit")
        doGotoMain()
    }

    fun isBust(isP:Boolean):Boolean{
        // Over 21?
        var amt: Int

        if (isP) {
            amt = getPlayerTotal()
        }else {
            amt = getDealerTotal()
        }

        return amt > 21
    }

    fun clickHit(view: View){
        // Add a card to clients hand
        doHit(true)

    }
    fun clickDouble(view: View){
        doDouble()

    }
    fun clickStay(view: View){
        doStay(true)

    }
    fun clickGotoMain(view: View){
        doGotoMain()
    }

    fun drawCardFromDeck(side: Boolean):Card{
        var go = true
        var temp: Card
        do {
            temp = Deck.random()!!
        } while (temp.Holder != null)
        // Mark as held
        temp.Holder = side

        if (side) {
            Hand_Player.plus(temp)
            TV_CPlayer!!.setText(cardToStr(temp))
        } else {
            Hand_Dealer.plus(temp)
        }

        return temp
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
        isBetDoubled = true
        B_Double!!.visibility = View.INVISIBLE

    }
    fun doGotoMain(){
        // Change activity
        TV_CashM!!.setText("$" + Cash)
        startActivity(Intent(this, MainActivity::class.java))
    }
}